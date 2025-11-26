package com.example.seath.service;

import com.example.seath.model.Produto;
import com.example.seath.repository.ProdutoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodigoPdfService {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Lê o PDF, extrai mapa Código/Nome e atualiza produtos existentes.
     */
    @Transactional
    public String processarPdfDeCodigos(InputStream inputStream) throws IOException {
        // 1. Extrair dados do PDF
        Map<String, String> mapaDescricaoCodigo = lerPdfMapearCodigos(inputStream);
        
        if (mapaDescricaoCodigo.isEmpty()) {
            return "Nenhum código identificado no PDF. Verifique o formato.";
        }

        int atualizados = 0;
        int ignorados = 0; // Já tinham código
        int errosDuplicidade = 0;

        // 2. Buscar produtos no banco
        List<Produto> todosProdutos = produtoRepository.findAll();

        for (Produto prod : todosProdutos) {
            // Só atualiza se estiver sem código (ou se quiser forçar atualização, remova o if)
            if (prod.getCodigo() != null && !prod.getCodigo().isEmpty()) {
                ignorados++;
                continue;
            }

            // Normaliza o nome do banco para comparar com o do PDF (ignora acentos/espaços/case)
            String nomeBancoNormalizado = normalizarTexto(prod.getNome());
            
            // Tenta encontrar match exato no mapa extraído do PDF
            if (mapaDescricaoCodigo.containsKey(nomeBancoNormalizado)) {
                String novoCodigo = mapaDescricaoCodigo.get(nomeBancoNormalizado);

                // VALIDAÇÃO DE UNICIDADE (REGRA DO NEGÓCIO)
                // Verifica se já existe OUTRO produto com esse código
                Optional<Produto> existenteComCodigo = produtoRepository.findByCodigo(novoCodigo);
                
                if (existenteComCodigo.isPresent()) {
                    // Já existe um produto com esse código!
                    // Se for o mesmo ID, ok. Se for outro, temos uma duplicidade.
                    if (!existenteComCodigo.get().getId().equals(prod.getId())) {
                        System.out.println("Conflito: O código " + novoCodigo + " pertence ao ID " 
                                + existenteComCodigo.get().getId() + " e tentamos aplicar ao ID " + prod.getId());
                        errosDuplicidade++;
                        continue; 
                    }
                }

                // Aplica o código
                prod.setCodigo(novoCodigo);
                produtoRepository.save(prod);
                atualizados++;
            }
        }

        return String.format("Processamento Concluído: %d produtos atualizados. %d ignorados (já tinham código). %d conflitos de unicidade evitados.", 
                atualizados, ignorados, errosDuplicidade);
    }

    private Map<String, String> lerPdfMapearCodigos(InputStream is) throws IOException {
        Map<String, String> mapa = new HashMap<>();
        
        try (PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            String[] linhas = text.split("\\r?\\n");

            // REGEX BASEADA NA IMAGEM DO PDF
            // Padrão: ITEM(números) ESPAÇO CÓDIGO(números) ESPAÇO DESCRIÇÃO(...) ESPAÇO UNIDADE(sigla) ...
            // Ex: "1 0361259 BOBINAS ... VIRGEM UN 26.000"
            
            // Regex explicada:
            // ^\s*\d+\s+       -> Início, item numérico (ex: "1 ")
            // (\d+)            -> GRUPO 1: CÓDIGO (ex: "0361259")
            // \s+              -> Espaço
            // (.+?)            -> GRUPO 2: DESCRIÇÃO (Pega tudo até encontrar a unidade)
            // \s+(?:UN|UND|PCT|RL|RLO|KG|BL|FR|AMP|CX|LIT|M)\b -> Lookahead para unidades comuns médicas
            
            String regexStr = "^\\s*\\d+\\s+(\\d+)\\s+(.+?)\\s+(?:UN|UND|PCT|RL|RLO|KG|BL|FR|AMP|CX|LIT|M|GL|AP)\\b";
            Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);

            for (String linha : linhas) {
                Matcher matcher = pattern.matcher(linha.trim());
                if (matcher.find()) {
                    String codigo = matcher.group(1).trim();
                    String descricao = matcher.group(2).trim();
                    
                    // Salva no mapa com a chave normalizada (para bater com o banco que pode ter espaços/acentos diferentes)
                    mapa.put(normalizarTexto(descricao), codigo);
                }
            }
        }
        return mapa;
    }

    // Função auxiliar para facilitar o "Match" entre PDF e Site
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        // Remove acentos
        String norm = Normalizer.normalize(texto, Normalizer.Form.NFD);
        norm = norm.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Remove caracteres não alfanuméricos, passa para minúsculas e trim
        return norm.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}