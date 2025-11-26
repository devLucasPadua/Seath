package com.example.seath.repository;

import com.example.seath.model.Pregao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PregaoRepository extends JpaRepository<Pregao, Long> {

    // --- MÉTODOS NOVOS (Gerenciador de Pregão / Scraper) ---
    
    // Busca pelo ID da URL (ex: 424766) para evitar duplicação na extração
    Optional<Pregao> findByPortalId(String portalId);
    
    // Lista todos para o Dropdown, ordenados do mais recente para o antigo
    List<Pregao> findAllByOrderByIdDesc();


    // --- MÉTODOS LEGADOS (Usados no FichaController / Tela de Cadastro) ---

    // Recupera os 3 últimos cadastrados para o painel lateral
    List<Pregao> findTop3ByOrderByIdDesc();

    // Validações de existência
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByProcessoIgnoreCase(String processo);
    boolean existsByDescricaoIgnoreCase(String descricao);

    // Busca na barra lateral da tela de Cadastro (Campo de pesquisa)
    List<Pregao> findByNomeContainingIgnoreCaseOrProcessoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(
            String nome, String processo, String descricao);
}