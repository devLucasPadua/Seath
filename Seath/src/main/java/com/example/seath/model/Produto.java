package com.example.seath.model;

import jakarta.persistence.*;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo; // Será preenchido futuramente

    @Column(columnDefinition = "TEXT") // Textos de licitação podem ser longos
    private String nome;   // Vem da descrição do item do Scraper

    // Construtores, Getters e Setters
    public Produto() {}

    public Produto(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}