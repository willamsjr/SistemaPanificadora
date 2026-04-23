package controller;

import dao.ProdutoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Produto;

import java.math.BigDecimal;

public class EditarProdutoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtPreco;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtMinimo;

    private Produto produto;
    private Stage stage;

    private boolean alterado = false; // <<<<<< NECESSÁRIO PARA O EstoqueController
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // Retorna se o produto foi alterado (usado pelo EstoqueController)
    public boolean isAlterado() {
        return alterado;
    }

    // Recebe o Stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Recebe o produto para edição
    public void setProduto(Produto produto) {
        this.produto = produto;

        if (produto != null) {
            txtNome.setText(produto.getNome());
            txtPreco.setText(produto.getPreco().toString());
            txtQuantidade.setText(produto.getQntEstoque().toString());
            txtMinimo.setText(produto.getEstoqueMinimo().toString());
        }
    }

    @FXML
    private void salvar() {

        // valida campos
        String nome = txtNome.getText().trim();
        String precoStr = txtPreco.getText().trim();
        String qtdStr = txtQuantidade.getText().trim();
        String minStr = txtMinimo.getText().trim();

        if (nome.isEmpty() || precoStr.isEmpty() || qtdStr.isEmpty() || minStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dados incompletos", "Preencha todos os campos.");
            return;
        }

        BigDecimal preco;
        int quantidade;
        int minimo;

        try {
            preco = new BigDecimal(precoStr.replace(",", "."));
            quantidade = Integer.parseInt(qtdStr);
            minimo = Integer.parseInt(minStr);

            if (preco.compareTo(BigDecimal.ZERO) < 0 || quantidade < 0 || minimo < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Valores inválidos", "Nenhum valor pode ser negativo.");
                return;
            }

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato inválido", "Verifique preço e quantidade.");
            return;
        }

        // atualiza o produto
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setQntEstoque(quantidade);
        produto.setEstoqueMinimo(minimo);

        boolean atualizado = produtoDAO.atualizar(produto);

        if (!atualizado) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar", "Não foi possível atualizar o produto.");
            return;
        }

        alterado = true; // <<<<< IMPORTANTE

        fecharJanela();
    }

    @FXML
    private void cancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        if (stage != null) {
            stage.close();
        } else {
            txtNome.getScene().getWindow().hide();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo);
        a.setHeaderText(titulo);
        a.setContentText(mensagem);
        a.showAndWait();
    }
}
