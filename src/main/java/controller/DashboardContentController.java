package controller;

import dao.AgendamentoDAO;
import dao.ProdutoDAO;
import dao.VendaDAO;
import model.Venda; // Certifique-se de que a model Venda existe
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

public class DashboardContentController {

    private VendaDAO vendaDAO = new VendaDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    @FXML private Label lblTotalVendasHoje;
    @FXML private Label lblAgendamentosPendentes;
    @FXML private Label lblEstoqueBaixo;
    @FXML private VBox vboxUltimasVendas;

    @FXML
    public void initialize() {
        carregarEstatisticas();
        carregarUltimasVendas();
    }

    private void carregarEstatisticas() {
        double totalVendas = vendaDAO.calcularTotalVendasHoje();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        lblTotalVendasHoje.setText(nf.format(totalVendas));

        int pendentes = agendamentoDAO.contarAgendamentosPendentes();
        lblAgendamentosPendentes.setText(String.valueOf(pendentes));

        int estoqueBaixo = produtoDAO.contarEstoqueBaixo();
        lblEstoqueBaixo.setText(String.valueOf(estoqueBaixo));
    }

    private void carregarUltimasVendas() {
        // 1. O Atendente pede os dados para o Cofre (DAO)
        List<Venda> vendas = vendaDAO.buscarUltimasCincoVendas();

        if (vboxUltimasVendas != null) {
            vboxUltimasVendas.getChildren().clear();
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

            for (Venda v : vendas) {
                HBox linha = new HBox();
                linha.setSpacing(10);
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.setStyle("-fx-background-color: #F9F9F9; -fx-padding: 10; -fx-background-radius: 10;");

                Label hora = new Label((v.getData() != null) ? v.getData().format(dtf) : "--:--");
                Label titulo = new Label("Venda #" + v.getId());
                Region mola = new Region();
                HBox.setHgrow(mola, Priority.ALWAYS);
                Label valor = new Label(nf.format(v.getValorTotal()));
                valor.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

                linha.getChildren().addAll(hora, titulo, mola, valor);
                vboxUltimasVendas.getChildren().add(linha);
            }
        }
    }
}