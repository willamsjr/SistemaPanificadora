package model;

import java.math.BigDecimal;

public class Produto {

    private Integer id;
    private String nome;
    private BigDecimal preco;
    private Integer qntEstoque;
    private Integer estoqueMinimo;

    public Produto() {}

    public Produto(Integer id, String nome, BigDecimal preco, Integer qntEstoque, Integer estoqueMinimo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.qntEstoque = qntEstoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Integer getQntEstoque() {
        return qntEstoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public void setQntEstoque(Integer qntEstoque) {
        this.qntEstoque = qntEstoque;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
