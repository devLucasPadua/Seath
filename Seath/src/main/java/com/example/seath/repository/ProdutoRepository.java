package com.example.seath.repository;

import com.example.seath.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    // Busca produto pelo nome (para evitar duplicação na importação via site)
    Optional<Produto> findByNomeIgnoreCase(String nome);
    
    // Busca produto pelo código (para validação de unicidade)
    Optional<Produto> findByCodigo(String codigo);
    
    // CORREÇÃO AQUI: Removido o "Dt" errado. 
    // A lógica é: Buscar onde (Código É Nulo) OU (Código É Igual a "Parametro")
    List<Produto> findByCodigoIsNullOrCodigo(String codigoVazio);
}