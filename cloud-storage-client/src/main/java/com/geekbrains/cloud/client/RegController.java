package com.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TextField;

public class RegController implements Initializable {

    public TextField regLoginField;
    public PasswordField regPasswordField;
    public TextField regNicknameField;

    private MainController controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controller = new MainController();
    }

    public void registration(ActionEvent actionEvent) throws IOException {
        try {
            if (regLoginField.getText().isEmpty() || regPasswordField.getText().isEmpty() || regNicknameField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Введите логин, пароль и никнейм", ButtonType.OK).
                        showAndWait();
                return;
            }
            controller.sendRegMessage(regLoginField.getText(), regPasswordField.getText(), regNicknameField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backToAuthPanel(ActionEvent actionEvent) {
        Platform.exit();
    }
}
