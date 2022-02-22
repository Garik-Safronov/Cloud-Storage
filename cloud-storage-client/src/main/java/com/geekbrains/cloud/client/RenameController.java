package com.geekbrains.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RenameController implements Initializable {

    @FXML
    public TextField newFileNameTextField;
    public String newFileName;

    MainController controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controller = new MainController();
    }

    public void renameButton(ActionEvent actionEvent) throws IOException {
        if (!newFileNameTextField.getText().isEmpty()) {
            newFileName = newFileNameTextField.getText();
            closeRenameWindow();
            controller.renameFile(newFileName);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Укажите новое имя файла", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void cancelButton(ActionEvent actionEvent) {
        closeRenameWindow();
    }

    public void closeRenameWindow() {
        Stage stage = (Stage) newFileNameTextField.getScene().getWindow();
        stage.close();
    }
}
