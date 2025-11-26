package com.example.seath.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EditalParserService {

    public Map<String, String> extrairDadosEdital(File arquivoEdital) {
        Map<String, String> dados = new HashMap<>();
        
        try (PDDocument doc = PDDocument.load(arquivoEdital)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            // Lê apenas o início (geralmente primeira pagina tem o preambulo)
            stripper.setEndPage(10);
            
            String texto = stripper.getText(doc);
            String[] linhas = texto.split("\\r?\\n");
            
            // --- REGEXES INTELIGENTES ---
            
            // Busca "Pregão Eletrônico 227/2025"
            Pattern pPregaoHeader = Pattern.compile("(?i)(PREGÃO|CONCORRÊNCIA|REGISTRO DE PREÇOS).*?ELETRÔNICO");
            Pattern pNumeroFormato = Pattern.compile("(\\d{1,6}\\s*/\\s*\\d{4})");

            // Busca "Processo nº"
            Pattern pProcesso = Pattern.compile("(?i)Processo\\s*n[º°o]?\\s*([0-9./-]+)");

            // Busca "Objeto"
            Pattern pObjetoInicio = Pattern.compile("(?i)^(?:I+|[0-9]+)\\.?\\s*(?:DO\\s+)?OBJETO");
            // Palavras que indicam o fim do bloco do Objeto
            Pattern pObjetoFim = Pattern.compile("(?i)^(?:I+|[0-9]+)\\.|VALOR|JUSTIFICATIVA|DOTAÇÃO|PARTICIPAÇÃO");

            boolean lendoObjeto = false;
            StringBuilder objetoBuilder = new StringBuilder();

            for (int i = 0; i < linhas.length; i++) {
                String linha = linhas[i].trim();
                if(linha.isEmpty()) continue;

                // 1. PROCESSO
                if (!dados.containsKey("processo")) {
                    Matcher m = pProcesso.matcher(linha);
                    if(m.find()) dados.put("processo", m.group(1).replace(" ", ""));
                }

                // 2. PREGÃO (Busca por proximidade)
                if (!dados.containsKey("pregao")) {
                    if (pPregaoHeader.matcher(linha).find()) {
                        Matcher mNum = pNumeroFormato.matcher(linha);
                        if (mNum.find()) {
                            dados.put("pregao", mNum.group(1).replace(" ", ""));
                        } else {
                            // Olha as próximas 3 linhas
                            for(int k=1; k<=3 && (i+k)<linhas.length; k++){
                                String prox = linhas[i+k].trim();
                                Matcher mNext = pNumeroFormato.matcher(prox);
                                if(mNext.find() && prox.length() < 30) { // Evita falsos positivos em textos longos
                                    dados.put("pregao", mNext.group(1).replace(" ", ""));
                                    break;
                                }
                            }
                        }
                    } else {
                        // Tenta achar numero solto "227/2025" se estiver sozinho na linha (comum em capas)
                        if(linha.matches("^\\d+/\\d{4}$")) dados.put("pregao", linha);
                    }
                }

                // 3. OBJETO (Multilinha)
                if (pObjetoInicio.matcher(linha).find()) {
                    lendoObjeto = true;
                    String resto = linha.replaceAll("(?i)^(?:I+|[0-9]+)\\.?\\s*(?:DO\\s+)?OBJETO[:.-]*", "").trim();
                    if(!resto.isEmpty()) objetoBuilder.append(resto).append(" ");
                    continue;
                }
                if (lendoObjeto) {
                    if (pObjetoFim.matcher(linha).find()) {
                        lendoObjeto = false;
                    } else {
                        objetoBuilder.append(linha).append(" ");
                    }
                }
            }
            
            if(objetoBuilder.length() > 5) {
                String obj = objetoBuilder.toString().trim();
                if(obj.length() > 1000) obj = obj.substring(0, 1000);
                dados.put("objeto", obj);
            }

        } catch (Exception e) { System.err.println("Erro EditalParser: " + e.getMessage()); }
        
        return dados;
    }
}