package com.example.seath.controller;

import com.example.seath.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ImportController {

    @Autowired
    private ImportService importService;

    @GetMapping("/importar")
    public String paginaImportar() {
        return "importar"; // Nome da nova página HTML
    }

    @PostMapping("/importar")
    public String uploadArquivo(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, selecione um arquivo para importar.");
            return "redirect:/importar";
        }

        try {
            importService.importarPlanilha(file.getInputStream());
            redirectAttributes.addFlashAttribute("success", "Planilha importada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falha ao importar a planilha: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/"; // Redireciona para a página principal após a importação
    }
}