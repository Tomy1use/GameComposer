package de.mirkosertic.gameengine.action;

import de.mirkosertic.gameengine.event.GameEvent;

public class SystemTick extends GameEvent {

    public final long totalTicks;
    public final long gameTime;
    public final long elapsedTimeSinceLastLoop;

    SystemTick(long aTotalTicks, long aGameTime, long aElapsedTimeSinceLastLoop) {
        super("SystemTick");
        totalTicks = aTotalTicks;
        gameTime = aGameTime;
        elapsedTimeSinceLastLoop = aElapsedTimeSinceLastLoop;
    }
}
