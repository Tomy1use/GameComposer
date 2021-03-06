package de.mirkosertic.gameengine.arcade;

import de.mirkosertic.gameengine.action.SystemTick;
import de.mirkosertic.gameengine.core.Behavior;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;
import de.mirkosertic.gameengine.event.GameEventListener;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;
import de.mirkosertic.gameengine.type.Position;
import de.mirkosertic.gameengine.type.Reflectable;
import de.mirkosertic.gameengine.type.Speed;

import java.util.HashMap;
import java.util.Map;

public class ConstantMovementBehavior implements Behavior, ConstantMovement, Reflectable<ConstantMovementClassInformation> {

    static final String TYPE = "ConstantMovement";

    private final GameObjectInstance objectInstance;

    private final Property<Speed> speed;
    private final Property<Speed> rotationSpeed;

    ConstantMovementBehavior(GameObjectInstance aObjectInstance) {
        this(aObjectInstance, aObjectInstance.getOwnerGameObject().getComponentTemplate(ConstantMovementBehaviorTemplate.class));
    }

    ConstantMovementBehavior(GameObjectInstance aObjectInstance, ConstantMovementBehaviorTemplate aTemplate) {
        objectInstance = aObjectInstance;

        GameEventManager theEventManager = aObjectInstance.getOwnerGameObject().getGameScene().getRuntime().getEventManager();

        speed = new Property<Speed>(Speed.class, this, SPEED_PROPERTY, aTemplate.speedProperty().get(), theEventManager);
        rotationSpeed = new Property<Speed>(Speed.class, this, ROTATIONSPEED_PROPERTY, aTemplate.rotationSpeedProperty().get(), theEventManager);
    }

    public void registerEvents(GameRuntime aGameRuntime) {
        aGameRuntime.getEventManager().register(objectInstance, SystemTick.class, new GameEventListener<SystemTick>() {
            public void handleGameEvent(SystemTick aEvent) {
                handleGameLoop(aEvent);
            }
        });
    }

    private void handleGameLoop(SystemTick aTick) {

        Position theCurrentPosition = objectInstance.positionProperty().get();
        objectInstance.positionProperty().set(theCurrentPosition.translate(objectInstance.rotationAngleProperty().get(), speed.get().speed / 1000d * aTick.elapsedTimeSinceLastLoop));

        int theAngleDif = (int) (rotationSpeed.get().speed / 1000d * aTick.elapsedTimeSinceLastLoop);
        if (theAngleDif != 0) {
            objectInstance.rotationAngleProperty().set(objectInstance.rotationAngleProperty().get().add(theAngleDif));
        }
    }


    @Override
    public ConstantMovementClassInformation getClassInformation() {
        return ConstantMovementClassInformation.INSTANCE;
    }

    @Override
    public ConstantMovementBehaviorTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getComponentTemplate(ConstantMovementBehaviorTemplate.class);
    }

    @Override
    public Property<Speed> speedProperty() {
        return speed;
    }

    @Override
    public Property<Speed> rotationSpeedProperty() {
        return rotationSpeed;
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        theResult.put(SPEED_PROPERTY, speed.get().serialize());
        theResult.put(ROTATIONSPEED_PROPERTY, rotationSpeed.get().serialize());
        return theResult;
    }

    public static ConstantMovementBehavior deserialize(GameRuntime aRuntime, GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        ConstantMovementBehavior theResult = new ConstantMovementBehavior(aObjectInstance);
        theResult.speed.setQuietly(Speed.deserialize((Map<String, Object>) aSerializedData.get(SPEED_PROPERTY)));
        Map<String, Object> theRotationSpeed = (Map<String, Object>) aSerializedData.get(ROTATIONSPEED_PROPERTY);
        if (theRotationSpeed != null) {
            theResult.rotationSpeed.setQuietly(Speed.deserialize(theRotationSpeed));
        }
        theResult.registerEvents(aRuntime);
        return theResult;
    }
}
