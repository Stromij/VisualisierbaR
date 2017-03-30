package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.database.Database;
import com.github.bachelorpraktikum.dbvisualization.database.DatabaseUser;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import com.github.bachelorpraktikum.dbvisualization.datasource.DatabaseSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.Nonnull;

public class DatabaseChooserController implements SourceChooser<DataSource> {

    private static final int DEFAULT_SQL_PORT = 3306;
    private static final String DEFAULT_PROTOCOL = "http";
    private static final int INVALID_PORT = -1;
    @FXML
    private BorderPane rootPaneDatabase;
    @FXML
    private TextField databaseURIField;
    @FXML
    private TextField databaseNameField;
    @FXML
    private TextField portField;
    @FXML
    public Label uriError;

    private ReadOnlyObjectWrapper<String> databaseURIProperty;
    private ReadOnlyObjectWrapper<String> databaseNameProperty;
    private IntegerProperty portProperty;
    private ReadOnlyObjectWrapper<URI> completeURIProperty;
    private ObservableBooleanValue uriChosen;
    private boolean closed;

    @FXML
    public void initialize() {
        databaseURIProperty = new ReadOnlyObjectWrapper<>();
        databaseNameProperty = new ReadOnlyObjectWrapper<>();
        portProperty = new SimpleIntegerProperty(INVALID_PORT);
        completeURIProperty = new ReadOnlyObjectWrapper<>();
        uriChosen = completeURIProperty.isNotNull();

        databaseURIField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseURIProperty.set(null);
            } else {
                databaseURIProperty.set(newValue.trim());
            }
            check();
        });

        databaseNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            databaseNameProperty.set(newValue);
            check();
        });

        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            newValue = newValue.trim();
            try {
                int port = Integer.parseUnsignedInt(newValue);
                portProperty.set(port);
            } catch (NumberFormatException ignored) {
                portProperty.set(INVALID_PORT);
            }
            check();
        });

        loadInitialValues();
        closed = false;
    }

    private void loadInitialValues() {
        String uriString = ConfigKey.initialDatabaseUri.get();
        String portString = ConfigKey.initialDatabasePort.get();
        String nameString = ConfigKey.initialDatabaseName.get();
        if (uriString != null && portString != null && nameString != null) {
            int port = DEFAULT_SQL_PORT;
            try {
                port = Integer.parseUnsignedInt(portString);
            } catch (NumberFormatException e) {
                String message = String.format("Couldn't parse port to number: %s", e.getMessage());
                Logger.getLogger(getClass().getName()).info(message);
            }
            nameString = stripLeadingSlash(nameString);
            URI uri = createCompleteURI(uriString, port, nameString);

            completeURIProperty.set(uri);
            databaseURIField.setText(uri.getHost());
            String path = uri.getPath();
            path = stripLeadingSlash(path);
            databaseNameField.setText(path);
            portField.setText(String.valueOf(uri.getPort()));
        }

        if (portField.getText().trim().isEmpty() || Objects.equals(portField.getText(), "-1")) {
            if (portString == null || portString.trim().isEmpty()) {
                portString = String.valueOf(DEFAULT_SQL_PORT);
            }
            portField.setText(String.valueOf(portString));
        }
    }

    private void check() {
        URI uri = null;
        if (databaseURIProperty.get() != null
            && databaseNameProperty.get() != null && !databaseNameProperty.get().trim().isEmpty()
            && portProperty.get() != INVALID_PORT) {
            String uriString = databaseURIProperty.get();
            uri = createCompleteURI(uriString, portProperty.get(),
                databaseNameProperty.get());
            if (uri != null) {
                setInitialUri(uri);
            }
        }
        completeURIProperty.set(uri);
        uriError.setVisible(uri == null);
    }

    private URI createCompleteURI(String url, int port, String path) {
        String uriString = String.format("%s:%d/%s", url, port, path);
        uriString = prependScheme(uriString);
        URI uri = null;

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            String message = String.format("Couldn't create uri after check: %s", uriString);
            Logger.getLogger(getClass().getName()).severe(message);
        }

        return uri;
    }

    private String prependScheme(String uri) {
        if (!uri.contains("://")) {
            uri = String.format("%s://%s", DEFAULT_PROTOCOL, uri);
        }

        return uri;
    }

    private String stripLeadingSlash(@Nonnull String uriString) {
        if (uriString.startsWith("/")) {
            uriString = uriString.replaceFirst("/", "");
        }

        return uriString;
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {
        return uriChosen;
    }

    @Nonnull
    @Override
    public DataSource getResource() throws IOException {
        Stage stage = ((Stage) rootPaneDatabase.getScene().getWindow());
        Database database = createDatabase(stage);
        return new DatabaseSource(database, null);
    }

    @Nonnull
    private Database createDatabase(Stage stage) throws IOException {
        stage.setOnHiding(event -> closed = true);

        Database database = null;
        DatabaseUser user = null;
        boolean loginWasClosed = false;
        while (database == null && !closed && !loginWasClosed) {
            try {
                if (user == null) {
                    database = new Database(completeURIProperty.get());
                } else {
                    database = new Database(completeURIProperty.get(), user);
                }
            } catch (PoolInitializationException e) {
                Logger.getLogger(getClass().getName())
                    .info(String.format("Couldn't connect to db: %s", e.getCause()));
            } finally {
                if (database == null && !closed) {
                    user = showLoginWindow();
                    loginWasClosed = user == null;
                }
            }
        }
        if (database == null) {
            throw new IOException(
                "Session was closed by user before a valid connection could be established. Can't read from database with current configuration.");
        }

        return database;
    }

    private void setInitialUri(@Nonnull URI uri) {
        if (uri.getHost() != null) {
            ConfigKey.initialDatabaseUri.set(uri.getHost());
        }
        ConfigKey.initialDatabasePort.set(String.valueOf(uri.getPort()));
        ConfigKey.initialDatabaseName.set(stripLeadingSlash(uri.getPath()));
    }


    private DatabaseUser showLoginWindow() throws IOException {
        Dialog dia = new Dialog<>();
        dia.initModality(Modality.APPLICATION_MODAL);
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        Parent root = loader.load();
        final Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

        LoginController controller = loader.getController();
        if (controller.manuallyClosed()) {
            return null;
        }

        return new DatabaseUser(controller.getUser(), controller.getPassword());
    }
}
