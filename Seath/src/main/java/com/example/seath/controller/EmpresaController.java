package com.example.seath.controller;

import com.example.seath.model.Empresa;
import com.example.seath.repository.EmpresaRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    // Lista todas as empresas cadastradas (Fragmento HTMX)
    @GetMapping("/view/empresas")
    public String listarEmpresas(Model model) {
        model.addAttribute("listaEmpresas", empresaRepository.findAll());
        model.addAttribute("empresaForm", new Empresa()); // Para cadastro manual futuro se quiser
        return "fragments/gerenciar_empresas :: conteudoEmpresas";
    }

    // Processa o PDF para extrair APENAS empresas
    @PostMapping("/empresas/importar-lote")
    public String importarLotePdf(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Selecione um arquivo PDF.");
        } else {
            try {
                int novas = processarPdfEmpresas(file);
                if (novas > 0) {
                    model.addAttribute("success", novas + " novas empresas cadastradas com sucesso!");
                } else {
                    model.addAttribute("info", "Nenhuma empresa nova encontrada no arquivo (todas já existiam ou formato inválido).");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Erro ao processar PDF: " + e.getMessage());
            }
        }
        model.addAttribute("listaEmpresas", empresaRepository.findAll());
        return "fragments/gerenciar_empresas :: conteudoEmpresas";
    }

    // Lógica de Extração de Empresas (Isolada para o Controller)
    private int processarPdfEmpresas(MultipartFile file) throws IOException {
        int count = 0;
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Regex para pegar Nome e CNPJ
            // Aceita formatos com "Tipo:" ou direto, e tolera espaços no CNPJ
            Pattern pattern = Pattern.compile("^(.*?)\\s*(?:\\|.*?Documento|CNPJ:?|CPF:?)\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\s*/?\\s*\\d{4}\\s*-\\s*\\d{2})", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String nome = matcher.group(1).trim();
                String cnpj = matcher.group(2).replace(" ", "").trim();

                // Limpezas de nome
                if (nome.contains("Página")) nome = nome.split("Página")[0].trim();
                nome = nome.replaceAll("^(Vencedor:|Fornecedor:|Empresa:)", "").trim();

                if (!nome.isEmpty() && !cnpj.isEmpty()) {
                    if (!empresaRepository.existsByCnpj(cnpj)) {
                        Empresa nova = new Empresa();
                        nova.setNome(nome.length() > 255 ? nome.substring(0, 255) : nome);
                        nova.setCnpj(cnpj);
                        empresaRepository.save(nova);
                        count++;
                    }
                }
            }
        }
        return count;
    }
}