package de.mirkosertic.gamecomposer.objectinspector.gameobjectinstance;

import de.mirkosertic.gamecomposer.objectinspector.ObjectInspectorElementController;
import de.mirkosertic.gameengine.type.AbsolutePositionAnchor;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import de.mirkosertic.gamecomposer.PropertyBinder;
import de.mirkosertic.gamecomposer.ObjectSelectedEvent;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.type.Angle;
import de.mirkosertic.gameengine.type.Position;
import javafx.util.StringConverter;

public class GameObjectInstanceEditorController implements ObjectInspectorElementController {

    @FXML
    TextField nameTextField;

    @FXML
    TextField xTextField;

    @FXML
    TextField yTextField;

    @FXML
    TextField rotationTextField;

    @FXML
    CheckBox visible;

    @FXML
    CheckBox absolutePosition;

    @FXML
    ComboBox absolutePositionAnchor;

    @FXML
    Hyperlink jumpToObject;

    @Inject
    Event<Object> eventGateway;

    private Parent view;
    private GameObjectInstance object;

    @Override
    public void cleanup() {
        PropertyBinder.unbind(object.nameProperty());
        PropertyBinder.unbind(object.positionProperty());
        PropertyBinder.unbind(object.rotationAngleProperty());
        PropertyBinder.unbind(object.visibleProperty());
        PropertyBinder.unbind(object.absolutePositionProperty());
    }

    public GameObjectInstanceEditorController initialize(Parent aView, GameObjectInstance aObject) {
        view = aView;
        object = aObject;

        absolutePositionAnchor.getItems().clear();
        absolutePositionAnchor.getItems().addAll(AbsolutePositionAnchor.values());
        absolutePositionAnchor.getSelectionModel().select(object.absolutePositionAnchorProperty().get());
        absolutePositionAnchor.setConverter(new StringConverter<AbsolutePositionAnchor>() {
            @Override
            public String toString(AbsolutePositionAnchor aAnchor) {
                return aAnchor.name();
            }

            @Override
            public AbsolutePositionAnchor fromString(String aName) {
                return AbsolutePositionAnchor.valueOf(aName);
            }
        });
        absolutePositionAnchor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                object.absolutePositionAnchorProperty().set((AbsolutePositionAnchor) absolutePositionAnchor.getSelectionModel().getSelectedItem());
            }
        });


        PropertyBinder.bind(object.nameProperty(), nameTextField.textProperty());
        PropertyBinder.bind(object.positionProperty(), xTextField.textProperty(), new PropertyBinder.Converter<Position, String>() {
            @Override
            public String beanToUI(Position aValue) {
                return Integer.toString((int) aValue.x);
            }

            @Override
            public Position uiToBean(String aValue) {
                Position theCurrentPosition = object.positionProperty().get();
                return new Position(Integer.parseInt(aValue), theCurrentPosition.y);
            }
        });
        PropertyBinder.bind(object.positionProperty(), yTextField.textProperty(), new PropertyBinder.Converter<Position, String>() {
            @Override
            public String beanToUI(Position aValue) {
                return Integer.toString((int) aValue.y);
            }

            @Override
            public Position uiToBean(String aValue) {
                Position theCurrentPosition = object.positionProperty().get();
                return new Position(theCurrentPosition.x, Integer.parseInt(aValue));
            }
        });
        PropertyBinder.bind(object.rotationAngleProperty(), rotationTextField.textProperty(), new PropertyBinder.Converter<Angle, String>() {
            @Override
            public String beanToUI(Angle aValue) {
                return Integer.toString(aValue.angleInDegrees);
            }

            @Override
            public Angle uiToBean(String aValue) {
                return new Angle(Integer.parseInt(aValue));
            }
        });
        PropertyBinder.bind(object.visibleProperty(), visible.selectedProperty());
        PropertyBinder.bind(object.absolutePositionProperty(), absolutePosition.selectedProperty());

        rotationTextField.setText(Integer.toString(aObject.rotationAngleProperty().get().angleInDegrees));

        jumpToObject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                eventGateway.fire(new ObjectSelectedEvent(object.getOwnerGameObject()));
            }
        });

        return this;
    }

    @Override
    public Node getView() {
        return view;
    }
}