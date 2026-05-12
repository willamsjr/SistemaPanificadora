package app;

import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


import java.awt.*;
import java.io.IOException;
import model.Funcionario;

public class MainApp extends Application {

    private Stage primaryStage;
    private static Funcionario usuarioLogado;

    public static Funcionario getUsuarioLogado() { return usuarioLogado; }
    public static void setUsuarioLogado(Funcionario funcionario) { usuarioLogado = funcionario; }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
            ImageView imageView = new ImageView(logo);
            imageView.setFitWidth(400);
            imageView.setPreserveRatio(true);

            StackPane splashLayout = new StackPane(imageView);
            splashLayout.setStyle("-fx-background-color: transparent;");

            Scene splashScene = new Scene(splashLayout, 450, 450,javafx.scene.paint.Color.TRANSPARENT);

                Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.TRANSPARENT); // Sem bordas
            splashStage.setScene(splashScene);
            splashStage.centerOnScreen();
            splashStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
                splashStage.close();
                showLoginScreen();
            });
            delay.play();

        } catch (Exception e) {
            System.err.println("Logo não encontrada, iniciando login direto...");
            showLoginScreen();
        }
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/app/view/Login.fxml"));

            StackPane loginLayout = loader.load();
            Scene scene = new Scene(loginLayout);

            scene.getStylesheets().add(getClass().getResource("/app/css/styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/app/css/menu.css").toExternalForm());

            primaryStage.setTitle("Sistema de Padaria - Login");
            primaryStage.setScene(scene);

            // LINHA PARA TELA CHEIA NO LOGIN
            primaryStage.setMaximized(true);

            primaryStage.show();

            LoginController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela de Login.");
            e.printStackTrace();
        }
    }

    public void showDashboardScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/app/view/MenuPrincipal.fxml"));

            AnchorPane dashboardLayout = loader.load();
            Scene scene = new Scene(dashboardLayout);

            scene.getStylesheets().add(getClass().getResource("/app/css/styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/app/css/menu.css").toExternalForm());

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Sistema de Padaria - Dashboard");
            dashboardStage.setScene(scene);

            // LINHA PARA TELA CHEIA NA DASHBOARD
            dashboardStage.setMaximized(true);

            dashboardStage.show();

            if (primaryStage != null) {
                primaryStage.close();
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela da Dashboard.");
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}