package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.FuncionarioDAO;
import dao.VendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import model.Funcionario;
import model.Venda;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class RelatoriosController {

    private final VendaDAO vendaDAO = new VendaDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    private final ObservableList<Venda> listaVendas = FXCollections.observableArrayList();
    private final NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFim;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpar;

    @FXML private ComboBox<Funcionario> comboFuncionario;
    @FXML private TextField txtMinValor;
    @FXML private TextField txtMaxValor;
    @FXML private TextField txtProdutoFiltro;
    @FXML private TextField txtBuscaRapida;

    @FXML private TableView<Venda> tblVendas;
    @FXML private TableColumn<Venda, Integer> colIdVenda;
    @FXML private TableColumn<Venda, LocalDateTime> colData;
    @FXML private TableColumn<Venda, String> colFuncionario;
    @FXML private TableColumn<Venda, String> colProduto;
    @FXML private TableColumn<Venda, Integer> colQuantidade;
    @FXML private TableColumn<Venda, BigDecimal> colValorTotal;

    @FXML private Label lblTotalVendasPeriodo;
    @FXML private Label lblValorTotalPeriodo;

    @FXML private BarChart<String, Number> barChartVendas;

    @FXML
    private void initialize() {
        configurarTabela();
        datePickerFim.setValue(LocalDate.now());
        carregarFuncionariosNoFiltro();

        // Vincula as ações dos botões
        btnBuscar.setOnAction(e -> handleBuscarPeriodoComFiltros());
        btnLimpar.setOnAction(e -> handleLimpar());

        carregarTodasVendas();

        tblVendas.setOnMouseClicked(event -> {
            // Verifica se foi clique duplo e se tem algo selecionado
            if (event.getClickCount() == 2 && tblVendas.getSelectionModel().getSelectedItem() != null) {
                exibirPopUpDetalhes(tblVendas.getSelectionModel().getSelectedItem());
            }
        });
    }

    @FXML
    private void acaoBotaoAjuda() {
        mostrarAjuda("Ajuda: Relatório de Vendas",
                "• Gráfico de Vendas: Mostra os 5 produtos mais vendidos no período selecionado.\n\n" +
                        "• Filtros: Você pode filtrar por Data, Funcionário ou buscar produtos específicos.\n\n" +
                        "• DETALHAR VENDA: Clique DUAS VEZES sobre uma venda na tabela para abrir um pop-up com todos os produtos, quantidades e preços individuais daquele pedido.\n\n" +
                        "• Exportar PDF: Gera um documento pronto para impressão com os dados filtrados na tela.");
    }

    private void exibirPopUpDetalhes(Venda vendaSelecionada) {
        VendaDAO dao = new VendaDAO();
        List<String> itens = dao.buscarDetalhesItens(vendaSelecionada.getId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhamento da Venda #" + vendaSelecionada.getId());
        alert.setHeaderText("Produtos Vendidos por: " + vendaSelecionada.getNomeFuncionario());

        StringBuilder sb = new StringBuilder();
        itens.forEach(item -> sb.append("✔ ").append(item).append("\n"));

        sb.append("\n--------------------------\n");
        sb.append("VALOR TOTAL: ").append(formatadorMoeda.format(vendaSelecionada.getValorTotal()));

        // --- AJUSTE PARA NÃO CORTAR ---
        TextArea areaTexto = new TextArea(sb.toString());
        areaTexto.setEditable(false);
        areaTexto.setWrapText(true); // Quebra a linha se for muito grande
        areaTexto.setPrefHeight(250); // Altura ideal para ver vários itens
        areaTexto.setPrefWidth(350);  // Largura para não cortar o nome

        // Remove o fundo branco padrão do TextArea para parecer um alerta limpo
        areaTexto.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");

        alert.getDialogPane().setContent(areaTexto);
        alert.getDialogPane().setMinWidth(400);
        alert.getDialogPane().setMinHeight(400);
        // ------------------------------

        alert.showAndWait();
    }

    private void configurarTabela() {
        tblVendas.setItems(listaVendas);

        colIdVenda.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFuncionario.setCellValueFactory(new PropertyValueFactory<>("nomeFuncionario"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeTotal"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        // Formatação da Data (BR)
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colData.setCellFactory(column -> new TableCell<Venda, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatadorData.format(item));
                }
            }
        });

        // Formatação de Moeda na Tabela
        colValorTotal.setCellFactory(column -> new TableCell<Venda, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatadorMoeda.format(item));
                }
            }
        });
    }

    private void carregarFuncionariosNoFiltro() {
        try {
            ObservableList<Funcionario> items = FXCollections.observableArrayList(funcionarioDAO.listarTodos());
            comboFuncionario.setItems(items);
            comboFuncionario.setPromptText("Todos");

            comboFuncionario.setCellFactory(cb -> new ListCell<Funcionario>() {
                @Override
                protected void updateItem(Funcionario item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });

            comboFuncionario.setButtonCell(new ListCell<Funcionario>() {
                @Override
                protected void updateItem(Funcionario item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });
        } catch (Exception ex) {
            System.err.println("Erro ao carregar funcionários: " + ex.getMessage());
        }
    }

    private void carregarTodasVendas() {
        List<Venda> vendasDoBanco = vendaDAO.buscarTodasVendasRelatorio();
        listaVendas.setAll(vendasDoBanco);
        atualizarResumo();
    }

    private void limparFiltros() {
        datePickerInicio.setValue(null);
        datePickerFim.setValue(LocalDate.now());
        comboFuncionario.getSelectionModel().clearSelection();
        if(txtMinValor != null) txtMinValor.clear();
        if(txtMaxValor != null) txtMaxValor.clear();
        if(txtProdutoFiltro != null) txtProdutoFiltro.clear();
        if(txtBuscaRapida != null) txtBuscaRapida.clear();
    }

    @FXML
    private void handleBuscarPeriodoComFiltros() {
        LocalDate LDinicio = datePickerInicio.getValue();
        LocalDate LDfim = datePickerFim.getValue();
        LocalDateTime inicio = (LDinicio != null ? LDinicio.atStartOfDay() : null);
        LocalDateTime fim = (LDfim != null ? LDfim.atTime(23, 59, 59) : null);

        Integer idFuncionario = null;
        Funcionario sel = comboFuncionario.getSelectionModel().getSelectedItem();
        if (sel != null) idFuncionario = sel.getId();

        BigDecimal minValor = (txtMinValor != null) ? parseDecimal(txtMinValor.getText()) : null;
        BigDecimal maxValor = (txtMaxValor != null) ? parseDecimal(txtMaxValor.getText()) : null;
        String produtoFiltro = (txtProdutoFiltro != null) ? txtProdutoFiltro.getText() : null;

        String busca = (txtBuscaRapida != null) ? txtBuscaRapida.getText() : null;
        Integer idVendaBusca = null;
        if (busca != null && !busca.isEmpty()) {
            try {
                idVendaBusca = Integer.parseInt(busca);
            } catch (NumberFormatException e) {
                produtoFiltro = busca;
            }
        }

        List<Venda> filtrados = vendaDAO.buscarVendasFiltradasRelatorio(
                inicio, fim, idFuncionario, minValor, maxValor, produtoFiltro, idVendaBusca
        );

        listaVendas.setAll(filtrados);
        atualizarResumo();
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null) return null;
        String t = s.trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (Exception e) {
            return null;
        }
    }

    private void atualizarResumo() {
        BigDecimal totalValor = BigDecimal.ZERO;
        for (Venda venda : listaVendas) {
            totalValor = totalValor.add(venda.getValorTotal());
        }

        lblTotalVendasPeriodo.setText(String.valueOf(listaVendas.size()));
        lblValorTotalPeriodo.setText(formatadorMoeda.format(totalValor));

        atualizarGrafico();
    }

    private void atualizarGrafico() {
        barChartVendas.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Double> resumoVendas = listaVendas.stream()
                .filter(v -> v.getNomeProduto() != null && v.getValorTotal() != null)
                .collect(Collectors.groupingBy(
                        v -> {
                            // Se o nome for muito grande (combo), corta para não quebrar o gráfico
                            String nome = v.getNomeProduto();
                            return nome.length() > 15 ? nome.substring(0, 12) + "..." : nome;
                        },
                        Collectors.summingDouble(v -> v.getValorTotal().doubleValue())
                ));

        resumoVendas.entrySet().stream()
                .sorted((b, a) -> a.getValue().compareTo(b.getValue()))
                .limit(5)
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        barChartVendas.getData().add(series);

        // Pintura Vinho (#6e0d0d)
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #6e0d0d;");
                    Tooltip.install(data.getNode(), new Tooltip(data.getXValue() + ": " + formatadorMoeda.format(data.getYValue())));
                }
            }
        });
    }

    @FXML
    private void handleLimpar() {
        limparFiltros();
        carregarTodasVendas();
    }

    @FXML
    private void handleExportarPDF() {
        if (listaVendas.isEmpty()) {
            exibirAlerta("Aviso", "Não há dados na tabela para exportar.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório PDF");
        fileChooser.setInitialFileName("Relatorio_Vendas_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(btnBuscar.getScene().getWindow());

        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Fontes
                Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
                Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

                // Título do documento
                Paragraph pTitulo = new Paragraph("RELATÓRIO DE VENDAS - SISTEMA PADARIA", fonteTitulo);
                pTitulo.setAlignment(Element.ALIGN_CENTER);
                pTitulo.setSpacingAfter(20);
                document.add(pTitulo);

                // Informações de resumo
                document.add(new Paragraph("Data de Emissão: " + LocalDateTime.now().format(formatadorData), fonteNormal));
                document.add(new Paragraph("Total de Registros: " + lblTotalVendasPeriodo.getText(), fonteNormal));
                document.add(new Paragraph("Valor Total do Período: " + lblValorTotalPeriodo.getText(), fonteNormal));
                document.add(new Paragraph(" ")); // Espaço

                // Tabela (5 colunas)
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 3, 3, 4, 2});

                // Cabeçalhos da tabela no PDF
                String[] colunas = {"ID", "Data", "Funcionário", "Produto", "Total"};
                for (String nomeColuna : colunas) {
                    PdfPCell cell = new PdfPCell(new Phrase(nomeColuna, fonteCabecalho));
                    cell.setBackgroundColor(new BaseColor(110, 13, 13)); // Cor Vinho #6e0d0d
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Preencher dados da tabela
                for (Venda v : listaVendas) {
                    table.addCell(new Phrase(String.valueOf(v.getId()), fonteNormal));
                    table.addCell(new Phrase(v.getData().format(formatadorData), fonteNormal));
                    table.addCell(new Phrase(v.getNomeFuncionario(), fonteNormal));
                    table.addCell(new Phrase(v.getNomeProduto(), fonteNormal));
                    table.addCell(new Phrase(formatadorMoeda.format(v.getValorTotal()), fonteNormal));
                }

                document.add(table);
                exibirAlerta("Sucesso", "PDF gerado com sucesso em:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                e.printStackTrace();
                exibirAlerta("Erro", "Falha ao gerar o PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                if (document.isOpen()) document.close();
            }
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarAjuda(String titulo, String texto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/AjudaView.fxml"));
            Parent root = loader.load();

            AjudaController ajuda = loader.getController();
            ajuda.initData(titulo, texto);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajuda - " + titulo);

            // --- AS LINHAS QUE RESOLVEM ---
            stage.sizeToScene(); // Ajusta a janela ao tamanho do conteúdo
            stage.setResizable(false);
            // ------------------------------

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}