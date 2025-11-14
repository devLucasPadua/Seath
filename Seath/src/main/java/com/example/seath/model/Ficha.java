package com.example.seath.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import jakarta.persistence.Column;

import com.example.seath.model.converter.LocalDateConverter;
import jakarta.persistence.Convert;

@Entity
public class Ficha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pregao;
    private String processo;
    private String item;
    private String licitante;
    private String infoProduto;
    private String marca;
    private String material;
    private String modelo;
    private String tamanho;
    private String lote;
    
    @Convert(converter = LocalDateConverter.class)
    private LocalDate fabricacao;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate validade;
    
    private String referencia;
    private Integer quantidade;
    private String formaAvaliacao;
    private String enviadoPara;
    private String responsavel;
    
    @Convert(converter = LocalDateConverter.class)
    private LocalDate dataEnvio;

// Getters e Setters 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPregao() {
        return pregao;
    }

    public void setPregao(String pregao) {
        this.pregao = pregao;
    }

    public String getProcesso() {
        return processo;
    }

    public void setProcesso(String processo) {
        this.processo = processo;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getLicitante() {
        return licitante;
    }

    public void setLicitante(String licitante) {
        this.licitante = licitante;
    }

    public String getInfoProduto() {
        return infoProduto;
    }

    public void setInfoProduto(String infoProduto) {
        this.infoProduto = infoProduto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDate getFabricacao() {
        return fabricacao;
    }

    public void setFabricacao(LocalDate fabricacao) {
        this.fabricacao = fabricacao;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getFormaAvaliacao() {
        return formaAvaliacao;
    }

    public void setFormaAvaliacao(String formaAvaliacao) {
        this.formaAvaliacao = formaAvaliacao;
    }

    public String getEnviadoPara() {
        return enviadoPara;
    }

    public void setEnviadoPara(String enviadoPara) {
        this.enviadoPara = enviadoPara;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
