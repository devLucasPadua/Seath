package com.example.seath.repository;

import com.example.seath.model.Pregao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PregaoRepository extends JpaRepository<Pregao, Long> {

    // Novos métodos para verificar a existência por campo
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByProcessoIgnoreCase(String processo);
    boolean existsByDescricaoIgnoreCase(String descricao);
    
}   