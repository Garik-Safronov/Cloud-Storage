package com.geekbrains.cloud.client;

import com.geekbrains.cloud.core.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class ClientController implements Initializable {

    public TextField clientPathField;
    public TextField serverPathField;
    public ListView<String> clientView;
    public ListView<String> serverView;

    private ObjectDecoderInputStream in;
    private ObjectEncoderOutputStream out;
    private Path clientDir;
    private Path serverDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get(System.getProperty("user.home"));
            serverDir = Paths.get("localserver");

            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream());

            updateClientPathField(clientDir);
            updateClientView();
            initMouseListeners();

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) in.readObject();
                log.info("received: {}", message);
                switch (message.getType()) {
                    case FILE:
                        processFileMessage((FileMessage) message);
                        break;
                    case LIST:
                        processListMessage((ListMessage) message);
                        break;
                    case PATH_RESPONSE:
                        updateServerPathField((PathResponse) message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processListMessage(ListMessage message) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(message.getFiles());
        });
    }

    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        out.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        out.writeObject(new FileRequest(fileName));
    }

    public void renameFile(ActionEvent actionEvent) {
    }

    public void createDir(ActionEvent actionEvent) {
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

    private void updateServerPathField(PathResponse message) {
        Platform.runLater(() -> {
            serverPathField.setText(message.getNewDir());
        });
    }
}