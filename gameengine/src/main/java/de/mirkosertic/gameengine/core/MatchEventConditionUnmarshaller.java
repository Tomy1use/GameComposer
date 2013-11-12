package de.mirkosertic.gameengine.core;

import java.util.Map;

public class MatchEventConditionUnmarshaller implements ConditionUnmarshaller {

    @Override
    public String getTypeKey() {
        return MatchEventCondition.TYPE_VALUE;
    }

    @Override
    public Condition unmarshall(Map<String, Object> aSerializedData) {
        return MatchEventCondition.unmarshall(aSerializedData);
    }
}