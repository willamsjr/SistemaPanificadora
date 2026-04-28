package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AjudaController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblTexto;

    // Método para carregar os dados (que você já deve ter)
    public void initData(String titulo, String texto) {
        lblTitulo.setText(titulo);
        lblTexto.setText(texto);
    }

    @FXML
    private void fechar(ActionEvent event) {
        // Pega a janela atual através do evento do botão e fecha ela
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}