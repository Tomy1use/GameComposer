package de.mirkosertic.gameengine.type;

import java.util.HashMap;
import java.util.Map;

public class ResourceName implements ValueProvider<String> {

    public final String name;

    public ResourceName(String aName) {
        name = aName;
    }

    @Override
    public String get() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceName that = (ResourceName) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put("name", name);
        return theResult;
    }

    public static ResourceName deserialize(Map<String, Object> aSerializedData) {
        return new ResourceName((String) aSerializedData.get("name"));
    }
}
