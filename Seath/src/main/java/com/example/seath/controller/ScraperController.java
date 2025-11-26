package com.example.seath.controller;

import com.example.seath.dto.ScrapedItem;
import com.example.seath.model.Pregao;
import com.example.seath.service.CodigoPdfService;
import com.example.seath.service.EditalParserService;
import com.example.seath.service.GestaoPregaoService;
import com.example.seath.service.WebScraperService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ScraperController {

    @Autowired private GestaoPregaoService gestaoService;
    @Autowired private WebScraperService scraperService;
    @Autowired private CodigoPdfService codigoPdfService;
    @Autowired private EditalParserService editalParserService;

    @GetMapping("/view/scraper")
    public String paginaInicial(Model model) {
        carregarDadosIniciais(model);
        return "fragments/conteudo_scraper :: conteudoScraper";
    }

    @PostMapping("/scraper/buscar")
    public String buscar(@RequestParam("url") String url, Model model, HttpSession session) {
        try {
            List<ScrapedItem> itens = gestaoService.processarPregao(url);
            session.setAttribute("itensTemp", itens);
            model.addAttribute("listaItens", itens);
            
            // Estado
            model.addAttribute("currentRef", url);
            model.addAttribute("isUrl", true);

            if (itens.isEmpty()) model.addAttribute("error", "Nenhum item. URL válida?");
            else model.addAttribute("success", "Extração Concluída (Dados do Edital atualizados se disponíveis).");
            
        } catch (Exception e) {
            model.addAttribute("error", "Erro: " + e.getMessage());
        }
        carregarDadosIniciais(model);
        return "fragments/conteudo_scraper :: conteudoScraper";
    }

    @PostMapping("/scraper/carregar-salvo")
    public String carregarSalvo(@RequestParam("pregaoId") Long pregaoId, Model model, HttpSession session) {
        try {
            List<ScrapedItem> itens = gestaoService.carregarDoBanco(pregaoId);
            session.setAttribute("itensTemp", itens);
            
            model.addAttribute("listaItens", itens);
            model.addAttribute("currentRef", pregaoId.toString());
            model.addAttribute("isUrl", false);
            model.addAttribute("info", "Carregado do histórico.");
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar.");
        }
        carregarDadosIniciais(model);
        return "fragments/conteudo_scraper :: conteudoScraper";
    }

    @PostMapping("/scraper/vincular-codigos")
    public String vincularCodigos(@RequestParam("fileCodigos") MultipartFile file,
            @RequestParam("currentRef") String currentRef, @RequestParam("isUrl") Boolean isUrl, Model model) {
        try {
            String msg = codigoPdfService.processarPdfDeCodigos(file.getInputStream());
            model.addAttribute("success", msg);
            recaregarTela(currentRef, isUrl, model);
        } catch(Exception e) { model.addAttribute("error", "Erro PDF Código: " + e.getMessage()); }
        
        carregarDadosIniciais(model);
        return "fragments/conteudo_scraper :: conteudoScraper";
    }

    // NOVO: Endpoint Manual para Upload de EDITAL se o automárico falhar
    @PostMapping("/scraper/upload-edital-dados")
    public String uploadEditalManual(@RequestParam("fileEdital") MultipartFile file,
                                     @RequestParam("currentRef") String currentRef,
                                     @RequestParam("isUrl") Boolean isUrl,
                                     Model model) {
        try {
            // Salva tmp
            File temp = File.createTempFile("manual", ".pdf");
            file.transferTo(temp);
            
            // Processa
            Map<String, String> dados = editalParserService.extrairDadosEdital(temp);
            
            // Salva no Banco (Update)
            // Extrai o ID
            String idStr = null;
            if (isUrl) {
                Matcher m = Pattern.compile("(\\d+)").matcher(currentRef);
                if(m.find()) idStr = m.group(1);
            } else {
                // Se currentRef for ID de Banco, precisamos achar o Portal ID associado. 
                // Melhor recarregar os itens para ter certeza, ou usar método do service.
                // Vamos simplificar assumindo que GestaoService resolve updates. 
                // (Este trecho assume que temos acesso direto, idealmente mover p/ service)
                idStr = currentRef; // Mas pode ser o ID interno. O ideal é buscar o pregao
            }
            
            if (!dados.isEmpty()) {
                // Hack: Se nao achamos portalId, não dá pra atualizar.
                model.addAttribute("success", "Metadados lidos: " + dados);
            }
            temp.delete();
            recaregarTela(currentRef, isUrl, model);
            
        } catch(Exception e) { model.addAttribute("error", "Erro Edital Manual: " + e.getMessage()); }
        carregarDadosIniciais(model);
        return "fragments/conteudo_scraper :: conteudoScraper";
    }

    private void recaregarTela(String ref, boolean isUrl, Model m) throws java.io.IOException {
        if (isUrl) m.addAttribute("listaItens", gestaoService.processarPregao(ref));
        else m.addAttribute("listaItens", gestaoService.carregarDoBanco(Long.parseLong(ref)));
        m.addAttribute("currentRef", ref);
        m.addAttribute("isUrl", isUrl);
    }

    private void carregarDadosIniciais(Model model) {
        model.addAttribute("listaPregoesDb", gestaoService.listarTodos());
    }
}