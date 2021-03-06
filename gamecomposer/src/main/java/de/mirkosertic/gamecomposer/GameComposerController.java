package de.mirkosertic.gamecomposer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.prefs.Preferences;

@Singleton
public class GameComposerController {

    private static final String GAME_DIRECTORY_PREF_KEY = "GameDirectory";
    private static final String GAME_EXPORTDIRECTORY_HTML_PREF_KEY = "GameExportDirectoryHTML";
    private static final String GAME_EXPORTDIRECTORY_ANDROID_PREF_KEY = "GameExportDirectoryAndroid";

    @Inject
    @ObjectInspector
    Controller objectInspector;

    @Inject
    @ContentArea
    Controller contentArea;

    @Inject
    @ProjectStructure
    Controller projectStructure;

    @Inject
    Event<Object> eventGateway;

    @FXML
    HBox childViews;

    @FXML
    Menu exportMenu;

    @FXML
    Label statusBar;

    @Inject
    Event<StatusEvent> statusEvent;

    private Preferences directoryPreferences;
    private Stage stage;

    public void initialize(Stage aStage) {

        stage = aStage;

        directoryPreferences = Preferences.userNodeForPackage(GameComposerController.class);
        exportMenu.setDisable(true);

        childViews.getChildren().add(objectInspector.getView());
        childViews.getChildren().add(contentArea.getView());
        childViews.getChildren().add(projectStructure.getView());

        HBox.setHgrow(objectInspector.getView(), Priority.SOMETIMES);
        HBox.setHgrow(contentArea.getView(), Priority.ALWAYS);
        HBox.setHgrow(projectStructure.getView(), Priority.SOMETIMES);

        eventGateway.fire(new ApplicationStartedEvent());
    }

    @FXML
    public void onClose() {
        try {
            directoryPreferences.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    @FXML
    public void onNew() {

        DirectoryChooser theDirectoryChooser = new DirectoryChooser();
        theDirectoryChooser.setTitle("Choose target directory");
        File theProjectDirectory = theDirectoryChooser.showDialog(stage);
        if (theProjectDirectory != null) {
            eventGateway.fire(new NewGameEvent(theProjectDirectory));

            directoryPreferences.put(GAME_DIRECTORY_PREF_KEY, theProjectDirectory.toString());
            exportMenu.setDisable(false);

            statusEvent.fire(new StatusEvent("New game created", StatusEvent.Severity.INFO));
        }
    }

    @FXML
    public void onSave() {
        eventGateway.fire(new SaveGameEvent());
        exportMenu.setDisable(false);
    }

    @FXML
    public void onOpen() {
        FileChooser theFileChooser = new FileChooser();
        theFileChooser.setTitle("Open game");

        String theLastDirectory = directoryPreferences.get(GAME_DIRECTORY_PREF_KEY, null);
        if (theLastDirectory != null) {
            File theDirectory = new File(theLastDirectory);
            if (theDirectory.isFile()) {
                theDirectory = theDirectory.getParentFile();
            }
            if (theDirectory.exists()) {
                theFileChooser.setInitialDirectory(theDirectory);
            }
        }

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Game files (game.json)", "game.json");
        theFileChooser.getExtensionFilters().add(extFilter);

        File theSelectedFile = theFileChooser.showOpenDialog(null);
        if (theSelectedFile != null) {
            eventGateway.fire(new LoadGameEvent(theSelectedFile));

            directoryPreferences.put(GAME_DIRECTORY_PREF_KEY, theSelectedFile.getParentFile().toString());
            exportMenu.setDisable(false);

            statusEvent.fire(new StatusEvent("Game loaded", StatusEvent.Severity.INFO));
        }
    }

    @FXML
    public void onSaveAndExportHTML5() {
        DirectoryChooser theDirectoryChooser = new DirectoryChooser();
        theDirectoryChooser.setTitle("Choose target directory");

        String theLastExportDir = directoryPreferences.get(GAME_EXPORTDIRECTORY_HTML_PREF_KEY, null);
        if (theLastExportDir != null) {
            theDirectoryChooser.setInitialDirectory(new File(theLastExportDir));
        }

        File theTargetDirectory = theDirectoryChooser.showDialog(stage);
        if (theTargetDirectory != null) {
            eventGateway.fire(new ExportGameHTML5Event(theTargetDirectory));

            directoryPreferences.put(GAME_EXPORTDIRECTORY_HTML_PREF_KEY, theTargetDirectory.toString());
        }
    }

    @FXML
    public void onSaveAndExportAndroid() {
        DirectoryChooser theDirectoryChooser = new DirectoryChooser();
        theDirectoryChooser.setTitle("Choose target directory");

        String theLastExportDir = directoryPreferences.get(GAME_EXPORTDIRECTORY_ANDROID_PREF_KEY, null);
        if (theLastExportDir != null) {
            theDirectoryChooser.setInitialDirectory(new File(theLastExportDir));
        }

        File theTargetDirectory = theDirectoryChooser.showDialog(stage);
        if (theTargetDirectory != null) {
            eventGateway.fire(new ExportGameAndroidEvent(theTargetDirectory));

            directoryPreferences.put(GAME_EXPORTDIRECTORY_ANDROID_PREF_KEY, theTargetDirectory.toString());
        }
    }

    public void handleStatus(final @Observes StatusEvent aStatusEvent) {
        Runnable theRunnable = new Runnable() {
            @Override
            public void run() {
                switch (aStatusEvent.getSeverity()) {
                    case ERROR:
                        statusBar.setTextFill(Color.RED);
                        break;
                    case INFO:
                        statusBar.setTextFill(Color.BLACK);
                        break;
                }
                statusBar.setText(aStatusEvent.getMessage());
            }
        };
        if (Platform.isFxApplicationThread()) {
            theRunnable.run();
        } else {
            Platform.runLater(theRunnable);
        }
    }
}