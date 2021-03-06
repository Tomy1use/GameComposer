package de.mirkosertic.gameengine.camera;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.BehaviorTemplate;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;
import de.mirkosertic.gameengine.core.UsedByReflection;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;
import de.mirkosertic.gameengine.type.Reflectable;

public class CameraBehaviorTemplate implements BehaviorTemplate<CameraBehavior>, Camera, Reflectable<CameraClassInformation> {

    private final Property<CameraType> type;
    private final GameObject owner;

    @UsedByReflection
    public CameraBehaviorTemplate(GameEventManager aEventManager, GameObject aOwner) {
        type = new Property<CameraType>(CameraType.class, this, TYPE_PROPERTY, CameraType.FOLLOWPLAYER, aEventManager);
        owner = aOwner;
    }

    @Override
    public CameraClassInformation getClassInformation() {
        return CameraClassInformation.INSTANCE;
    }

    @Override
    public GameObject getOwner() {
        return owner;
    }

    public Property<CameraType> typeProperty() {
        return type;
    }

    public CameraBehavior create(GameObjectInstance aInstance, GameRuntime aGameRuntime) {
        final CameraBehavior theResult = new CameraBehavior(aInstance, this);
        theResult.registerEvents(aGameRuntime);
        return theResult;
    }

    @Override
    public String getTypeKey() {
        return CameraBehavior.TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(CameraBehavior.TYPE_ATTRIBUTE, CameraBehavior.TYPE);
        theResult.put("cameratype", type.get().name());
        return theResult;
    }

    public static CameraBehaviorTemplate deserialize(GameEventManager aEventManager, GameObject aOwner, Map<String, Object> aSerializedData) {
        CameraBehaviorTemplate theTemplate = new CameraBehaviorTemplate(aEventManager, aOwner);
        String theCameraType = (String) aSerializedData.get("cameratype");
        if (theCameraType != null) {
            theTemplate.type.setQuietly(CameraType.valueOf(theCameraType));
        }
        return theTemplate;
    }
}
