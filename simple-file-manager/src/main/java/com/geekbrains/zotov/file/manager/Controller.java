package com.geekbrains.zotov.file.manager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    ListView<FileInfo> filesList;
    @FXML
    TextField pathField;

    Path root;
    Path selecteCopyFile;
    Path selecteMoveFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        filesList.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> param) {
                return new ListCell<FileInfo>() {
                    @Override
                    protected void updateItem(FileInfo item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFileName = String.format("%-30s", item.getFileName());
                            String formattedFileLength = String.format("%,d bytes", item.getLength());

                            if (item.getLength() == -1L) {
                                formattedFileLength = String.format("%s", "[DIR]");
                            }
                            if (item.getLength() == -2L) {
                                formattedFileLength = "";
                            }
                            String text = String.format("%s %20s", formattedFileName, formattedFileLength);

                            setText(text);
                        }
                    }
                };
            }
        });
        goToPath(Paths.get("1"));
    }

    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }


    public void goToPath(Path path) {
        root = path;
        pathField.setText(root.toAbsolutePath().toString());
        filesList.getItems().clear();
        filesList.getItems().add(new FileInfo(FileInfo.UP_TOKEN, -2L));
        filesList.getItems().addAll(scanFiles(path));
        filesList.getItems().sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                if (o1.getFileName().equals(FileInfo.UP_TOKEN)) {
                    return -1;
                }
                if ((int) Math.signum(o1.getLength()) == (int) Math.signum(o2.getLength())) {
                    return o1.getFileName().compareTo(o2.getFileName());
                }

                return new Long(o1.getLength() - o2.getLength()).intValue();
            }
        });

    }


    public List<FileInfo> scanFiles(Path root) {
        try {
//            List<FileInfo> out = new ArrayList<>();
//            List<Path> pathInRoot = null;
//            pathInRoot = Files.list(root).collect(Collectors.toList());
//            for (Path p : pathInRoot) {
//                out.add(new FileInfo(p));
//            }
//
//            return out;
            return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Files scan exception" + root);
        }
    }

    public void filesListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
            if (fileInfo != null) {
                if (fileInfo.isDirectory()) {
                    Path pathTo = root.resolve(fileInfo.getFileName());
                    goToPath(pathTo);
                }
                if (fileInfo.isUpElement()) {
                    Path pathTo = root.toAbsolutePath().getParent();
                    goToPath(pathTo);
                }
            }
        }

    }


    public void refresh() {
        goToPath(root);
    }


    public void copyAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if (selecteCopyFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())) {
            return;
        }
        if (selecteCopyFile == null) {
            selecteCopyFile = root.resolve(fileInfo.getFileName());
            ((Button) actionEvent.getSource()).setText("Копируется: " + fileInfo.getFileName());
            return;
        }
        if (selecteCopyFile != null) {
            try {
                Files.copy(selecteCopyFile, root.resolve(selecteCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selecteCopyFile = null;
                ((Button) actionEvent.getSource()).setText("Копирование");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно скопировать файл");
                alert.showAndWait();
            }
        }
    }
    public void moveAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if (selecteMoveFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())) {
            return;
        }
        if (selecteMoveFile == null) {
            selecteMoveFile = root.resolve(fileInfo.getFileName());
            ((Button) actionEvent.getSource()).setText("Перемещается: " + fileInfo.getFileName());
            return;
        }
        if (selecteMoveFile != null) {
            try {
                Files.move(selecteMoveFile, root.resolve(selecteMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selecteMoveFile = null;
                ((Button) actionEvent.getSource()).setText("Перемещение");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно переместить файл");
                alert.showAndWait();
            }
        }
    }

    public void deleteAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement()) {
            return;
        }
        try {
            Files.delete(root.resolve(fileInfo.getFileName()));
            refresh();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить файл");
            alert.showAndWait();
        }
    }
}
