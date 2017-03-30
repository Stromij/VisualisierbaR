package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button closeButton;
    @FXML
    private Button openButton;
    @FXML
    private BorderPane rootPane;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    private ObjectProperty<String> userProperty;
    private ObjectProperty<String> passwordProperty;
    private boolean manuallyClosed;

    @FXML
    public void initialize() {
        userProperty = new SimpleObjectProperty<>();
        passwordProperty = new SimpleObjectProperty<>();

        userField.textProperty().bindBidirectional(userProperty);
        passwordField.textProperty().bindBidirectional(passwordProperty);

        passwordProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && userProperty.getValue() != null) {
                openButton.setDisable(false);
            }
        });
        userProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && passwordProperty.getValue() != null) {
                openButton.setDisable(false);
            }
        });

        openButton.setOnAction(event -> close(false));
        closeButton.setOnAction(event -> close(true));
    }

    Button getOpenButton() {
        return openButton;
    }

    public String getUser() {
        return Optional.ofNullable(userProperty.getValue()).orElse("");
    }

    public String getPassword() {
        return Optional.ofNullable(passwordProperty.getValue()).orElse("");
    }

    void setStage(Stage stage) {
        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public void show() {
        Stage stage = new Stage();
        Scene scene = new Scene(rootPane);

        stage.setScene(scene);
        stage.showAndWait();
    }

    public void close(boolean manualClose) {
        manuallyClosed = manualClose;
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    boolean manuallyClosed() {
        return manuallyClosed || openButton.isDisabled();
    }
}
