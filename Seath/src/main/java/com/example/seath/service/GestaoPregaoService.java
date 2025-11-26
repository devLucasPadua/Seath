package com.example.seath.service;

import com.example.seath.dto.ScrapedItem;
import com.example.seath.model.ItemLicitacao;
import com.example.seath.model.Pregao;
import com.example.seath.model.Produto; // Importação nova
import com.example.seath.repository.PregaoRepository;
import com.example.seath.repository.ProdutoRepository; // Importação nova
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GestaoPregaoService {

    @Autowired private PregaoRepository pregaoRepository;
    @Autowired private ProdutoRepository produtoRepository; 
    @Autowired private WebScraperService scraperService;

    public List<Pregao> listarTodos() {
        return pregaoRepository.findAllByOrderByIdDesc();
    }

    @Transactional
    public List<ScrapedItem> processarPregao(String urlOuId) throws IOException {
        String portalId = extrairId(urlOuId);
        if (portalId == null) throw new IOException("Não foi possível identificar o ID do pregão.");

        Optional<Pregao> existente = pregaoRepository.findByPortalId(portalId);
        if (existente.isPresent()) {
            return enriquecerComCodigos(converterEntidadeParaDto(existente.get()));
        }

        if (!urlOuId.startsWith("http")) throw new IOException("Para novos pregões, forneça a URL completa.");
        
        List<ScrapedItem> itensExtraidos = scraperService.extrairHibrido(urlOuId);

        if (!itensExtraidos.isEmpty()) {
            // 1. Salvar estrutura do Pregão
            salvarNovoPregao(portalId, itensExtraidos);
            
            // 2. AUTOMATIZAÇÃO: Salvar novos produtos no catálogo global
            atualizarCatalogoProdutos(itensExtraidos);
        }

        return enriquecerComCodigos(itensExtraidos);
    }

    @Transactional
    public List<ScrapedItem> carregarDoBanco(Long idInterno) {
        Pregao pregao = pregaoRepository.findById(idInterno)
                .orElseThrow(() -> new RuntimeException("Pregão não encontrado."));
        return enriquecerComCodigos(converterEntidadeParaDto(pregao));
    }

    /**
     * Salva automaticamente no Gerenciar Produtos se o item ainda não existir lá.
     */
    private void atualizarCatalogoProdutos(List<ScrapedItem> itens) {
        int novos = 0;
        for (ScrapedItem item : itens) {
            // Verifica duplicidade pelo nome (igual fazemos no ProdutoController)
            // Remove espaços e ignora case
            String nome = item.getDescricao().trim();
            
            // Se não existir um produto com esse nome (ignorando case)
            if (produtoRepository.findByNomeIgnoreCase(nome).isEmpty()) {
                Produto prod = new Produto();
                prod.setNome(nome);
                prod.setCodigo(""); // Sem código inicialmente
                produtoRepository.save(prod);
                novos++;
            }
        }
        if (novos > 0) System.out.println(">>> [SEATH] Catálogo atualizado: " + novos + " novos produtos inseridos.");
    }

    private List<ScrapedItem> enriquecerComCodigos(List<ScrapedItem> itensDoPregao) {
        List<Produto> produtosCadastrados = produtoRepository.findAll();
        Map<String, String> mapaProdutos = new HashMap<>();
        for (Produto p : produtosCadastrados) {
            if (p.getCodigo() != null && !p.getCodigo().isEmpty()) {
                mapaProdutos.put(normalizar(p.getNome()), p.getCodigo());
            }
        }

        for (ScrapedItem item : itensDoPregao) {
            String descNormalizada = normalizar(item.getDescricao());
            if (mapaProdutos.containsKey(descNormalizada)) {
                item.setCodigoBanco(mapaProdutos.get(descNormalizada));
            } else {
                item.setCodigoBanco(null); 
            }
        }
        return itensDoPregao;
    }

    private void salvarNovoPregao(String portalId, List<ScrapedItem> itensDto) {
        Pregao pregao = new Pregao();
        pregao.setPortalId(portalId);
        pregao.setNome("Pregão " + portalId);
        pregao.setDescricao("Importado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        List<ItemLicitacao> itensDb = new ArrayList<>();
        for (ScrapedItem dto : itensDto) {
            itensDb.add(new ItemLicitacao(
                    dto.getNumero(), dto.getDescricao(), dto.getUnidade(),
                    dto.getQuantidade(), dto.getEmpresa(), dto.getDescricaoSituacao(), pregao
            ));
        }
        pregao.setItens(itensDb);
        pregaoRepository.save(pregao);
    }

    private List<ScrapedItem> converterEntidadeParaDto(Pregao p) {
        return p.getItens().stream().map(i -> {
            ScrapedItem dto = new ScrapedItem();
            dto.setNumero(i.getNumeroItem());
            dto.setDescricao(i.getDescricao());
            dto.setUnidade(i.getUnidade());
            dto.setQuantidade(i.getQuantidade());
            dto.setEmpresa(i.getEmpresaVencedora());
            dto.setDescricaoSituacao(i.getSituacao());
            dto.setMelhorLance("-"); dto.setValorReferencia("-");
            return dto;
        }).collect(Collectors.toList());
    }

    private String extrairId(String input) {
        Matcher m = Pattern.compile("(\\d+)").matcher(input);
        String lastMatch = null;
        while(m.find()) lastMatch = m.group();
        return lastMatch;
    }
    
    private String normalizar(String texto) {
        if (texto == null) return "";
        String n = Normalizer.normalize(texto, Normalizer.Form.NFD);
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return n.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}