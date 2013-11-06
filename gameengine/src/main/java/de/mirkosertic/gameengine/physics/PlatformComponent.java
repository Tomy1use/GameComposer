package de.mirkosertic.gameengine.physics;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.*;

public class PlatformComponent implements GameComponent {

    public static String TYPE = "PlatformComponent";

    private GameRuntime gameRuntime;
    private GameObjectInstance objectInstance;
    private PlatformComponentTemplate platformTemplate;

    private boolean jumping;

    PlatformComponent(GameObjectInstance aObjectInstance, GameRuntime aGameRuntime) {
        gameRuntime = aGameRuntime;
        objectInstance = aObjectInstance;
        platformTemplate = aObjectInstance.getOwnerGameObject().getComponentTemplate(PlatformComponentTemplate.class);
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    public void registerEvents(GameRuntime aGameRuntime) {
        aGameRuntime.getEventManager().register(objectInstance, KeyPressedGameEvent.class, new GameEventListener<KeyPressedGameEvent>() {
            public void handleGameEvent(KeyPressedGameEvent aEvent) {
                handleKeyPressed(aEvent);
            }
        });
        aGameRuntime.getEventManager().register(objectInstance, KeyReleasedGameEvent.class, new GameEventListener<KeyReleasedGameEvent>() {
            public void handleGameEvent(KeyReleasedGameEvent aEvent) {
                handleKeyReleased(aEvent);
            }
        });
        aGameRuntime.getEventManager().register(objectInstance, GameObjectCollisionEvent.class, new GameEventListener<GameObjectCollisionEvent>() {
            public void handleGameEvent(GameObjectCollisionEvent aEvent) {
                handleCollision(aEvent);
            }
        });
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void handleKeyPressed(KeyPressedGameEvent aEvent) {
        if (aEvent.getKeyCode() == platformTemplate.getMoveLeftKey()) {
            gameRuntime.getEventManager().fire(new ApplyForceToGameObjectInstance(objectInstance, -platformTemplate.getLeftRightImpulse(), 0));
        }
        if (aEvent.getKeyCode() == platformTemplate.getMoveRightKey()) {
            gameRuntime.getEventManager().fire(new ApplyForceToGameObjectInstance(objectInstance, platformTemplate.getLeftRightImpulse(), 0f));
        }
        if (aEvent.getKeyCode() == platformTemplate.getJumpKey() && !isJumping()) {
            gameRuntime.getEventManager().fire(new ApplyImpulseToGameObjectInstance(objectInstance, 0, platformTemplate.getJumpImpulse()));
            setJumping(true);
        }
    }

    public void handleKeyReleased(KeyReleasedGameEvent aEvent) {
        // Reset Acceleration to zero ?
    }

    public void handleCollision(GameObjectCollisionEvent aEvent) {
        GameObjectInstance theOtherInstance = aEvent.getOtherInstanceOrNullIfNotAffected(objectInstance);
        if (theOtherInstance != null && theOtherInstance.getComponent(StaticComponent.class) != null) {
            double my = theOtherInstance.getPosition().y - objectInstance.getPosition().y;
            if (my > 0) {
                // Object is under the game object
                setJumping(false);
            } else {
                double mx = theOtherInstance.getPosition().x - objectInstance.getPosition().x;
                // Object is on the right side
                if (mx > 0) {
                    setJumping(false);
                }
                // Object is on the left side
                if (mx < 0) {
                    setJumping(false);
                }
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        return theResult;
    }

    public static PlatformComponent deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData, GameRuntime aGameRuntime) {
        PlatformComponent theComponent = new PlatformComponent(aObjectInstance, aGameRuntime);
        theComponent.registerEvents(aGameRuntime);
        return theComponent;
    }
}