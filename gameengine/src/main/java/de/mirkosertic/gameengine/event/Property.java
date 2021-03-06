package de.mirkosertic.gameengine.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Property<T> extends ReadOnlyProperty<T> {

    private final Set<GameEventListener<PropertyChanged>> changeListener;

    public Property(Class<T> aType, Object aOwner, String aName, T aDefaultValue, GameEventListener<PropertyChanged> aListener) {
        super(aType, aOwner, aName, aDefaultValue);
        changeListener = new HashSet<GameEventListener<PropertyChanged>>();
        if (aListener != null) {
            changeListener.add(aListener);
        }
    }

    public Property(Class<T> aType, Object aOwner, String aName, GameEventListener<PropertyChanged> aListener) {
        this(aType, aOwner, aName, null, aListener);
    }

    public Property(Class<T> aType, Object aOwner, String aName, T aDefaultValue) {
        this(aType, aOwner, aName, aDefaultValue, null);
    }

    public void set(T aValue) {
        //TODO: maybe we can implement type checking here?
        T theOldValue = value;
        value = aValue;
        Set<GameEventListener> theKnownListener = new HashSet<GameEventListener>(changeListener);
        for (GameEventListener<PropertyChanged> theListener : theKnownListener) {
            theListener.handleGameEvent(new PropertyChanged(this, theOldValue));
        }
    }

    public void clear() {
        value = null;
    }

    public void setQuietly(T aValue) {
        value = aValue;
    }

    public Set<GameEventListener<PropertyChanged>> getChangeListener() {
        return Collections.unmodifiableSet(changeListener);
    }

    public void addChangeListener(GameEventListener<PropertyChanged> aListener) {
        changeListener.add(aListener);
    }

    public void removeChangeListener(GameEventListener<PropertyChanged> aListener) {
        changeListener.remove(aListener);
    }
}
