package com.geekbrains.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateDirController implements Initializable {

    public TextField newDirNameTextField;
    public String newDirName;

    MainController controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controller = new MainController();
    }

    public void mkdirButton(ActionEvent actionEvent) throws IOException {
        if (!newDirNameTextField.getText().isEmpty()) {
            newDirName = newDirNameTextField.getText();
            closeCreateDirWindow();
            controller.createDirOnStorage(newDirName);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Укажите новое имя файла", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void cancelButton(ActionEvent actionEvent) {
        closeCreateDirWindow();
    }

    public void closeCreateDirWindow() {
        Stage stage = (Stage) newDirNameTextField.getScene().getWindow();
        stage.close();
    }
}
