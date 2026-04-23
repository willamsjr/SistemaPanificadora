package controller;

import app.MainApp;
import dao.FuncionarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Funcionario;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblMensagemErro;

    private MainApp mainApp;
    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void inicialize(){
        btnLogin.setDefaultButton(true);
    }

    @FXML
    public void handleLogin() {
        String usuario = txtUsuario.getText();
        String senha = txtSenha.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblMensagemErro.setText("Preencha usuário e senha.");
            return;
        }

        // AUTENTICA USANDO O BANCO
        Funcionario funcionario = funcionarioDAO.autenticar(usuario, senha);

        if (funcionario != null) {
            lblMensagemErro.setText("");

            if (mainApp != null) {
                MainApp.setUsuarioLogado(funcionario);

                // 2. Mudar a tela
                mainApp.showDashboardScreen();
            }

        } else {
            lblMensagemErro.setText("Usuário ou senha inválidos.");
        }
    }
}