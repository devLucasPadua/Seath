package com.example.seath.dto;

import java.io.Serializable;

public class ScrapedItem implements Serializable {
    private String numero;          
    private String descricao;
    // Removemos getters/setters de Unidade/Qtd da view, mas podemos manter no objeto por segurança
    private String unidade;
    private String quantidade;
    private String melhorLance;
    private String valorReferencia;
    private String codigoSituacao;
    private String descricaoSituacao;
    private String empresa;         
    
    // NOVO CAMPO: Código recuperado do banco "Produtos"
    private String codigoBanco;     

    public ScrapedItem() {}

    public ScrapedItem(String numero, String descricao, String unidade, String quantidade, 
                       String melhorLance, String valorReferencia, String codigoSituacao, 
                       String descricaoSituacao, String empresa) {
        this.numero = numero;
        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.melhorLance = melhorLance;
        this.valorReferencia = valorReferencia;
        this.codigoSituacao = codigoSituacao;
        this.descricaoSituacao = descricaoSituacao;
        this.empresa = empresa;
    }

    // Getters e Setters
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getQuantidade() { return quantidade; }
    public void setQuantidade(String quantidade) { this.quantidade = quantidade; }
    public String getMelhorLance() { return melhorLance; }
    public void setMelhorLance(String melhorLance) { this.melhorLance = melhorLance; }
    public String getValorReferencia() { return valorReferencia; }
    public void setValorReferencia(String valorReferencia) { this.valorReferencia = valorReferencia; }
    public String getCodigoSituacao() { return codigoSituacao; }
    public void setCodigoSituacao(String codigoSituacao) { this.codigoSituacao = codigoSituacao; }
    public String getDescricaoSituacao() { return descricaoSituacao; }
    public void setDescricaoSituacao(String descricaoSituacao) { this.descricaoSituacao = descricaoSituacao; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    
    // Novo Getter/Setter
    public String getCodigoBanco() { return codigoBanco; }
    public void setCodigoBanco(String codigoBanco) { this.codigoBanco = codigoBanco; }
}