package controller;

import dao.FuncionarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.Funcionario;

public class FuncionariosPopupController {

    @FXML private TextField txtNome;
    @FXML private TextField txtLogin;
    @FXML private TextField txtSenha;

    @FXML private Button btnSalvar;

    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    @FXML
    private void initialize() {
        btnSalvar.setOnAction(e -> salvarFuncionario());
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText();
        String login = txtLogin.getText();
        String senha = txtSenha.getText();

        if (nome.isEmpty() || login.isEmpty() || senha.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios",
                    "Preencha todos os campos para continuar.");
            return;
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        funcionario.setLogin(login);
        funcionario.setSenhaHash(senha); // senhaHash = senha simples por enquanto

        boolean sucesso = funcionarioDAO.cadastrar(funcionario);

        if (sucesso) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                    "Funcionário cadastrado com sucesso!");
            limparCampos();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro",
                    "Erro ao salvar no banco de dados.");
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtLogin.clear();
        txtSenha.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
