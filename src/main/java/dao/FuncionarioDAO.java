package dao;

import conexao.ConexaoDB;
import model.Funcionario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    private Connection connection;

    public FuncionarioDAO() {
        this.connection = ConexaoDB.getConnection();
    }

    // ===============================
    // CADASTRAR
    // ===============================
    public boolean cadastrar(Funcionario funcionario) {
        String sql = "INSERT INTO funcionario (nome, login, senha_hash, cargo) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, funcionario.getNome());
            ps.setString(2, funcionario.getLogin());
            ps.setString(3, funcionario.getSenhaHash());
            ps.setString(4, funcionario.getCargo() == null ? "FUNCIONARIO" : funcionario.getCargo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar funcionário:");
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // LISTAR (nome que seu controller usa)
    // ===============================
    public List<Funcionario> listar() {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT * FROM funcionario";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Funcionario f = new Funcionario();

                f.setId(rs.getInt("id_funcionario"));
                f.setNome(rs.getString("nome"));
                f.setLogin(rs.getString("login"));
                f.setSenhaHash(rs.getString("senha_hash"));
                f.setCargo(rs.getString("cargo"));

                lista.add(f);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar funcionários:");
            e.printStackTrace();
        }

        return lista;
    }

    // ===============================
    // LISTAR TODOS (continua funcionando)
    // ===============================
    public List<Funcionario> listarTodos() {
        return listar(); // usa a mesma função
    }

    // ===============================
    // ATUALIZAR
    // ===============================
    public boolean atualizar(Funcionario funcionario) {
        String sql = "UPDATE funcionario SET nome=?, login=?, senha_hash=?, cargo=? WHERE id_funcionario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, funcionario.getNome());
            ps.setString(2, funcionario.getLogin());
            ps.setString(3, funcionario.getSenhaHash());
            ps.setString(4, funcionario.getCargo());
            ps.setInt(5, funcionario.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar funcionário:");
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // EXCLUIR
    // ===============================
    public boolean excluir(Integer id) {
        String sql = "DELETE FROM funcionario WHERE id_funcionario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir funcionário:");
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // BUSCAR POR LOGIN
    // ===============================
    public Funcionario buscarPorLogin(String login) {
        String sql = "SELECT * FROM funcionario WHERE login=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Funcionario f = new Funcionario();

                    f.setId(rs.getInt("id_funcionario"));
                    f.setNome(rs.getString("nome"));
                    f.setLogin(rs.getString("login"));
                    f.setSenhaHash(rs.getString("senha_hash"));
                    f.setCargo(rs.getString("cargo"));

                    return f;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar funcionário por login:");
            e.printStackTrace();
        }

        return null;
    }

    // ===============================
    // AUTENTICAÇÃO
    // ===============================
    public Funcionario autenticar(String login, String senhaDigitada) {

        String sql = "SELECT * FROM funcionario WHERE login=? AND senha_hash=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, senhaDigitada);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Funcionario f = new Funcionario();

                    f.setId(rs.getInt("id_funcionario"));
                    f.setNome(rs.getString("nome"));
                    f.setLogin(rs.getString("login"));
                    f.setSenhaHash(rs.getString("senha_hash"));
                    f.setCargo(rs.getString("cargo"));

                    return f;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao autenticar funcionário:");
            e.printStackTrace();
        }

        return null;
    }
}
