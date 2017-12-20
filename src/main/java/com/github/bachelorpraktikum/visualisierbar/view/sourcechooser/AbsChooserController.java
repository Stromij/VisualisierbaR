package com.github.bachelorpraktikum.visualisierbar.view.sourcechooser;

import com.github.bachelorpraktikum.visualisierbar.config.ConfigKey;
import com.github.bachelorpraktikum.visualisierbar.datasource.AbsSource;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

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
    private DirectoryChooser directoryChooser;


    private ReadOnlyObjectWrapper<URI> fileUriProperty;
    private ReadOnlyObjectWrapper<String> productProperty;
    private ReadOnlyObjectWrapper<String> completeProperty;
    private ObservableBooleanValue fileChosen;


    @FXML
    private void initialize() {

        fileUriProperty = new ReadOnlyObjectWrapper<>();
        productProperty = new ReadOnlyObjectWrapper<>();
        completeProperty = new ReadOnlyObjectWrapper<>();
        fileChosen = completeProperty.isNotNull();

        //Initalisiere den FileChooser
        directoryChooser = new DirectoryChooser();
        String initialDirectory = getInitialDirectory();
        directoryChooser.setInitialDirectory(new File(initialDirectory));

        // Füge dem Durchsuchen-Button einen Eventlistener hinzu und öffne das Durchsuchen-Fenster bei Auslösung
        explorerButton.setOnAction(event -> updatePath(openDirectoryChooser()));
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
            check();
        });

        // Add a Listener to the Product-Name-Field
        productNameField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                productProperty.set(null);
            } else {
                productProperty.set(newValue.trim());
            }
            check();
        });


        // Add an EventHandler to the URI-Field: By hitting Enter it schould fire the "Open Source" Button
        absFileURIField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ((Button) rootPane.getScene().lookup("#openSource")).fire();
            }
        });

        // Add an EventHandler to the productName-Field: By hitting Enter it schould fire the "Open Source" Button
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
    private File openDirectoryChooser() {
        return directoryChooser.showDialog(rootPane.getScene().getWindow());
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
        if (file.isDirectory()) {
            ConfigKey.initialLogFileDirectory.set(path);
            directoryChooser.setInitialDirectory(new File(path));
        }
    }

    @Nonnull
    @Override
    public AbsSource getResource() throws IOException {
        return new AbsSource(completeProperty.get(), new File(fileUriProperty.getValue().getPath()), this.productProperty.getValue());
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {return fileChosen;}


    private void check(){
        String command = null;

        if(fileUriProperty.get() != null && productProperty.get() != null)
            {String parent = new File(fileUriProperty.getValue().getPath()).getParentFile().toString();
             command = String.format("absc -v -product=%s -erlang %s*.abs -d %s/gen/erlang/",
                    productProperty.get(), fileUriProperty.getValue().getPath(), parent);}

        completeProperty.set(command);
    }
}