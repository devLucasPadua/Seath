package com.example.seath.service;

import com.example.seath.dto.ScrapedItem;
import com.example.seath.model.Empresa;
import com.example.seath.model.Pregao;
import com.example.seath.model.Produto;
import com.example.seath.repository.EmpresaRepository;
import com.example.seath.repository.PregaoRepository;
import com.example.seath.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WebScraperService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private PregaoRepository pregaoRepository;
    @Autowired private ProdutoRepository produtoRepository; // Para catálogo de produtos
    @Autowired private EditalParserService editalParserService; // Injeção

    // ==========================================================================
    // 1. ORQUESTRADOR HÍBRIDO
    // ==========================================================================
    public List<ScrapedItem> extrairHibrido(String url) throws IOException {
        String idLicitacao = extrairIdLicitacao(url);
        if (idLicitacao == null) throw new IOException("ID não identificado na URL.");

        System.out.println(">>> [SEATH] 1. Web API (Itens)...");
        List<ScrapedItem> itensWeb = getItensDaWeb(idLicitacao);
        
        // Se vazio, paramos
        if (itensWeb.isEmpty()) return itensWeb;

        // Atualiza Catálogo de Produtos automaticamente (Cria produtos sem código se novos)
        atualizarCatalogoProdutos(itensWeb);

        // PREPARA AMBIENTE SELENIUM (Uma única sessão para Vencedores + Editais)
        WebDriverManager.chromedriver().setup();
        Path tempDir = Files.createTempDirectory("seath_run_" + System.currentTimeMillis());
        File tempDirFile = tempDir.toFile();
        
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--ignore-certificate-errors");
        // User agent real evita 403/500
        o.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", tempDir.toAbsolutePath().toString());
        prefs.put("plugins.always_open_pdf_externally", true);
        o.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(o);

        try {
            driver.get(url);
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".lista-registros, table")));

            // --- 2. BUSCAR VENCEDORES/ATA ---
            System.out.println(">>> [SEATH] 2. Buscando Vencedores...");
            File pdfVencedores = baixarArquivoPorLogica(driver, tempDirFile, "VENCEDORES");
            
            if (pdfVencedores != null) {
                Map<Integer, String> mapaVencedores = mapearVencedoresPdf(pdfVencedores);
                // Realiza o JOIN
                for (ScrapedItem item : itensWeb) {
                    try {
                        Integer cod = Integer.parseInt(item.getNumero());
                        item.setEmpresa(mapaVencedores.getOrDefault(cod, ""));
                    } catch (Exception e) {}
                }
                pdfVencedores.delete();
            }

            // --- 3. BUSCAR DADOS DO EDITAL (Loop e Validação) ---
            System.out.println(">>> [SEATH] 3. Escaneando Editais...");
            
            // Recarrega elementos da lista para evitar StaleReference
            List<WebElement> listaArquivos = driver.findElements(By.cssSelector(".lista-registros .item"));
            
            Map<String, String> metadadosFinais = new HashMap<>();
            
            for (int i = 0; i < listaArquivos.size(); i++) {
                // Precisa recarregar a cada iteração do loop se a pagina mudou, mas aqui nao muda
                // Porem, o findElements devolve objetos. Vamos varrer o texto.
                // Re-buscamos para ser seguro.
                List<WebElement> currentList = driver.findElements(By.cssSelector(".lista-registros .item"));
                WebElement linha = currentList.get(i);
                
                String txt = linha.getText().toUpperCase();
                if (txt.contains("EDITAL") && txt.contains("PDF")) {
                    System.out.println("    -> Verificando Edital na linha " + (i+1));
                    WebElement btn = buscarBotaoNoElemento(linha);
                    
                    if (btn != null) {
                        File pdfEdital = baixarClickAndWait(driver, btn, tempDirFile);
                        if (pdfEdital != null) {
                            Map<String, String> d = editalParserService.extrairDadosEdital(pdfEdital);
                            if(d.containsKey("pregao")) metadadosFinais.put("pregao", d.get("pregao"));
                            if(d.containsKey("processo")) metadadosFinais.put("processo", d.get("processo"));
                            if(d.containsKey("objeto")) metadadosFinais.put("objeto", d.get("objeto"));
                            pdfEdital.delete();
                            
                            // Se achou os principais, pode parar de baixar editais antigos
                            if(metadadosFinais.containsKey("pregao") && metadadosFinais.containsKey("processo")) {
                                break; 
                            }
                        }
                    }
                }
            }

            // --- 4. ATUALIZAR PREGÃO NO BANCO ---
            atualizarPregaoNoBanco(idLicitacao, 
                    metadadosFinais.get("pregao"), 
                    metadadosFinais.get("processo"), 
                    metadadosFinais.get("objeto"));

        } catch(Exception e) {
            System.err.println(">>> [SEATH] Erro Selenium: " + e.getMessage());
        } finally {
            if(driver!=null) try{driver.quit();}catch(Exception e){}
            try {
                // Cleanup folder
                Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch(Exception e){}
        }

        return itensWeb;
    }

    // ==========================================================================
    // WEB JSOUP
    // ==========================================================================
    private List<ScrapedItem> getItensDaWeb(String id) {
        List<ScrapedItem> l = new ArrayList<>();
        int pag = 1;
        boolean c = true;
        while(c) {
            try {
                String u = "https://compras.api.portaldecompraspublicas.com.br/v2/licitacao/" + id + "/itens?filtro=&pagina=" + pag;
                String j = Jsoup.connect(u).ignoreContentType(true).ignoreHttpErrors(true).sslSocketFactory(criarSocketFactorySemSSL()).userAgent("Mozilla/5.0").execute().body();
                List<ScrapedItem> p = parsearJsonItens(j);
                if(p.isEmpty()) c = false; else { l.addAll(p); pag++; Thread.sleep(200); }
                if(pag>50) break;
            } catch(Exception e){ c = false; }
        }
        return l;
    }
    private List<ScrapedItem> parsearJsonItens(String json) {
        List<ScrapedItem> l = new ArrayList<>();
        try {
            JsonNode r = objectMapper.readTree(json);
            JsonNode it = r.path("itens").path("result");
            if(it.isMissingNode()) it = r.path("result");
            if(it.isArray()){
                for(JsonNode n : it){
                    String cd = n.path("codigo").asText("-");
                    String d = n.path("descricao").asText("").replaceAll("(?i)^Item\\s+\\d+\\s+-\\s+Lote\\s+\\d+:\\s*", "").trim();
                    if(d.isEmpty() && cd.equals("-")) continue;
                    l.add(new ScrapedItem(cd, d, n.path("unidade").asText(), n.path("quantidade").asText("0"), 
                            n.path("melhorLance").asText("-"), n.path("valorReferencia").asText("-"), 
                            n.path("situacao").path("codigo").asText(), n.path("situacao").path("descricao").asText(), ""));
                }
            }
        }catch(Exception e){}
        return l;
    }

    // ==========================================================================
    // HELPER SELENIUM
    // ==========================================================================
    private File baixarArquivoPorLogica(WebDriver driver, File dir, String tipo) {
        try {
            List<WebElement> itens = driver.findElements(By.cssSelector(".lista-registros .item"));
            for(WebElement item : itens) {
                String t = item.getText().toUpperCase();
                boolean alvo = false;
                if(tipo.equals("VENCEDORES") && (t.contains("VENCEDORES") || (t.contains("ATA") && !t.contains("PARCIAL")))) alvo=true;
                if(alvo) {
                    WebElement btn = buscarBotaoNoElemento(item);
                    if(btn!=null) return baixarClickAndWait(driver, btn, dir);
                }
            }
        } catch(Exception e){}
        return null;
    }

    private File baixarClickAndWait(WebDriver d, WebElement b, File dir) {
        try {
            ((org.openqa.selenium.JavascriptExecutor)d).executeScript("arguments[0].click();", b);
            long end = System.currentTimeMillis()+30000; 
            while(System.currentTimeMillis() < end) {
                File[] fs = dir.listFiles();
                if(fs!=null) for(File f:fs) if(f.getName().toLowerCase().endsWith(".pdf") && !f.getName().endsWith(".crdownload")) { Thread.sleep(500); return f; }
                Thread.sleep(500);
            }
        } catch(Exception e){}
        return null;
    }
    private WebElement buscarBotaoNoElemento(WebElement e) {
        try { return e.findElement(By.cssSelector("a.btn-default")); } 
        catch(Exception x) { try{return e.findElement(By.tagName("a"));}catch(Exception y){return null;} }
    }

    // ==========================================================================
    // PDF PARSER + SALVAR EMPRESAS + CATALOGO
    // ==========================================================================
    private Map<Integer, String> mapearVencedoresPdf(File f) {
        Map<Integer, String> map = new HashMap<>();
        try(PDDocument d = PDDocument.load(f)){
            String txt = new PDFTextStripper().getText(d);
            String[] lines = txt.split("\\r?\\n");
            String empAtual = null;
            Pattern pEmp = Pattern.compile("^(.*?)\\s*(?:\\|.*?Documento|CNPJ:?|CPF:?)\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\s*/?\\s*\\d{4}\\s*-\\s*\\d{2})", Pattern.CASE_INSENSITIVE);
            Pattern pItem = Pattern.compile("^\\s*(\\d+)\\s+ITEM", Pattern.CASE_INSENSITIVE);
            
            for(String l : lines){
                Matcher me = pEmp.matcher(l.trim());
                if(me.find()){
                    empAtual = me.group(1).split("Página")[0].replaceAll("^(Vencedor:|Fornecedor:|Empresa:)","").trim();
                    salvarEmpresaSeNaoExistir(empAtual, me.group(2).replace(" ", ""));
                    continue;
                }
                if(empAtual!=null){
                    Matcher mi = pItem.matcher(l.trim());
                    if(mi.find()) try{map.put(Integer.parseInt(mi.group(1)), empAtual);}catch(Exception e){}
                }
            }
        } catch(Exception e){}
        return map;
    }

    private void salvarEmpresaSeNaoExistir(String n, String c) {
        try {
            if(!empresaRepository.existsByCnpj(c) && !empresaRepository.existsByNomeIgnoreCase(n)){
                Empresa e = new Empresa(); e.setNome(n.length()>255?n.substring(0,255):n); e.setCnpj(c);
                empresaRepository.save(e);
            }
        } catch(Exception e){}
    }

    private void atualizarCatalogoProdutos(List<ScrapedItem> itens) {
        for(ScrapedItem i : itens) {
            try {
                String nm = i.getDescricao().trim();
                if(produtoRepository.findByNomeIgnoreCase(nm).isEmpty()) {
                    Produto p = new Produto(); p.setNome(nm); p.setCodigo("");
                    produtoRepository.save(p);
                }
            }catch(Exception e){}
        }
    }

    private void atualizarPregaoNoBanco(String pid, String numP, String numProc, String obj) {
        try {
            Optional<Pregao> o = pregaoRepository.findByPortalId(pid);
            if(o.isPresent()) {
                Pregao p = o.get();
                if(numP!=null) p.setNome("Pregão "+numP);
                if(numProc!=null) p.setProcesso(numProc);
                if(obj!=null) p.setDescricao(obj);
                pregaoRepository.save(p);
            }
        } catch(Exception e){}
    }

    // UTILS
    private String extrairIdLicitacao(String u) {if(u==null)return null; String c=u.split("\\?")[0]; if(c.endsWith("/"))c=c.substring(0, c.length()-1); Matcher m=Pattern.compile("(\\d+)$").matcher(c); return m.find()?m.group(1):null;}
    private SSLSocketFactory criarSocketFactorySemSSL() {try{TrustManager[] t={new X509TrustManager(){public void checkClientTrusted(X509Certificate[] c,String a){}public void checkServerTrusted(X509Certificate[] c,String a){}public X509Certificate[] getAcceptedIssuers(){return null;}}};SSLContext s=SSLContext.getInstance("SSL");s.init(null,t,new SecureRandom());return s.getSocketFactory();}catch(Exception e){throw new RuntimeException(e);}}
    public byte[] gerarCsv(List<ScrapedItem> l){ try(ByteArrayOutputStream o=new ByteArrayOutputStream(); PrintWriter w=new PrintWriter(new OutputStreamWriter(o,StandardCharsets.UTF_8))){w.write(0xEF);w.write(0xBB);w.write(0xBF);w.println("Item;Cód;Produto;Empresa;Situação");for(ScrapedItem i:l) w.printf("%s;%s;%s;%s;%s%n",escaparCsv(i.getNumero()),escaparCsv(i.getCodigoBanco()),escaparCsv(i.getDescricao()),escaparCsv(i.getEmpresa()),escaparCsv(i.getDescricaoSituacao())); w.flush();return o.toByteArray();}catch(Exception e){return new byte[0];} }
    private String escaparCsv(String v){return v==null?"":"\""+v.replace("\"","\"\"").replace("\n"," ").trim()+"\"";}
}