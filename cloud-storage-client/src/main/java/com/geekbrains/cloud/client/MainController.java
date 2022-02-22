package com.geekbrains.cloud.client;

import com.geekbrains.cloud.core.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable {

    public TextField clientPathField;
    public TextField serverPathField;
    public ListView<String> clientView;
    public ListView<String> serverView;

    private Socket socket;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;

    public Path clientDir;
    public Path serverDir;
    public RenameController renameController;
    public CreateDirController createDirController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream());

            renameController = new RenameController();

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

            clientDir = Paths.get(System.getProperty("user.home"));

            updateClientPathField(clientDir);
            updateClientView();
            initMouseListeners();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) in.readObject();
                log.info("received: {}", message);
                switch (message.getType()) {
                    case AUTH_OK:
                        processAuthConfirmMessage((AuthConfirm) message);
                        break;
                    case AUTH_ERROR:
                        new Alert(Alert.AlertType.ERROR, "Неверные логин или пароль!", ButtonType.OK).showAndWait();
                        break;
                    case REG_OK:
                        new Alert(Alert.AlertType.CONFIRMATION, "Успешная регистрация", ButtonType.OK).showAndWait();
                        openWindow("authPanel.fxml");
                        break;
                    case REG_ERROR:
                        new Alert(Alert.AlertType.ERROR, "Ошибка регистрации", ButtonType.OK).showAndWait();
                        break;
                    case FILE:
                        processFileMessage((FileMessage) message);
                        break;
                    case LIST:
                        updateServerView((ListMessage) message);
                        break;
                    case PATH_RESPONSE:
                        updateServerPathField((PathResponse) message);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAuthMessage(String login, String password) throws IOException {
        out.writeObject(new AuthMessage(login, password));
        out.flush();
    }

    private void processAuthConfirmMessage(AuthConfirm message) {
    }

    public void sendRegMessage(String login, String password, String nickname) throws IOException {
        out.writeObject(new RegMessage(login, password, nickname));
        out.flush();
    }

    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    public void uploadButton(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        out.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }

    public void downloadButton(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        out.writeObject(new FileRequest(fileName));
    }

    public void renameFileButton(ActionEvent actionEvent) throws IOException {
        String oldFileName = serverView.getSelectionModel().getSelectedItem();
        openWindow("renamePanel.fxml");
        renameController.newFileNameTextField.setText(oldFileName);
    }

    public void renameFile(String newFileName) throws IOException {
        String oldFileName = serverView.getSelectionModel().getSelectedItem();
        out.writeObject(new RenameMessage(oldFileName, newFileName));
    }

    public void createDirButton(ActionEvent actionEvent) throws IOException {
        openWindow("createDirPanel.fxml");
    }

    public void createDirOnStorage(String newDirName) throws IOException {
        out.writeObject(new CreateDirMessage(createDirController.newDirNameTextField.getText()));
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        out.writeObject(new DeleteMessage(fileName));
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateServerView(ListMessage message) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(message.getFiles());
        });
    }

    private void initMouseListeners() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(clientView.getSelectionModel().getSelectedItem());
                if (Files.isDirectory(current)) {
                    clientDir = current;
                    Platform.runLater(this::updateClientView);
                    updateClientPathField(clientDir);
                }
            }
        });
        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String targetDir = serverView.getSelectionModel().getSelectedItem();
                try {
                    out.writeObject(new UpdatePathRequest(targetDir));
                    serverDir = Paths.get(targetDir);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void clientPathUpButton(ActionEvent actionEvent) {
        clientDir = clientDir.getParent().normalize();
        if (clientDir != null) {
            updateClientPathField(clientDir);
            updateClientView();
        }
    }

    public void updateClientPathField(Path path) {
        Platform.runLater(() -> {
            clientPathField.setText(path.normalize().toString());
        });
    }

    public void severPathUpButton(ActionEvent actionEvent) throws IOException {
        out.writeObject(new PathUpRequest());
    }

    public void updateServerPathField(PathResponse message) {
        Platform.runLater(() -> {
            serverPathField.setText(message.getNewDir());
        });
    }

    public void openWindow(String fxml) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root1 = loader.load();

                if (fxml.equals("authPanel.fxml")) {
                    AuthController authController = loader.getController();
                }
                if (fxml.equals("renamePanel.fxml")) {
                    RenameController renameController = loader.getController();
                }
                if (fxml.equals("createDirPanel.fxml")) {
                    CreateDirController createDirController = loader.getController();
                }
//                MainController controller = loader.getController();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root1));
                stage.resizableProperty().set(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void openAuthWindow(ActionEvent actionEvent) {
        openWindow("authPanel.fxml");
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }
}