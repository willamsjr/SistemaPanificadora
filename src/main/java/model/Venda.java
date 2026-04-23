package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Venda {

    private Integer id;
    private LocalDateTime data;
    private BigDecimal valorTotal;
    private Integer idFuncionario;
    private Integer idCliente;

    private String nomeFuncionario;
    private String nomeCliente;

    // 🔹 CAMPO QUE FALTAVA
    private String formaPagamento;

    // novos campos para exibir no relatório resumo
    private String nomeProduto;      // nome do (primeiro) produto vendido / resumo
    private Integer quantidadeTotal; // soma das quantidades vendidas na venda

    private List<Item_venda> itens;

    public Venda() {}

    // ============================================================
    // GETTERS E SETTERS
    // ============================================================

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public Integer getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(Integer idFuncionario) { this.idFuncionario = idFuncionario; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public List<Item_venda> getItens() { return itens; }
    public void setItens(List<Item_venda> itens) { this.itens = itens; }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String nomeFuncionario) { this.nomeFuncionario = nomeFuncionario; }

    public String getNomeCliente() {
        if (nomeCliente == null || nomeCliente.isEmpty()) {
            return "Venda Anônima";
        }
        return nomeCliente;
    }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    // 🔹 getters e setters novos para relatório
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public Integer getQuantidadeTotal() { return quantidadeTotal == null ? 0 : quantidadeTotal; }
    public void setQuantidadeTotal(Integer quantidadeTotal) { this.quantidadeTotal = quantidadeTotal; }

    // ============================================================
    // 🔹 GETTERS E SETTERS DA FORMA DE PAGAMENTO (ADICIONADOS)
    // ============================================================
    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
