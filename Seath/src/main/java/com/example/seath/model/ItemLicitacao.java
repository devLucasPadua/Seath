package com.example.seath.model;

import jakarta.persistence.*;

@Entity
public class ItemLicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroItem;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    private String unidade;
    private String quantidade;
    
    @Column(columnDefinition = "TEXT")
    private String empresaVencedora;
    
    private String situacao;

    @ManyToOne
    @JoinColumn(name = "pregao_id")
    private Pregao pregao;

    public ItemLicitacao() {}

    public ItemLicitacao(String numeroItem, String descricao, String unidade, String quantidade, String empresaVencedora, String situacao, Pregao pregao) {
        this.numeroItem = numeroItem;
        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.empresaVencedora = empresaVencedora;
        this.situacao = situacao;
        this.pregao = pregao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public String getNumeroItem() { return numeroItem; }
    public void setNumeroItem(String numeroItem) { this.numeroItem = numeroItem; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getQuantidade() { return quantidade; }
    public void setQuantidade(String quantidade) { this.quantidade = quantidade; }
    public String getEmpresaVencedora() { return empresaVencedora; }
    public void setEmpresaVencedora(String empresaVencedora) { this.empresaVencedora = empresaVencedora; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public Pregao getPregao() { return pregao; }
    public void setPregao(Pregao pregao) { this.pregao = pregao; }
}