package com.example.seath.controller;

import com.example.seath.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ImportController {

    @Autowired
    private ImportService importService;

    @GetMapping("/importar")
    public String paginaImportar() {
        return "importar";
    }

    @PostMapping("/importar")
    public String uploadArquivo(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, selecione um arquivo para importar.");
            return "redirect:/importar";
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String nomePregao = "";
            String numeroPregao = "";

            if (originalFilename != null) {
                // Lógica para extrair o Nome e o Número do Pregão do nome do arquivo
                // Exemplo: "Algodão - PE 029.xlsx"
                
                // 1. Encontra o separador " - PE "
                int separadorIndex = originalFilename.indexOf(" - PE ");
                if (separadorIndex != -1) {
                    // O nome é tudo o que vem antes do separador
                    nomePregao = originalFilename.substring(0, separadorIndex).trim();

                    // Pega a string que vem depois do separador
                    String parteNumerica = originalFilename.substring(separadorIndex + " - PE ".length());

                    // ### LÓGICA CORRIGIDA AQUI ###
                    // 2. Usa Regex para encontrar a primeira sequência de um ou mais dígitos
                    Pattern pattern = Pattern.compile("(\\d+)");
                    Matcher matcher = pattern.matcher(parteNumerica);

                    if (matcher.find()) {
                        // Pega o primeiro grupo de números encontrado (ex: "029")
                        numeroPregao = matcher.group(1).trim();
                    }
                }
            }

            // Passa os dados extraídos para o serviço
            importService.importarPlanilha(file.getInputStream(), nomePregao, numeroPregao);
            
            redirectAttributes.addFlashAttribute("success", "Planilha importada com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falha ao importar a planilha: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/";
    }
}