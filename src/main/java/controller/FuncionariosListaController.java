package controller;

import dao.FuncionarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Funcionario;
import javafx.scene.control.cell.PropertyValueFactory;

public class FuncionariosListaController {

    @FXML private TableView<Funcionario> tblFuncionarios;
    @FXML private TableColumn<Funcionario, Integer> colId;
    @FXML private TableColumn<Funcionario, String> colNome;
    @FXML private TableColumn<Funcionario, String> colLogin;

    @FXML private Button btnFechar;

    private FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private ObservableList<Funcionario> funcionariosObservable = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabela();
        carregarFuncionarios();

        btnFechar.setOnAction(e -> fecharJanela());
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));

        tblFuncionarios.setItems(funcionariosObservable);
    }

    private void carregarFuncionarios() {
        funcionariosObservable.setAll(funcionarioDAO.listar());
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnFechar.getScene().getWindow();
        stage.close();
    }
}
