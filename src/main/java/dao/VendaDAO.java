package dao;

import conexao.ConexaoDB;
import model.Venda;
import model.Item_venda;
import model.Produto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class VendaDAO {

    private final Item_vendaDAO itemVendaDAO = new Item_vendaDAO();

    // REGISTRAR VENDA (usa TRIGGERS para baixar o estoque)
    public boolean registrarVenda(Venda venda) {
        if (venda == null || venda.getItens() == null || venda.getItens().isEmpty()) {
            System.err.println("RN01: Não é possível registrar uma venda sem produtos.");
            return false;
        }

        Connection conn = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConnection();
            conn.setAutoCommit(false);

            int idVenda = inserirVendaPrincipal(conn, venda);
            if (idVenda <= 0) throw new SQLException("Falha ao inserir venda principal.");

            // salva itens
            itemVendaDAO.cadastrarItens(conn, idVenda, venda.getItens());

            conn.commit();

            // atualiza objeto Venda com id gerado
            venda.setId(idVenda);

            sucesso = true;
        } catch (SQLException e) {
            System.err.println("ERRO na transação de venda! Rollback...");
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignore) {}
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ignore) {}
        }

        return sucesso;
    }

    // Insere a linha principal da venda e retorna o id gerado
    private int inserirVendaPrincipal(Connection conn, Venda venda) throws SQLException {
        String sql = "INSERT INTO venda (data, valor_total, id_funcionario, id_cliente, forma_pagamento) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // se no seu código você prefere NOW() no SQL
            Timestamp ts = (venda.getData() != null) ? Timestamp.valueOf(venda.getData()) : new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(1, ts);

            // valor total (BigDecimal)
            stmt.setBigDecimal(2, venda.getValorTotal() == null ? BigDecimal.ZERO : venda.getValorTotal());

            // id_funcionario
            if (venda.getIdFuncionario() != null) stmt.setInt(3, venda.getIdFuncionario());
            else stmt.setNull(3, Types.INTEGER);

            // id_cliente
            if (venda.getIdCliente() != null) stmt.setInt(4, venda.getIdCliente());
            else stmt.setNull(4, Types.INTEGER);

            // forma_pagamento
            stmt.setString(5, venda.getFormaPagamento());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        return -1;
    }

    // Buscar itens de uma venda
    public List<Item_venda> buscarItensDaVenda(int idVenda) {
        return itemVendaDAO.buscarItensPorVenda(idVenda);
    }

    // Calcular total de vendas do dia
    public double calcularTotalVendasHoje() {
        String sql = "SELECT SUM(valor_total) AS total FROM venda WHERE DATE(data) = CURDATE()";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total.doubleValue() : 0.0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular total de vendas de hoje: " + e.getMessage());
        }
        return 0.0;
    }

    // BUSCAR TODAS AS VENDAS PARA RELATÓRIO (resumido)
    public List<Venda> buscarTodasVendasRelatorio() {
        String sql =
                "SELECT v.id_venda, v.data, v.valor_total, v.id_funcionario, v.id_cliente, " +
                        " (SELECT p.nome FROM item_venda iv JOIN produto p ON iv.id_produto = p.id_produto WHERE iv.id_venda = v.id_venda LIMIT 1) AS nome_produto, " +
                        " (SELECT SUM(iv.quantidade) FROM item_venda iv WHERE iv.id_venda = v.id_venda) AS quantidade_total, " +
                        " f.nome AS nome_funcionario, c.nome AS nome_cliente " +
                        "FROM venda v " +
                        "LEFT JOIN funcionario f ON v.id_funcionario = f.id_funcionario " +
                        "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "ORDER BY v.data DESC";

        List<Venda> lista = new ArrayList<>();

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Venda v = mapearVendaRelatorio(rs);
                lista.add(v);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas vendas (relatório): " + e.getMessage());
        }
        return lista;
    }

    // compatibilidade com TesteDAO antigo
    public List<Venda> buscarTodasVendas() {
        return buscarTodasVendasRelatorio();
    }

    // BUSCAR VENDAS POR PERÍODO (utilizado em testes/relatórios)
    public List<Venda> buscarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        String sql =
                "SELECT v.id_venda, v.data, v.valor_total, v.id_funcionario, v.id_cliente, " +
                        " (SELECT p.nome FROM item_venda iv JOIN produto p ON iv.id_produto = p.id_produto WHERE iv.id_venda = v.id_venda LIMIT 1) AS nome_produto, " +
                        " (SELECT SUM(iv.quantidade) FROM item_venda iv WHERE iv.id_venda = v.id_venda) AS quantidade_total, " +
                        " f.nome AS nome_funcionario, c.nome AS nome_cliente " +
                        "FROM venda v " +
                        "LEFT JOIN funcionario f ON v.id_funcionario = f.id_funcionario " +
                        "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "WHERE v.data BETWEEN ? AND ? " +
                        "ORDER BY v.data DESC";

        List<Venda> lista = new ArrayList<>();

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVendaRelatorio(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas por período: " + e.getMessage());
        }

        return lista;
    }

    // BUSCAR VENDAS COM FILTROS (para RelatoriosController)
    public List<Venda> buscarVendasFiltradasRelatorio(LocalDateTime inicio, LocalDateTime fim,
                                                      Integer idFuncionario, BigDecimal minValor, BigDecimal maxValor,
                                                      String produtoFiltro, Integer idVendaExato) {

        StringBuilder sb = new StringBuilder(
                "SELECT v.id_venda, v.data, v.valor_total, v.id_funcionario, v.id_cliente, " +
                        " (SELECT p.nome FROM item_venda iv JOIN produto p ON iv.id_produto = p.id_produto WHERE iv.id_venda = v.id_venda LIMIT 1) AS nome_produto, " +
                        " (SELECT SUM(iv.quantidade) FROM item_venda iv WHERE iv.id_venda = v.id_venda) AS quantidade_total, " +
                        " f.nome AS nome_funcionario, c.nome AS nome_cliente " +
                        "FROM venda v " +
                        "LEFT JOIN funcionario f ON v.id_funcionario = f.id_funcionario " +
                        "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // CORREÇÃO: Filtros de data independentes
        if (inicio != null) {
            sb.append(" AND v.data >= ? ");
            params.add(Timestamp.valueOf(inicio));
        }

        if (fim != null) {
            sb.append(" AND v.data <= ? ");
            params.add(Timestamp.valueOf(fim));
        }

        if (idFuncionario != null) {
            sb.append(" AND v.id_funcionario = ? ");
            params.add(idFuncionario);
        }

        if (minValor != null) {
            sb.append(" AND v.valor_total >= ? ");
            params.add(minValor);
        }

        if (maxValor != null) {
            sb.append(" AND v.valor_total <= ? ");
            params.add(maxValor);
        }

        if (idVendaExato != null) {
            sb.append(" AND v.id_venda = ? ");
            params.add(idVendaExato);
        }

        if (produtoFiltro != null && !produtoFiltro.trim().isEmpty()) {
            sb.append(" AND EXISTS (SELECT 1 FROM item_venda iv JOIN produto p ON iv.id_produto = p.id_produto " +
                    " WHERE iv.id_venda = v.id_venda AND UPPER(p.nome) LIKE UPPER(?)) ");
            params.add("%" + produtoFiltro.trim() + "%");
        }

        sb.append(" ORDER BY v.data DESC");

        List<Venda> lista = new ArrayList<>();

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            // System.out.println("SQL: " + sb.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVendaRelatorio(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas filtradas: " + e.getMessage());
        }

        return lista;
    }

    private Venda mapearVendaRelatorio(ResultSet rs) throws SQLException {
        Venda v = new Venda();

        v.setId(rs.getInt("id_venda"));

        Timestamp ts = rs.getTimestamp("data");
        if (ts != null) v.setData(ts.toLocalDateTime());

        v.setValorTotal(rs.getBigDecimal("valor_total"));

        // nomes
        try { v.setNomeFuncionario(rs.getString("nome_funcionario")); } catch (SQLException ignore) {}
        try { v.setNomeCliente(rs.getString("nome_cliente")); } catch (SQLException ignore) {}

        // resumo produto / quantidade
        try { v.setNomeProduto(rs.getString("nome_produto")); } catch (SQLException ignore) {}
        try {
            int qt = rs.getInt("quantidade_total");
            if (!rs.wasNull()) v.setQuantidadeTotal(qt);
        } catch (SQLException ignore) {}

        return v;
    }

    public List<Venda> buscarUltimasCincoVendas() {
        String sql = "SELECT id_venda, data, valor_total FROM venda ORDER BY data DESC LIMIT 5";
        List<Venda> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Venda v = new Venda();
                v.setId(rs.getInt("id_venda"));
                Timestamp ts = rs.getTimestamp("data");
                if (ts != null) v.setData(ts.toLocalDateTime());
                v.setValorTotal(rs.getBigDecimal("valor_total"));
                lista.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar as últimas 5 vendas: " + e.getMessage());
        }
        return lista;
    }
}

