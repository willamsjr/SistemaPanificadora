package controller;

import dao.ProdutoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produto;

import java.io.IOException;
import java.math.BigDecimal;

public class EstoqueController {

    // =================== TABELA =========================
    @FXML private TableView<Produto> tableEstoque;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, BigDecimal> colPreco;
    @FXML private TableColumn<Produto, Integer> colQtd;
    @FXML private TableColumn<Produto, Integer> colMin;

    // =================== CAMPOS DE CADASTRO =========================
    @FXML private TextField txtNome;
    @FXML private TextField txtPreco;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtMinima;

    // =================== BOTÕES =========================
    @FXML private Button btnCadastrarProduto;
    @FXML private Button btnEditarProduto;
    @FXML private Button btnExcluirProduto;
    @FXML private Button btnAtualizar;
    @FXML private Button btnAjuda; // <<<<< ADICIONADO AQUI

    private ObservableList<Produto> listaProdutos;
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // =====================================================
    // INICIALIZAÇÃO
    // =====================================================
    @FXML
    public void initialize() {
        configurarColunas();
        carregarTabela();
        aplicarCorEstoqueBaixo();

        btnCadastrarProduto.setOnAction(event -> cadastrarProduto());
        btnEditarProduto.setOnAction(event -> abrirTelaEditar());
        btnExcluirProduto.setOnAction(event -> excluirProduto());
        btnAtualizar.setOnAction(event -> carregarTabela());

        // Vincula a ação ao botão de ajuda
        if (btnAjuda != null) {
            btnAjuda.setOnAction(event -> acaoBotaoAjuda());
        }
    }

    @FXML
    private void acaoBotaoAjuda() {
        // Texto focado em Estoque conforme a tela
        mostrarAjuda("Controle de Estoque",
                "• Cadastrar: Preencha o formulário à direita para adicionar novos produtos.\n\n" +
                        "• Gestão: Selecione um item na tabela para Editar ou Excluir. Itens com quantidade baixa podem ser destacados.");
    }

    // =====================================================
    // CONFIGURAR COLUNAS
    // =====================================================
    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qntEstoque"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));
    }

    // =====================================================
    // CARREGAR TABELA
    // =====================================================
    private void carregarTabela() {
        listaProdutos = FXCollections.observableArrayList(produtoDAO.listarTodos());
        tableEstoque.setItems(listaProdutos);
    }

    // =====================================================
    // CATEGORIZAR LINHAS COM ESTOQUE BAIXO
    // =====================================================
    private void aplicarCorEstoqueBaixo() {
        tableEstoque.setRowFactory(tv -> new TableRow<Produto>() {
            @Override
            protected void updateItem(Produto produto, boolean empty) {
                super.updateItem(produto, empty);

                getStyleClass().remove("low-stock-row");

                if (!empty && produto != null && produto.getQntEstoque() <= produto.getEstoqueMinimo()) {
                    getStyleClass().add("low-stock-row");
                }
            }
        });
    }

    // =====================================================
    // CADASTRAR PRODUTO NA PRÓPRIA TELA
    // =====================================================
    private void cadastrarProduto() {
        if (txtNome.getText().isEmpty() ||
                txtPreco.getText().isEmpty() ||
                txtQuantidade.getText().isEmpty() ||
                txtMinima.getText().isEmpty()) {

            alerta("Preencha todos os campos!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String nome = txtNome.getText();
            BigDecimal preco = new BigDecimal(txtPreco.getText().replace(",", "."));
            int qtd = Integer.parseInt(txtQuantidade.getText());
            int min = Integer.parseInt(txtMinima.getText());

            Produto novo = new Produto();
            novo.setNome(nome);
            novo.setPreco(preco);
            novo.setQntEstoque(qtd);
            novo.setEstoqueMinimo(min);

            produtoDAO.adicionar(novo);

            limparCampos();
            carregarTabela();

        } catch (Exception e) {
            alerta("Erro nos valores numéricos. Verifique preço e quantidades.", Alert.AlertType.ERROR);
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtPreco.clear();
        txtQuantidade.clear();
        txtMinima.clear();
    }

    private void alerta(String texto, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(texto);
        alert.showAndWait();
    }

    // =====================================================
    // EDITAR PRODUTO → abre modal
    // =====================================================
    private void abrirTelaEditar() {
        Produto selecionado = tableEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            alerta("Selecione um produto para editar!", Alert.AlertType.WARNING);
            return;
        }

        boolean alterado = abrirModal("/app/view/EditarProduto.fxml", "Editar Produto", selecionado);

        if (alterado) {
            carregarTabela();
        }
    }

    // =====================================================
    // EXCLUIR
    // =====================================================
    private void excluirProduto() {
        Produto selecionado = tableEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            alerta("Selecione um produto para excluir!", Alert.AlertType.WARNING);
            return;
        }

        produtoDAO.excluir(selecionado.getId());
        carregarTabela();
    }

    // =====================================================
    // ABRIR MODAL
    // =====================================================
    private boolean abrirModal(String arquivo, String titulo, Produto produtoEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(arquivo));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Object controller = loader.getController();

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);

            try {
                controller.getClass().getMethod("setProduto", Produto.class)
                        .invoke(controller, produtoEditar);
            } catch (Exception ignored) {}

            try {
                controller.getClass().getMethod("setStage", Stage.class)
                        .invoke(controller, stage);
            } catch (Exception ignored) {}

            stage.showAndWait();

            try {
                return (boolean) controller.getClass()
                        .getMethod("isAlterado")
                        .invoke(controller);
            } catch (Exception e) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void mostrarAjuda(String titulo, String texto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/AjudaView.fxml"));
            Parent root = loader.load();

            // Configurando o texto dentro do AjudaController personalizado
            AjudaController ajuda = loader.getController();
            ajuda.initData(titulo, texto);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajuda - " + titulo);

            // Travas de segurança para a janela
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setResizable(false);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao carregar ajuda: " + e.getMessage());
        }
    }
}