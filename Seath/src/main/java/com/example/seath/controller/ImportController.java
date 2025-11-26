package com.example.seath.controller;

import com.example.seath.repository.VencedorRepository;
import com.example.seath.service.PdfImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ImportController {

    @Autowired private PdfImportService pdfImportService;
    @Autowired private VencedorRepository vencedorRepository;

    // Carrega o fragmento HTML (Tabela + Botão)
    @GetMapping("/view/importar")
    public String getPaginaImportar(Model model) {
        model.addAttribute("listaVencedores", vencedorRepository.findAll());
        return "fragments/conteudo_importar :: conteudoImportar";
    }

    // Processa o Upload
    @PostMapping("/importar/pdf")
    public String uploadPdf(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Selecione um arquivo PDF.");
        } else {
            try {
                pdfImportService.importarPdfVencedores(file.getInputStream());
                model.addAttribute("success", "PDF processado com sucesso! Dados extraídos.");
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Erro ao processar PDF: " + e.getMessage());
            }
        }
        
        // Retorna a tabela atualizada
        model.addAttribute("listaVencedores", vencedorRepository.findAll());
        return "fragments/conteudo_importar :: conteudoImportar";
    }
}