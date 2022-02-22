package com.geekbrains.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AuthController implements Initializable {

    public TextField authLoginField;
    public PasswordField authPasswordField;
    private MainController controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
      controller = new MainController();
    }

    public void authButton(ActionEvent actionEvent) throws IOException {
        if (authLoginField.getText().isEmpty() || authPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Введите логин и пароль", ButtonType.OK).showAndWait();
            return;
        }
        controller.sendAuthMessage(authLoginField.getText(), authPasswordField.getText());
    }

    public void openRegWindow(ActionEvent event) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("regPanel.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
