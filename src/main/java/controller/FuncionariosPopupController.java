package controller;

import dao.FuncionarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Funcionario;

public class FuncionariosPopupController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf; // Adicionado conforme o novo FXML
    @FXML private TextField txtLogin;
    @FXML private TextField txtSenha;
    @FXML private ComboBox<String> cbCargo; // O novo campo de Cargo

    @FXML private Button btnSalvar;

    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    @FXML
    private void initialize() {
        // Preenche as opções de Cargo logo que a tela abre
        cbCargo.setItems(FXCollections.observableArrayList(
                "Administrador",
                "Gerente",
                "Atendente",
                "Padeiro"
        ));

        btnSalvar.setOnAction(e -> salvarFuncionario());
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText();
        String cpf = txtCpf.getText();
        String login = txtLogin.getText();
        String senha = txtSenha.getText();
        String cargo = cbCargo.getValue(); // Pega o valor selecionado no ComboBox

        // Validação: agora verificamos se o cargo também foi selecionado
        if (nome.isEmpty() || login.isEmpty() || senha.isEmpty() || cargo == null) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios",
                    "Preencha todos os campos e selecione um cargo para continuar.");
            return;
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        funcionario.setCpf(cpf); // Certifique-se que sua Model Funcionario tem o setCpf
        funcionario.setLogin(login);
        funcionario.setSenhaHash(senha);
        funcionario.setCargo(cargo); // Certifique-se que sua Model Funcionario tem o setCargo

        boolean sucesso = funcionarioDAO.cadastrar(funcionario);

        if (sucesso) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                    "Funcionário cadastrado com sucesso!");
            limparCampos();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro",
                    "Erro ao salvar no banco de dados. Verifique se o banco tem a coluna 'cargo'.");
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtCpf.clear();
        txtLogin.clear();
        txtSenha.clear();
        cbCargo.getSelectionModel().clearSelection(); // Limpa a seleção do cargo
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}