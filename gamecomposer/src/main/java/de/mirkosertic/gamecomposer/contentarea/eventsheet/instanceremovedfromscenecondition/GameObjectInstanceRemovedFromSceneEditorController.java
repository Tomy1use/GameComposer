package de.mirkosertic.gamecomposer.contentarea.eventsheet.instanceremovedfromscenecondition;

import de.mirkosertic.gamecomposer.Controller;
import de.mirkosertic.gamecomposer.StringConverterFactory;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstanceRemovedFromSceneCondition;
import de.mirkosertic.gameengine.core.GameScene;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameObjectInstanceRemovedFromSceneEditorController implements Controller {

    @Inject
    StringConverterFactory stringConverterFactory;

    @FXML
    ComboBox objectType;

    private GameScene gameScene;
    private GameObjectInstanceRemovedFromSceneCondition condition;
    private Node view;

    GameObjectInstanceRemovedFromSceneEditorController initialize(Node aView, GameScene aGameScene, GameObjectInstanceRemovedFromSceneCondition aCondition) {
        gameScene = aGameScene;
        view = aView;
        condition = aCondition;

        List<GameObject> theSortedGameObjects = Arrays.asList(aGameScene.getObjects());
        Collections.sort(theSortedGameObjects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject o1, GameObject o2) {
                return o1.nameProperty().get().compareTo(o2.nameProperty().get());
            }
        });


        objectType.getItems().clear();
        objectType.getItems().addAll(theSortedGameObjects);
        objectType.setConverter(stringConverterFactory.createGameObjectStringConverter());
        objectType.getSelectionModel().select(aCondition.gameObjectProperty().get());

        objectType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onObjectTypeSelected();
            }
        });

        return this;
    }

    public Node getView() {
        return view;
    }

    private void onObjectTypeSelected() {
        condition.gameObjectProperty().set((GameObject) objectType.getSelectionModel().getSelectedItem());
    }
}
