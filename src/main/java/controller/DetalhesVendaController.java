package controller;

import dao.VendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Item_venda;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DetalhesVendaController {

    private VendaDAO vendaDAO = new VendaDAO();
    private final NumberFormat formatador = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));

    @FXML private TableView<Item_venda> tblItens;
    @FXML private TableColumn<Item_venda, String> colProduto;
    @FXML private TableColumn<Item_venda, Integer> colQtd;
    @FXML private TableColumn<Item_venda, BigDecimal> colPrecoUnit;
    @FXML private TableColumn<Item_venda, BigDecimal> colSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnFechar;

    private ObservableList<Item_venda> listaItens = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tblItens.setItems(listaItens);
        colProduto.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("produtoNome"));
        colQtd.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantidade"));
        colPrecoUnit.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("precoUnitario"));
        colSubtotal.setCellValueFactory(cell -> {
            BigDecimal subtotal = cell.getValue().getPrecoTotalItem();
            return new javafx.beans.property.SimpleObjectProperty<>(subtotal);
        });

        btnFechar.setOnAction(e -> {
            Stage s = (Stage) btnFechar.getScene().getWindow();
            s.close();
        });
    }

    // called from caller to load and show items
    public void carregarItens(int idVenda) {
        List<Item_venda> itens = vendaDAO.buscarItensDaVenda(idVenda);
        listaItens.setAll(itens);

        BigDecimal total = BigDecimal.ZERO;
        for (Item_venda it : itens) {
            total = total.add(it.getPrecoTotalItem());
        }
        lblTotal.setText(formatador.format(total));
    }
}
