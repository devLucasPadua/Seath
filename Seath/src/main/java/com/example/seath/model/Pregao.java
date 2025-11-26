package com.example.seath.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pregao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String portalId; // Ex: 424766 (ID da URL)
    
    private String nome;     // Ex: "Pregão 227/2025" (Usado no Dropdown)
    
    private String processo; // Ex: "3516200.../2025-14" (Usado na lateral 'Proc:')

    @Column(columnDefinition = "TEXT") 
    private String descricao; // Ex: "Aquisição de peças odontológicas..." (Objeto do Edital)

    @OneToMany(mappedBy = "pregao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemLicitacao> itens = new ArrayList<>();

    public Pregao() {}

    public Pregao(String portalId, String nome, String descricao) {
        this.portalId = portalId;
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPortalId() { return portalId; }
    public void setPortalId(String portalId) { this.portalId = portalId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getProcesso() { return processo; }
    public void setProcesso(String processo) { this.processo = processo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public List<ItemLicitacao> getItens() { return itens; }
    public void setItens(List<ItemLicitacao> itens) { this.itens = itens; }
}