package dao;

import conexao.ConexaoDB;
import model.Item_venda;
import model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class Item_vendaDAO {

    public void cadastrarItens(Connection conn, int id_venda, List<Item_venda> itens) throws SQLException {
        String sql = "INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unit) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Item_venda item : itens) {
                stmt.setInt(1, id_venda);
                stmt.setInt(2, item.getIdProduto());
                stmt.setInt(3, item.getQuantidade());
                stmt.setBigDecimal(4, item.getPrecoUnitario());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    // novo: buscar itens de uma venda (com dados do produto)
    public List<Item_venda> buscarItensPorVenda(int idVenda) {
        List<Item_venda> itens = new ArrayList<>();
        String sql = "SELECT iv.id_item, iv.quantidade, iv.preco_unit, p.id_produto, p.nome, p.preco, p.qnt_estoque " +
                "FROM item_venda iv " +
                "JOIN produto p ON iv.id_produto = p.id_produto " +
                "WHERE iv.id_venda = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVenda);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Item_venda item = new Item_venda();
                    item.setId(rs.getInt("id_item"));
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setPrecoUnitario(rs.getBigDecimal("preco_unit"));

                    Produto p = new Produto();
                    p.setId(rs.getInt("id_produto"));
                    p.setNome(rs.getString("nome"));
                    p.setPreco(rs.getBigDecimal("preco"));
                    p.setQntEstoque(rs.getInt("qnt_estoque"));

                    item.setProduto(p);
                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar itens da venda: " + e.getMessage());
        }

        return itens;
    }
}
