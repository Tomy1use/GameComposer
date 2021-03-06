package de.mirkosertic.gameengine.action;

import de.mirkosertic.gameengine.core.*;
import de.mirkosertic.gameengine.event.GameEvent;
import de.mirkosertic.gameengine.process.InvokeActionProcess;
import de.mirkosertic.gameengine.process.StartProcess;

public class ActionManager implements GameSystem {

    private final GameScene scene;

    ActionManager(GameScene aScene) {
        scene = aScene;
    }

    @Override
    public void proceedGame(long aTotalTicks, long aGameTime, long aElapsedTime) {
        scene.getRuntime().getEventManager().fire(new SystemTick(aTotalTicks, aGameTime, aElapsedTime));
    }

    void onEvent(GameEvent aEvent) {
        for (EventSheet theSheet : scene.getEventSheets()) {
            for (GameRule theRule : theSheet.getRules()) {
                if (!theRule.conditionProperty().isNull()) {
                    ConditionResult theResult = theRule.conditionProperty().get().appliesTo(scene, aEvent);
                    if (theResult.isConditionTrue()) {
                        Action[] theActions = theRule.getActions();
                        if (theActions.length > 0) {
                            // Build a process tree and run it
                            InvokeActionProcess theRootProcess = new InvokeActionProcess(scene, theResult, theActions[0]);
                            InvokeActionProcess theChildProcess = theRootProcess;
                            for (int i=1;i<theActions.length;i++) {
                                InvokeActionProcess theNewChildProcess = new InvokeActionProcess(scene, theResult, theActions[i]);
                                theChildProcess.setChildProcess(theNewChildProcess);
                                theChildProcess = theNewChildProcess;
                            }
                            scene.getRuntime().getEventManager().fire(new StartProcess(theRootProcess));
                        }
                    }
                }
            }
        }
    }
}