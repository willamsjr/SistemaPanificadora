package controller;

import app.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import model.Funcionario;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private AnchorPane mainContentPane;

    @FXML
    private Button btnGerenciarFuncionarios;

    @FXML
    private Button btnCadastroVendas;

    @FXML
    private Button btnRelatorios;

    @FXML
    private Button btnAgendamento;

    @FXML
    private Button btnEstoque;

    @FXML
    private void handleGerenciarFuncionarios() {
        loadView("/app/view/GerenciarFuncionarios.fxml");
    }

    @FXML
    private void handleDashboard() {
        loadView("/app/view/Dashboard.fxml");
    }


    @FXML
    private void handleCadastroVendas() {
        loadView("/app/view/CadastroVendas.fxml");
    }

    @FXML
    private void handleAgendamento() {
        loadView("/app/view/Agendamento.fxml");
    }

    @FXML
    private void handleEstoque() {
        loadView("/app/view/Estoque.fxml");
    }

    @FXML
    private void handleRelatorios() {
        loadView("/app/view/Relatorios.fxml");
    }

    @FXML
    private void handleSair() {
        Platform.exit();
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();
            mainContentPane.getChildren().setAll(newView);

            // Faz o conteúdo preencher 100% da área
            AnchorPane.setTopAnchor(newView, 0.0);
            AnchorPane.setBottomAnchor(newView, 0.0);
            AnchorPane.setLeftAnchor(newView, 0.0);
            AnchorPane.setRightAnchor(newView, 0.0);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ================================
    //  TELA INICIAL E RESTRIÇÃO DE ACESSO
    // ================================
    @FXML
    public void initialize() {
        loadView("/app/view/Dashboard.fxml");

        Funcionario usuarioLogado = MainApp.getUsuarioLogado();

        if (usuarioLogado != null) {
            String cargo = usuarioLogado.getCargo().toUpperCase();

            if ("PADEIRO".equals(cargo)) {
                // Aqui você só esconde os botões do MENU LATERAL
                esconderBotao(btnGerenciarFuncionarios);
                esconderBotao(btnCadastroVendas);
                esconderBotao(btnRelatorios);
            }
            else if ("ATENDENTE".equals(cargo)) {
                esconderBotao(btnGerenciarFuncionarios);
                esconderBotao(btnRelatorios);
            }
        }
    }

    // Método auxiliar para deixar o código limpo
    private void esconderBotao(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }
}