package com.example.seath.repository;

import com.example.seath.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByCnpjIgnoreCase(String cnpj);
    
    List<Empresa> findTop3ByOrderByIdDesc();
    
    List<Empresa> findByNomeContainingIgnoreCaseOrCnpjContainingIgnoreCase(String nome, String cnpj);
}