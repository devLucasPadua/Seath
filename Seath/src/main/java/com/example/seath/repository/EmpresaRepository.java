package com.example.seath.repository;

import com.example.seath.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    // Métodos de validação
    boolean existsByNomeIgnoreCase(String nome);
    
    // CORREÇÃO: Adicionando o método exato que o Service está chamando
    boolean existsByCnpj(String cnpj);
    
    // Este outro método pode ser mantido se usado em outras partes
    boolean existsByCnpjIgnoreCase(String cnpj);
    
    // Métodos de busca para a interface (autocomplete)
    List<Empresa> findTop3ByOrderByIdDesc();
    
    List<Empresa> findByNomeContainingIgnoreCaseOrCnpjContainingIgnoreCase(String nome, String cnpj);
}