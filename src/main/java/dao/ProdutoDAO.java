package dao;

import conexao.ConexaoDB;
import java.sql.*;
import model.Produto;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProdutoDAO {

    private Connection connection;

    public ProdutoDAO() {
        this.connection = ConexaoDB.getConnection();
    }

    public boolean adicionar(Produto produto) {
        String sql = "INSERT INTO produto(nome, preco, qnt_estoque, estoque_minimo) VALUES(?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPreco());
            stmt.setInt(3, produto.getQntEstoque());
            stmt.setInt(4, produto.getEstoqueMinimo() == null ? 5 : produto.getEstoqueMinimo()); // padrão 5
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Produto produto) {
        String sql = "UPDATE produto SET nome = ?, preco = ?, qnt_estoque = ?, estoque_minimo = ? WHERE id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPreco());
            stmt.setInt(3, produto.getQntEstoque());
            stmt.setInt(4, produto.getEstoqueMinimo() == null ? 5 : produto.getEstoqueMinimo());
            stmt.setInt(5, produto.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }

    public Produto buscarPorId(int id) {
        String sql = "SELECT * FROM produto WHERE id_produto = ?";
        Produto produto = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    produto = new Produto();
                    produto.setId(rs.getInt("id_produto"));
                    produto.setNome(rs.getString("nome"));
                    produto.setPreco(rs.getBigDecimal("preco"));
                    produto.setQntEstoque(rs.getInt("qnt_estoque"));
                    produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar: " + e.getMessage());
        }
        return produto;
    }

    public List<Produto> buscarPorNome(String nomeBusca) {
        // Usando as colunas reais do seu print: id_produto, nome, preco, qnt_estoque
        String sql = "SELECT id_produto, nome, preco, qnt_estoque FROM produto WHERE nome LIKE ? LIMIT 8";
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nomeBusca + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id_produto"));
                p.setNome(rs.getString("nome"));
                p.setPreco(rs.getBigDecimal("preco")); // Ajustado para 'preco'
                p.setQntEstoque(rs.getInt("qnt_estoque")); // Ajustado para 'qnt_estoque'
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produto ORDER BY nome";
        List<Produto> produtos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id_produto"));
                produto.setNome(rs.getString("nome"));
                produto.setPreco(rs.getBigDecimal("preco"));
                produto.setQntEstoque(rs.getInt("qnt_estoque"));
                produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage());
        }
        return produtos;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM produto WHERE id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir produto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Conta quantos produtos estão com estoque menor ou igual ao seu estoque_minimo.
     */
    public int contarEstoqueBaixo() {
        String sql = "SELECT COUNT(*) FROM produto WHERE qnt_estoque <= estoque_minimo";
        int contagem = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                contagem = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar estoque baixo: " + e.getMessage());
        }
        return contagem;
    }
}
