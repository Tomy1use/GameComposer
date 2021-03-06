package de.mirkosertic.gameengine.type;

import java.util.HashMap;
import java.util.Map;

public class Position implements Reflectable<PositionClassInformation> {

    public final float x;
    public final float y;

    public Position() {
        this(0f, 0f);
    }

    public Position(float aX, float aY) {
        x = aX;
        y = aY;
    }

    public Position(double aX, double aY) {
        x = (float) aX;
        y = (float) aY;
    }

    @Override
    public PositionClassInformation getClassInformation() {
        return PositionClassInformation.INSTANCE;
    }

    public Position add(Position aOffset) {
        return new Position(x + aOffset.x, y + aOffset.y);
    }

    public Position changeX(Float aNewX) {
        return new Position(aNewX, y);
    }

    public Position changeY(Float aNewY) {
        return new Position(x, aNewY);
    }

    public Position snapToGrid(int aGridWidth, int aGridHeight) {
        float theSnapX = x - x % aGridWidth;
        float theSnapY = y - y % aGridHeight;
        return new Position(theSnapX, theSnapY);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put("x", Float.toString(x));
        theResult.put("y", Float.toString(y));
        return theResult;
    }

    public static Position deserialize(Map<String, Object> aSerializedStructure) {
        float theX = Float.valueOf((String) aSerializedStructure.get("x"));
        float theY = Float.valueOf((String) aSerializedStructure.get("y"));
        return new Position(theX, theY);
    }

    public Position translate(Angle aAngle, double aDistance) {
        float theRadians = aAngle.toRadians();
        aDistance = 1;
        double theMX = Math.sin(theRadians) * aDistance;
        double theMY = Math.cos(theRadians) * aDistance;
        return new Position(x + theMX, y - theMY);
    }
}
