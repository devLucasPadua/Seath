package com.example.seath.repository;

import com.example.seath.model.Pregao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PregaoRepository extends JpaRepository<Pregao, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByProcessoIgnoreCase(String processo);
    boolean existsByDescricaoIgnoreCase(String descricao);
    List<Pregao> findByNomeContainingIgnoreCaseOrProcessoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String nome, String processo, String descricao);
}