package controller;

import dao.AgendamentoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import model.Agendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class AgendamentoController {

    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private ObservableList<Agendamento> listaAgendamentos = FXCollections.observableArrayList();

    @FXML private TableView<Agendamento> tblAgendamentos;
    @FXML private TableColumn<Agendamento, LocalDateTime> colData;
    @FXML private TableColumn<Agendamento, String> colCliente;
    @FXML private TableColumn<Agendamento, String> colTelefone;
    @FXML private TableColumn<Agendamento, String> colDescricao;
    @FXML private TableColumn<Agendamento, String> colStatus;

    @FXML private DatePicker datePickerData;
    @FXML private TextField txtHora;
    @FXML private TextField txtClienteNome;
    @FXML private TextField txtClienteTelefone;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnAgendar;
    @FXML private Button btnConcluirPedido;
    @FXML private Button btnDeletarPedido;
    @FXML private Button btnAjuda;

    @FXML
    private void initialize() {
        configurarTabela();
        carregarAgendamentos();

        btnAgendar.setOnAction(e -> handleAgendar());
        btnConcluirPedido.setOnAction(e -> handleConcluirPedido());
        btnDeletarPedido.setOnAction(e -> handleDeletarPedido());

        datePickerData.setValue(LocalDate.now());

        aplicarMascaraTelefone(txtClienteTelefone);
        aplicarMascaraHora(txtHora);

        // Bloqueia dias passados no calendário visualmente
        datePickerData.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Criar o formatador no padrão BR
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Configurar a coluna para exibir no formato BR
        colData.setCellFactory(column -> new TableCell<Agendamento, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Formata a data de "EUA" para "Brasil"
                    setText(formatter.format(item));
                }
            }
        });

        if (btnAjuda != null) {
            btnAjuda.setOnAction(event -> acaoBotaoAjuda());
        }
    }

    @FXML
    private void acaoBotaoAjuda() {
        // Texto focado em Agendamentos com a regra de exclusão
        mostrarAjuda("Ajuda: Agendamentos",
                "• Novo Agendamento: Use o painel à direita para inserir Data, Hora e Cliente.\n\n" +
                        "• Concluir Pedido: Selecione um item pendente e use o botão 'Concluir' após a entrega.\n\n" +
                        "• Apagar Pedido: Para segurança, um agendamento só pode ser apagado se já estiver com o status CONCLUÍDO.");
    }

    private void configurarTabela() {
        tblAgendamentos.setItems(listaAgendamentos);
        colData.setCellValueFactory(new PropertyValueFactory<>("dataAgendamento"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefoneCliente"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricaoPedido"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void carregarAgendamentos() {
        listaAgendamentos.clear();
        List<Agendamento> agendamentosDoBanco = agendamentoDAO.buscarTodos();
        listaAgendamentos.addAll(agendamentosDoBanco);
    }

    private void handleAgendar() {
        LocalDate data = datePickerData.getValue();
        String nomeCliente = txtClienteNome.getText();
        String descricao = txtDescricao.getText();
        String horaTexto = txtHora.getText();
        String telefoneOriginal = txtClienteTelefone.getText();

        String telefoneLimpo = telefoneOriginal.replaceAll("\\D", "");

        if (telefoneLimpo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo Obrigatório", "O Telefone do cliente é obrigatório para o agendamento!");
            return;
        }

        if (telefoneLimpo.length() < 11) {
            showAlert(Alert.AlertType.WARNING, "Telefone Incompleto",
                    "O telefone deve ter o DDD e os 9 dígitos (11 números).\nEx: (75) 99106-0092");
            return;
        }

        if (data == null || nomeCliente.isEmpty() || descricao.isEmpty() || horaTexto.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Obrigatórios", "Data, Hora, Nome do Cliente e Descrição são obrigatórios.");
            return;
        }

        LocalTime hora;
        try {
            hora = LocalTime.parse(horaTexto, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.WARNING, "Formato Inválido", "A Hora deve estar no formato HH:mm (ex: 08:30).");
            return;
        }

        LocalDateTime dataAgendamento = data.atTime(hora);

        if (dataAgendamento.isBefore(LocalDateTime.now())) {
            showAlert(Alert.AlertType.ERROR, "Horário Inválido",
                    "Não é possível agendar para um horário que já passou!");
            return;
        }

        Agendamento novoAgendamento = new Agendamento();
        novoAgendamento.setDataAgendamento(dataAgendamento);
        novoAgendamento.setNomeCliente(nomeCliente);
        novoAgendamento.setTelefoneCliente(telefoneOriginal); // Salva com a máscara (parênteses e traço)
        novoAgendamento.setDescricaoPedido(descricao);
        novoAgendamento.setStatus("Pendente");

        boolean sucesso = agendamentoDAO.cadastrar(novoAgendamento);

        if (sucesso) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Pedido agendado com sucesso!");
            limparCamposCadastro();
            carregarAgendamentos();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro no Cadastro", "Não foi possível salvar no banco de dados.");
        }
    }

    private void handleConcluirPedido() {
        Agendamento selecionado = tblAgendamentos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Item", "Por favor, selecione um agendamento na tabela primeiro.");
            return;
        }

        if (!selecionado.getStatus().equals("Pendente")) {
            showAlert(Alert.AlertType.INFORMATION, "Status Inválido", "Este agendamento já está " + selecionado.getStatus().toLowerCase() + ".");
            return;
        }

        boolean sucesso = agendamentoDAO.atualizarStatus(selecionado.getId(), "Concluído");

        if (sucesso) {
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Agendamento (ID: " + selecionado.getId() + ") marcado como Concluído.");
            carregarAgendamentos();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar o status no banco de dados.");
        }
    }

    private void handleDeletarPedido() {
        Agendamento selecionado = tblAgendamentos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Item", "Por favor, selecione um agendamento na tabela primeiro.");
            return;
        }

        if (!selecionado.getStatus().equals("Concluído")) {
            showAlert(Alert.AlertType.WARNING, "Ação Inválida", "Apenas agendamentos com status 'Concluído' podem ser apagados.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Apagar Agendamento ID: " + selecionado.getId());
        confirmacao.setContentText("Tem certeza que deseja apagar permanentemente este agendamento?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean sucesso = agendamentoDAO.deletar(selecionado.getId());
            if (sucesso) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Agendamento apagado.");
                carregarAgendamentos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível apagar o agendamento do banco de dados.");
            }
        }
    }

    private void limparCamposCadastro() {
        datePickerData.setValue(LocalDate.now());
        txtHora.clear();
        txtClienteNome.clear();
        txtClienteTelefone.clear();
        txtDescricao.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void aplicarMascaraTelefone(TextField textField) {
        textField.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.length() < antigo.length()) return;

            String apenasNumeros = novo.replaceAll("\\D", "");
            StringBuilder resultado = new StringBuilder();
            int tam = apenasNumeros.length();

            if (tam > 0) resultado.append("(").append(apenasNumeros.substring(0, Math.min(tam, 2)));
            if (tam > 2) resultado.append(") ").append(apenasNumeros.substring(2, Math.min(tam, 7)));
            if (tam > 7) resultado.append("-").append(apenasNumeros.substring(7, Math.min(tam, 11)));

            textField.setText(resultado.toString());
            Platform.runLater(textField::end);
        });
    }

    private void aplicarMascaraHora(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            String apenasDigitos = newValue.replaceAll("\\D", "");
            if (apenasDigitos.length() > 4) apenasDigitos = apenasDigitos.substring(0, 4);

            StringBuilder formatado = new StringBuilder();
            int tam = apenasDigitos.length();

            if (tam > 0) formatado.append(apenasDigitos.substring(0, Math.min(tam, 2)));
            if (tam > 2) formatado.append(":").append(apenasDigitos.substring(2, tam));

            if (!newValue.equals(formatado.toString())) {
                textField.setText(formatado.toString());
                javafx.application.Platform.runLater(textField::end);
            }
        });
    }

    private void mostrarAjuda(String titulo, String texto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/AjudaView.fxml"));
            Parent root = loader.load();

            // Pega o controller da janelinha de ajuda que criamos ontem
            AjudaController ajuda = loader.getController();
            ajuda.initData(titulo, texto);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajuda - " + titulo);

            // Travas de segurança (Modal)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}