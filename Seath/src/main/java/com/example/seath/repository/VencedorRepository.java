package com.example.seath.repository;

import com.example.seath.model.Vencedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VencedorRepository extends JpaRepository<Vencedor, Long> {
    boolean existsByCnpj(String cnpj);
}