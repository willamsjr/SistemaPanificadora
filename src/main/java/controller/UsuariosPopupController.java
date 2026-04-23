package controller;

import dao.FuncionarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox; // << IMPORT FALTANTE AQUI
import model.Funcionario;

import java.util.Optional;

public class UsuariosPopupController {

    // tabela
    @FXML private TableView<Funcionario> tblUsuarios;
    @FXML private TableColumn<Funcionario, Integer> colId;
    @FXML private TableColumn<Funcionario, String> colNome;
    @FXML private TableColumn<Funcionario, String> colLogin;
    @FXML private TableColumn<Funcionario, String> colCargo;
    @FXML private TableColumn<Funcionario, Void> colAcoes;

    // botões
    @FXML private Button btnNovo;
    @FXML private Label lblInfo;

    // popup de edição
    @FXML private VBox popupEdicao;
    @FXML private TextField txtNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<String> comboCargo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final ObservableList<Funcionario> listaUsuarios = FXCollections.observableArrayList();

    private Funcionario usuarioEmEdicao = null;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));

        tblUsuarios.setItems(listaUsuarios);

        comboCargo.getItems().addAll("ADMIN", "FUNCIONARIO");

        btnSalvar.setOnAction(e -> salvarEdicao());
        btnCancelar.setOnAction(e -> fecharPopup());
        btnNovo.setOnAction(e -> abrirPopupNovo());

        configurarColunaAcoes();
        carregarUsuarios();

        popupEdicao.setVisible(false);
        lblInfo.setText("");
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                btnEditar.setOnAction(e -> {
                    Funcionario f = getTableView().getItems().get(getIndex());
                    abrirPopupEditar(f);
                });
                btnExcluir.setOnAction(e -> {
                    Funcionario f = getTableView().getItems().get(getIndex());
                    excluirUsuarioConfirm(f);
                });

                btnEditar.setStyle("-fx-background-color: #f1c40f;");
                btnExcluir.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, btnEditar, btnExcluir);
                    setGraphic(box);
                }
            }
        });
    }

    private void carregarUsuarios() {
        listaUsuarios.setAll(funcionarioDAO.listarTodos());
    }

    private void abrirPopupNovo() {
        usuarioEmEdicao = null;
        txtNome.clear();
        txtLogin.clear();
        txtSenha.clear();
        comboCargo.getSelectionModel().select("FUNCIONARIO");
        popupEdicao.setVisible(true);
    }

    private void abrirPopupEditar(Funcionario f) {
        usuarioEmEdicao = f;
        txtNome.setText(f.getNome());
        txtLogin.setText(f.getLogin());
        txtSenha.clear();
        comboCargo.getSelectionModel().select(
                f.getCargo() == null ? "FUNCIONARIO" : f.getCargo().toUpperCase()
        );
        popupEdicao.setVisible(true);
    }

    private void fecharPopup() {
        usuarioEmEdicao = null;
        popupEdicao.setVisible(false);
    }

    private void salvarEdicao() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim();
        String senha = txtSenha.getText().trim();
        String cargo = comboCargo.getSelectionModel().getSelectedItem();

        if (nome.isEmpty() || login.isEmpty() || (usuarioEmEdicao == null && senha.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Campos obrigatórios", "Preencha nome, login e senha (para novo usuário).");
            return;
        }

        if (usuarioEmEdicao == null) {
            Funcionario novo = new Funcionario();
            novo.setNome(nome);
            novo.setLogin(login);
            novo.setSenhaHash(senha);
            novo.setCargo(cargo);

            boolean ok = funcionarioDAO.cadastrar(novo);
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário cadastrado.");
                carregarUsuarios();
                fecharPopup();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao cadastrar usuário. Verifique se o login é único.");
            }

        } else {
            usuarioEmEdicao.setNome(nome);
            usuarioEmEdicao.setLogin(login);
            if (!senha.isEmpty()) usuarioEmEdicao.setSenhaHash(senha);
            usuarioEmEdicao.setCargo(cargo);

            boolean ok = funcionarioDAO.atualizar(usuarioEmEdicao);
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário atualizado.");
                carregarUsuarios();
                fecharPopup();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar usuário.");
            }
        }
    }

    private void excluirUsuarioConfirm(Funcionario f) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirmar exclusão");
        conf.setHeaderText(null);
        conf.setContentText("Tem certeza que deseja excluir " + f.getNome() + "?");

        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = funcionarioDAO.excluir(f.getId());
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário excluído.");
                carregarUsuarios();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao excluir usuário.");
            }
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
