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
        // 1. Verifica se já existe um produto com esse nome (mesmo que inativo)
        String sqlBusca = "SELECT id_produto FROM produto WHERE nome = ?";

        try (PreparedStatement stmtBusca = connection.prepareStatement(sqlBusca)) {
            stmtBusca.setString(1, produto.getNome());
            ResultSet rs = stmtBusca.executeQuery();

            if (rs.next()) {
                // 2. Se existe, nós apenas REATIVAMOS e ATUALIZAMOS em vez de inserir novo
                int idExistente = rs.getInt("id_produto");
                produto.setId(idExistente);
                String sqlReativar = "UPDATE produto SET preco = ?, qnt_estoque = ?, estoque_minimo = ?, ativo = TRUE WHERE id_produto = ?";
                try (PreparedStatement stmtReativar = connection.prepareStatement(sqlReativar)) {
                    stmtReativar.setBigDecimal(1, produto.getPreco());
                    stmtReativar.setInt(2, produto.getQntEstoque());
                    stmtReativar.setInt(3, produto.getEstoqueMinimo());
                    stmtReativar.setInt(4, idExistente);
                    return stmtReativar.executeUpdate() > 0;
                }
            } else {
                // 3. Se não existe de jeito nenhum, faz o INSERT normal
                String sqlInsert = "INSERT INTO produto(nome, preco, qnt_estoque, estoque_minimo, ativo) VALUES(?, ?, ?, ?, TRUE)";
                try (PreparedStatement stmtInsert = connection.prepareStatement(sqlInsert)) {
                    stmtInsert.setString(1, produto.getNome());
                    stmtInsert.setBigDecimal(2, produto.getPreco());
                    stmtInsert.setInt(3, produto.getQntEstoque());
                    stmtInsert.setInt(4, produto.getEstoqueMinimo() == null ? 5 : produto.getEstoqueMinimo());
                    return stmtInsert.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao processar produto: " + e.getMessage());
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
        // Só busca se o produto estiver ativo
        String sql = "SELECT * FROM produto WHERE id_produto = ? AND ativo = TRUE";
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
        // Filtrando por ativo = TRUE para não vender produtos "excluídos"
        String sql = "SELECT id_produto, nome, preco, qnt_estoque FROM produto WHERE nome LIKE ? AND ativo = TRUE LIMIT 8";
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nomeBusca + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id_produto"));
                p.setNome(rs.getString("nome"));
                p.setPreco(rs.getBigDecimal("preco"));
                p.setQntEstoque(rs.getInt("qnt_estoque"));
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Produto> listarTodos() {
        // Lista apenas os produtos que não foram desativados
        String sql = "SELECT * FROM produto WHERE ativo = TRUE ORDER BY nome";
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
        // EXCLUSÃO LÓGICA: Apenas desativa o produto para manter integridade das vendas
        String sql = "UPDATE produto SET ativo = FALSE WHERE id_produto = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int contarEstoqueBaixo() {
        // Conta estoque baixo apenas de produtos que ainda estão em linha (ativos)
        String sql = "SELECT COUNT(*) FROM produto WHERE qnt_estoque <= estoque_minimo AND ativo = TRUE";
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