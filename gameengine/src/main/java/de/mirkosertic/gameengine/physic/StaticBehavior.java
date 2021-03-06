package de.mirkosertic.gameengine.physic;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.Behavior;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.type.Reflectable;

public class StaticBehavior implements Behavior, Static, Reflectable<StaticClassInformation> {
    
    static final String TYPE = "Static";

    private final GameObjectInstance objectInstance;

    StaticBehavior(GameObjectInstance aObjectInstance) {
        objectInstance = aObjectInstance;
    }

    @Override
    public StaticClassInformation getClassInformation() {
        return StaticClassInformation.INSTANCE;
    }

    @Override
    public String getTypeKey() {
        return TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(TYPE_ATTRIBUTE, TYPE);
        return theResult;
    }

    @Override
    public StaticBehaviorTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getComponentTemplate(StaticBehaviorTemplate.class);
    }

    public static StaticBehavior deserialize(GameObjectInstance aObjectInstance) {
        return new StaticBehavior(aObjectInstance);
    }
}
