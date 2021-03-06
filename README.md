FXGameComposer
=============

FXGameComposer is a game authoring tool and also a game runtime environment, targeting at JavaFX2 or HTML5 capable devices.

To see it in action please visit [this page](http://www.mirkosertic.de/doku.php/javastuff/javafxgameauthoring)

GameComposer
-----------

GameComposer is the authoring tool, the IDE. It is a JavaFX2 application. Using this tool, games can be designed
and tested. Finally, they can be exported to a target runtime, e.g. HTML5.

GameEngine
----------

GameEngine is the cross-platform game engine.

Core concepts and components of the game engine are:

| Concept /  component   | Description
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------
| Game                   | A Game is the top level container. Each Game is split into several GameScenes.
| GameScene              | A Scene is like a level of a game. A Scene has a collection of GameObjects, Assets and Events.
| GameSystem             | A GameSystem defines a subsystem of the game engine. There are GameSystems for physics, sounds and other stuff
| GameView               | A GameView is the presentation of a GameScene to some kind of consumer. This can be a screen renderer or even a remote consumer.
| GameObject             | A GameObject is a template for visible objects. The behavior of a GameObject is defined by GameComponentTemplates
| BehaviorTemplate       | A BehaviorTemplate defines some kind of behavior, e.g. if it is static, a sprite or driven by physics.
| GameObjectInstance     | A GameObjectInstance is an instance of a GameObject. It inherits the defined GameComponentTemplates by copying them into a GameComponent
| Behavior               | A Behavior is the behavior status for a BehaviorTemplate and a GameObjectInstance.
| Event                  | An Event can be triggered by a consumer by sending it via the GameView to the engine. An Event can also be triggered or consumed by a GameComponent or GameSystem.
| EventSheet             | An EventSheet is the rule engine. Each rule has a condition and some actions.
| GameLoop               | The GameLoop drives the engine by calling the GameSystems, rendering the GameView and dispatching events.
| GameProcess            | A GameProcess is something that takes multiple GameLoop cycles to complete, for instance playing background music or playing an animation.


GameEngine-FXRenderer
---------------------

This is the game presentation logic by implementing a GameView using JavaFX 2. This also includes a Soundsystem implementation based on Java Sound API.

GameEngine-GWTRenderer
--------------

This is the game presentation logic using GWT and HTML5(JavaScript) by implementing a GameView. This also includes a SoundSystem implementation based on HTML5 Media API.
Currently the HTML5 renderer tries to use WebGL. If WebGL is not available, it will fallback to the HTML5 Canvas API.

GameEngine-AndroidRenderer
--------------

This is the game presentation logic using the Android Java SDK. This also includes a SoundSystem implementation for Android devices.
We use an Android SurfaceView for rendering. Future versions will include OpenGL ES support.