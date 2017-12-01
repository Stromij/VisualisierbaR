package com.github.bachelorpraktikum.visualisierbar.view.sourcechooser;

import com.github.bachelorpraktikum.visualisierbar.config.ConfigKey;
import com.github.bachelorpraktikum.visualisierbar.datasource.AbsSource;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@ParametersAreNonnullByDefault
public class AbsChooserController implements SourceChooser<AbsSource>{

    @FXML
    private TextField absFileURIField;
    @FXML
    private TextField productNameField;

    @FXML
    private Pane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;


    private ReadOnlyObjectWrapper<URI> fileUriProperty;
    private BooleanBinding fileChosen;


    @FXML
    private void initialize() {

        fileUriProperty = new ReadOnlyObjectWrapper<>();
        fileChosen = fileUriProperty.isNotNull();
        fileChooser = new FileChooser();

        //Initalisiere den FileChooser
        fileChooser = new FileChooser();
        String initialDirectory = getInitialDirectory();
        fileChooser.setInitialDirectory(new File(initialDirectory));

        // Füge dem Durchsuchen-Button einen Eventlistener hinzu und öffne das Durchsuchen-Fenster bei Auslösung
        explorerButton.setOnAction(event -> updatePath(openFileChooser()));
        explorerButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                explorerButton.fire();
            }
        });


        // Add a Listener to the URI-Field
        absFileURIField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                fileUriProperty.set(null);
                return;
            }
            setInitialDirectory(newValue);
            URI uri = new File(newValue).getAbsoluteFile().toURI();
            fileUriProperty.set(uri);
        });


        // Add an EventHandler to the URI-Field: By pressing Enter it schould fire the "Open Source" Button
        absFileURIField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ((Button) rootPane.getScene().lookup("#openSource")).fire();
            }
        });

        // Add an EventHandler to the productName-Field: By pressing Enter it schould fire the "Open Source" Button
        productNameField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ((Button) rootPane.getScene().lookup("#openSource")).fire();
            }
        });

    }

    /**
     * Updates the text in the path field with the absolute path of the <code>file</code>.
     *
     * @param file File to get the path from
     */
    private void updatePath(@Nullable File file) {
        if (file == null) {
            return;
        }

        absFileURIField.setText(file.getAbsolutePath());
    }


    /**
     * Opens the system explorer where a file can be chosen.
     * If a file was chosen the {@link File file} will be returned.
     * If no file was chosen and the explorer has been closed, null will be returned.
     *
     * @return A {@link File file} or null
     */
    @Nullable
    private File openFileChooser() {
        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    /**
     * Tries to retrieve the initial directory from the configuration file.
     * If that fails, the $HOME directory will be used.
     *
     * @return Initial directory for the file chooser
     */
    private String getInitialDirectory() {
        String path = ConfigKey.initialLogFileDirectory.get(System.getProperty("user.home"));
        if (!new File(path).isDirectory()) {
            return System.getProperty("user.home");
        }
        return path;
    }


    /**
     * Tries to (re)set the initial directory in the configuration file.
     * If the given <code>path</code> is not a path it will not be set in the configuration file
     *
     * @param path New path to be set as Initial Directory
     */
    private void setInitialDirectory(String path) {
        File file = new File(path);
        if (file.isFile()) {
            path = file.getParent();
            ConfigKey.initialLogFileDirectory.set(path);
            fileChooser.setInitialDirectory(new File(path));
        }
    }

    @Nonnull
    @Override
    public AbsSource getResource() throws IOException {
        return new AbsSource(new File(fileUriProperty.get().toURL().getFile()), productNameField.getText());
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {return fileChosen;}
}