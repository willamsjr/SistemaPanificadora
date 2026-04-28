package controller;

import dao.ProdutoDAO;
import dao.VendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Item_venda;
import model.Produto;
import model.Venda;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.io.IOException;

public class CadastroVendasController {

    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private VendaDAO vendaDAO = new VendaDAO();
    private Produto produtoSelecionadoParaVenda;

    private ObservableList<Item_venda> carrinhoItens = FXCollections.observableArrayList();
    private final NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // Popup de sugestões (estilo Google)
    private ContextMenu popupBusca = new ContextMenu();

    private final int ID_FUNCIONARIO_LOGADO = 1;

    @FXML private TextField txtProdutoBusca;
    @FXML private TextField txtProdutoQtd;
    @FXML private Button btnAdicionarItem;

    @FXML private TableView<Item_venda> tblItensVenda;
    @FXML private TableColumn<Item_venda, String> colProdutoNome;
    @FXML private TableColumn<Item_venda, Integer> colProdutoQtd;
    @FXML private TableColumn<Item_venda, BigDecimal> colProdutoPrecoUnit;
    @FXML private TableColumn<Item_venda, BigDecimal> colProdutoPrecoTotal;

    @FXML private Label lblTotalVenda;
    @FXML private Button btnFinalizarVenda;
    @FXML private Button btnGerenciarFuncionarios;
    @FXML private Button btnGerenciarUsuarios;
    @FXML private Button btnRemoverItem;
    @FXML private Button btnAjuda;

    @FXML
    public void initialize() {
        // 1. Configurar Tabela
        tblItensVenda.setItems(carrinhoItens);
        tblItensVenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colProdutoNome.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colProdutoQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colProdutoPrecoUnit.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colProdutoPrecoTotal.setCellValueFactory(new PropertyValueFactory<>("precoTotalItem"));

        // 2. Lógica da Busca Inteligente (Heurística #7 - Eficiência)
        configurarBuscaInstantanea();

        // 3. Ações dos botões
        btnAdicionarItem.setOnAction(e -> handleAdicionarItem());
        btnFinalizarVenda.setOnAction(e -> handleFinalizarVenda());
        btnGerenciarFuncionarios.setOnAction(e -> abrirPopupFuncionarios());
        btnGerenciarUsuarios.setOnAction(e -> abrirPopupUsuarios());
        btnRemoverItem.setOnAction(e -> handleRemoverItemSelecionado());

        if (btnAjuda != null) {
            btnAjuda.setOnAction(e -> acaoBotaoAjuda());
            btnAjuda.toFront();
        }

        atualizarTotalVenda();
    }

    @FXML
    private void acaoBotaoAjuda() {
        mostrarAjuda("Ajuda: Vendas",
                "• Adicionar: Busque o produto, coloque a quantidade e clique em ADICIONAR.\n\n" +
                        "• Corrigir: Selecione o item na tabela e use o botão vermelho REMOVER ITEM.\n\n" +
                        "• Finalizar: Confira o valor total e clique em FINALIZAR VENDA para concluir.");
    }

    private void configurarBuscaInstantanea() {
        txtProdutoBusca.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.trim().isEmpty() || novo.matches("\\d+")) {
                popupBusca.hide();
                return;
            }

            List<Produto> encontrados = produtoDAO.buscarPorNome(novo);

            if (encontrados.isEmpty()) {
                popupBusca.hide();
            } else {
                popupBusca.getItems().clear();
                for (Produto p : encontrados) {
                    MenuItem item = new MenuItem(p.getNome() + " - " + formatadorMoeda.format(p.getPreco()));
                    item.setOnAction(e -> {
                        this.produtoSelecionadoParaVenda = p; // Guarda o produto inteiro aqui!
                        txtProdutoBusca.setText(p.getNome()); // Mostra o nome pro Marcos
                        txtProdutoQtd.requestFocus();
                        popupBusca.hide();
                    });
                    popupBusca.getItems().add(item);
                }
                if (!popupBusca.isShowing()) {
                    popupBusca.show(txtProdutoBusca, Side.BOTTOM, 0, 0);
                }
            }
        });
    }

    private void handleAdicionarItem() {
        Produto produtoFinal = null;
        String input = txtProdutoBusca.getText().trim();

        try {
            // 1. Prioridade: Se o Marcos clicou na lista, usa o produto da memória
            if (produtoSelecionadoParaVenda != null && input.equals(produtoSelecionadoParaVenda.getNome())) {
                produtoFinal = produtoSelecionadoParaVenda;
            }
            // 2. Se o Marcos digitou um ID na mão
            else if (input.matches("\\d+")) {
                produtoFinal = produtoDAO.buscarPorId(Integer.parseInt(input));
            }

            if (produtoFinal == null) {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Produto não encontrado. Selecione da lista ou digite o ID.");
                return;
            }

            int quantidade = Integer.parseInt(txtProdutoQtd.getText().trim());

            // Agora segue sua lógica normal de adicionar ao carrinho usando 'produtoFinal'
            processarInclusaoNoCarrinho(produtoFinal, quantidade);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Quantidade inválida!");
        }
    }

    private void handleRemoverItemSelecionado() {
        // Pega o item que o usuário clicou na tabela
        Item_venda itemSelecionado = tblItensVenda.getSelectionModel().getSelectedItem();

        if (itemSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Selecione um item na tabela para remover!");
            return;
        }

        //Abre a confirmação (Importante para o Marcos não apagar sem querer!)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover do carrinho?");
        alert.setContentText("Deseja remover o produto: " + itemSelecionado.getProdutoNome() + "?");

        ButtonType btnSim = new ButtonType("Sim, remover");
        ButtonType btnNao = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSim) {
                carrinhoItens.remove(itemSelecionado);
                atualizarTotalVenda();
            }
        });
    }

    // Método auxiliar para não repetir código
    private void processarInclusaoNoCarrinho(Produto p, int qtd) {
        if (qtd > p.getQntEstoque()) {
            showAlert(Alert.AlertType.WARNING, "Estoque Insuficiente", "Só temos " + p.getQntEstoque() + " unidades.");
            return;
        }

        carrinhoItens.add(new Item_venda(p, qtd));
        txtProdutoBusca.clear();
        txtProdutoQtd.setText("1");
        produtoSelecionadoParaVenda = null; // Limpa a memória para a próxima busca
        atualizarTotalVenda();
    }

    private BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Item_venda item : carrinhoItens) {
            total = total.add(item.getPrecoTotalItem());
        }
        return total;
    }

    private void confirmarRemocao(Item_venda item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Item do Carrinho");
        alert.setContentText("Deseja realmente remover o produto: " + item.getProdutoNome() + "?");

        // Personalizando os botões de sim/não
        ButtonType btnSim = new ButtonType("Sim, remover");
        ButtonType btnNao = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSim, btnNao);

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSim) {
                carrinhoItens.remove(item); // Remove da lista
                atualizarTotalVenda();      // Recalcula o R$ lá embaixo
            }
        });
    }

    private void atualizarTotalVenda() {
        lblTotalVenda.setText("TOTAL: " + formatadorMoeda.format(calcularTotal()));
    }

    private void handleFinalizarVenda() {
        if (carrinhoItens.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erro", "Adicione itens à venda.");
            return;
        }

        Venda venda = new Venda();
        venda.setIdFuncionario(ID_FUNCIONARIO_LOGADO);
        venda.setData(LocalDateTime.now());
        venda.setValorTotal(calcularTotal());

        ArrayList<Item_venda> itensLimpos = new ArrayList<>();
        for (Item_venda item : carrinhoItens) {
            itensLimpos.add(new Item_venda(item.getProduto(), item.getQuantidade()));
        }
        venda.setItens(itensLimpos);

        if (vendaDAO.registrarVenda(venda)) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Venda concluída!");
            carrinhoItens.clear();
            atualizarTotalVenda();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao registrar venda.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void abrirPopupFuncionarios() {
        abrirJanela("/app/view/Funcionariospopup.fxml", "Gerenciar Funcionários");
    }

    private void abrirPopupUsuarios() {
        abrirJanela("/app/view/UsuariosPopup.fxml", "Gerenciar Usuários");
    }

    private void abrirJanela(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao abrir: " + titulo);
        }
    }

    private void mostrarAjuda(String titulo, String texto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/AjudaView.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Arquivo AjudaView.fxml não encontrado!");
            }

            Parent root = loader.load();
            AjudaController ajuda = loader.getController();
            ajuda.initData(titulo, texto);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajuda - " + titulo);

            // --- AS TRAVAS DE SEGURANÇA ---

            // 1. Bloqueia a janela de vendas atrás (Obrigatório clicar em Entendi ou fechar)
            stage.initModality(Modality.APPLICATION_MODAL);

            // 2. Garante que ela apareça na frente de tudo
            stage.setAlwaysOnTop(true);

            // 3. Impede o usuário de redimensionar e bagunçar o texto
            stage.setResizable(false);

            // Use showAndWait() em vez de apenas show() para pausar o código aqui
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao carregar ajuda: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a ajuda.");
        }
    }
}