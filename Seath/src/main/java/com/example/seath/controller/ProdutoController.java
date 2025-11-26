package com.example.seath.controller;

import com.example.seath.dto.ScrapedItem;
import com.example.seath.model.Produto;
import com.example.seath.repository.ProdutoRepository;
import com.example.seath.service.CodigoPdfService;
import com.example.seath.service.WebScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProdutoController {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private WebScraperService webScraperService;
    @Autowired private CodigoPdfService codigoPdfService;

    @GetMapping("/view/produtos")
    public String listarProdutos(Model model) {
        model.addAttribute("listaProdutos", produtoRepository.findAll());
        return "fragments/conteudo_importar_produtos :: conteudoImportarProdutos";
    }

    @PostMapping("/produtos/importar")
    public String importarViaUrl(@RequestParam("url") String url, Model model) {
        try {
            List<ScrapedItem> itensExtraidos = webScraperService.extrairHibrido(url);

            if (itensExtraidos.isEmpty()) {
                model.addAttribute("error", "Nenhum item encontrado.");
            } else {
                int count = 0;
                int duplicadosIgnorados = 0;
                
                // Cache de nomes existentes para evitar consulta ao banco dentro do loop
                Set<String> nomesNoBanco = produtoRepository.findAll().stream()
                        .map(p -> p.getNome().trim().toLowerCase())
                        .collect(Collectors.toSet());

                for (ScrapedItem item : itensExtraidos) {
                    String nomeNormalizado = item.getDescricao().trim().toLowerCase();

                    if (nomesNoBanco.contains(nomeNormalizado)) {
                        duplicadosIgnorados++;
                        continue;
                    }

                    Produto novoProduto = new Produto();
                    novoProduto.setNome(item.getDescricao().trim()); // Salva bonito, mas compara normalizado
                    novoProduto.setCodigo(""); 
                    produtoRepository.save(novoProduto);
                    
                    nomesNoBanco.add(nomeNormalizado); // Adiciona ao set para o próximo loop
                    count++;
                }
                model.addAttribute("success", count + " produtos salvos. " + duplicadosIgnorados + " ignorados (já existentes).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro: " + e.getMessage());
        }
        model.addAttribute("listaProdutos", produtoRepository.findAll());
        return "fragments/conteudo_importar_produtos :: conteudoImportarProdutos";
    }

    @PostMapping("/produtos/vincular-codigos")
    public String uploadPdfCodigos(@RequestParam("fileCodigos") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Selecione um arquivo PDF.");
        } else {
            try {
                String resultado = codigoPdfService.processarPdfDeCodigos(file.getInputStream());
                model.addAttribute(resultado.contains("0 produtos") ? "error" : "success", resultado);
            } catch (Exception e) {
                model.addAttribute("error", "Erro ao processar PDF: " + e.getMessage());
            }
        }
        model.addAttribute("listaProdutos", produtoRepository.findAll());
        return "fragments/conteudo_importar_produtos :: conteudoImportarProdutos";
    }

    // --- NOVO MÉTODO PARA LIMPAR DUPLICATAS ---
    @PostMapping("/produtos/limpar-duplicados")
    public String limparDuplicatas(Model model) {
        try {
            List<Produto> todos = produtoRepository.findAll();
            Map<String, List<Produto>> agrupados = new HashMap<>();

            // 1. Agrupar por nome normalizado (lowercase + trim)
            for (Produto p : todos) {
                String chave = p.getNome().trim().toLowerCase();
                agrupados.computeIfAbsent(chave, k -> new ArrayList<>()).add(p);
            }

            int removidos = 0;

            // 2. Analisar grupos com mais de 1 item
            for (List<Produto> grupo : agrupados.values()) {
                if (grupo.size() > 1) {
                    // Ordena para priorizar: 1º Quem tem código, 2º Menor ID
                    grupo.sort((p1, p2) -> {
                        boolean p1TemCod = p1.getCodigo() != null && !p1.getCodigo().isEmpty();
                        boolean p2TemCod = p2.getCodigo() != null && !p2.getCodigo().isEmpty();
                        
                        if (p1TemCod && !p2TemCod) return -1; // p1 vem primeiro
                        if (!p1TemCod && p2TemCod) return 1;  // p2 vem primeiro
                        return p1.getId().compareTo(p2.getId()); // desempate por ID (mais antigo fica)
                    });

                    // Remove todos exceto o primeiro (o "Vencedor")
                    for (int i = 1; i < grupo.size(); i++) {
                        produtoRepository.delete(grupo.get(i));
                        removidos++;
                    }
                }
            }
            
            if (removidos > 0) {
                model.addAttribute("success", "Limpeza concluída: " + removidos + " duplicatas foram removidas.");
            } else {
                model.addAttribute("success", "O banco de dados já está limpo. Nenhuma duplicata encontrada.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erro ao limpar duplicatas: " + e.getMessage());
        }

        model.addAttribute("listaProdutos", produtoRepository.findAll());
        return "fragments/conteudo_importar_produtos :: conteudoImportarProdutos";
    }
}