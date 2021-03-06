package de.mirkosertic.gameengine.gwt;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import de.mirkosertic.gameengine.camera.CameraBehavior;
import de.mirkosertic.gameengine.core.ExpressionParser;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;
import de.mirkosertic.gameengine.core.GameScene;
import de.mirkosertic.gameengine.core.GestureDetector;
import de.mirkosertic.gameengine.input.DefaultGestureDetector;
import de.mirkosertic.gameengine.sprite.Sprite;
import de.mirkosertic.gameengine.sprite.SpriteBehavior;
import de.mirkosertic.gameengine.text.Text;
import de.mirkosertic.gameengine.text.TextBehavior;
import de.mirkosertic.gameengine.type.Angle;
import de.mirkosertic.gameengine.type.Color;
import de.mirkosertic.gameengine.type.Position;
import de.mirkosertic.gameengine.type.Size;

import thothbot.parallax.core.client.gl2.*;
import thothbot.parallax.core.client.gl2.arrays.Float32Array;
import thothbot.parallax.core.client.gl2.arrays.Uint16Array;
import thothbot.parallax.core.client.gl2.enums.*;
import thothbot.parallax.core.shared.math.Matrix4;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GWTWebGLGameView extends AbstractWebGameView {

    interface Resource extends ClientBundle {

        Resource INSTANCE = GWT.create(Resource.class);

        @Source("source/sprite.vs")
        TextResource getVertexShader();

        @Source("source/sprite.fs")
        TextResource getFragmentShader();
    }

    static class GLSprite {

        final Float32Array vertices;
        final Uint16Array faces;

        final WebGLBuffer vertexBuffer;
        final WebGLBuffer elementBuffer;
        final WebGLProgram program;

        WebGLUniformLocation modelViewTransform;
        WebGLUniformLocation screenPosition;
        WebGLUniformLocation scale;
        WebGLUniformLocation rotation;
        WebGLUniformLocation textureMap;
        WebGLUniformLocation drawBorder;

        int vertexPositionVariable;
        int uvVariable;

        private final WebGLRenderingContext renderingContext;
        Map<String, WebGLTexture> loadedTextures;

        GLSprite(WebGLRenderingContext aRenderingContext) {

            renderingContext = aRenderingContext;

            vertices = Float32Array.create(8 + 8);
            faces = Uint16Array.create(6);

            int i = 0;

            vertices.set(i++, -0.5f);
            vertices.set(i++, -0.5f);    // vertex 0
            vertices.set(i++, 0);
            vertices.set(i++, 1f);    // uv 0

            vertices.set(i++, 0.5f);
            vertices.set(i++, -0.5f);    // vertex 1
            vertices.set(i++, 1f);
            vertices.set(i++, 1f);    // uv 1

            vertices.set(i++, 0.5f);
            vertices.set(i++, 0.5f);    // vertex 2
            vertices.set(i++, 1f);
            vertices.set(i++, 0);    // uv 2

            vertices.set(i++, -0.5f);
            vertices.set(i++, 0.5f);    // vertex 3
            vertices.set(i++, 0);
            vertices.set(i++, 0);    // uv 3

            i = 0;

            faces.set(i++, 0);
            faces.set(i++, 1);
            faces.set(i++, 2);
            faces.set(i++, 0);
            faces.set(i++, 2);
            faces.set(i++, 3);

            vertexBuffer = aRenderingContext.createBuffer();
            elementBuffer = aRenderingContext.createBuffer();

            aRenderingContext.bindBuffer(BufferTarget.ARRAY_BUFFER, vertexBuffer);
            aRenderingContext.bufferData(BufferTarget.ARRAY_BUFFER, vertices, BufferUsage.STATIC_DRAW);

            aRenderingContext.bindBuffer(BufferTarget.ELEMENT_ARRAY_BUFFER, elementBuffer);
            aRenderingContext.bufferData(BufferTarget.ELEMENT_ARRAY_BUFFER, faces, BufferUsage.STATIC_DRAW);

            // Create the program
            program = aRenderingContext.createProgram();

            WebGLShader theVertexShader = aRenderingContext.createShader(WebGLConstants.VERTEX_SHADER);
            aRenderingContext.shaderSource(theVertexShader, Resource.INSTANCE.getVertexShader().getText());
            aRenderingContext.compileShader(theVertexShader);
            if (!aRenderingContext.getShaderParameterb(theVertexShader, WebGLConstants.COMPILE_STATUS)) {
                throw new RuntimeException(aRenderingContext.getShaderInfoLog(theVertexShader));
            }

            WebGLShader theFragmentShader = aRenderingContext.createShader(WebGLConstants.FRAGMENT_SHADER);
            aRenderingContext.shaderSource(theFragmentShader, Resource.INSTANCE.getFragmentShader().getText());
            aRenderingContext.compileShader(theFragmentShader);
            if (!aRenderingContext.getShaderParameterb(theFragmentShader, WebGLConstants.COMPILE_STATUS)) {
                throw new RuntimeException(aRenderingContext.getShaderInfoLog(theFragmentShader));
            }

            aRenderingContext.attachShader(program, theVertexShader);
            aRenderingContext.attachShader(program, theFragmentShader);
            aRenderingContext.linkProgram(program);

            if (!aRenderingContext.getProgramParameterb(program, ProgramParameter.LINK_STATUS)) {
                throw new RuntimeException(aRenderingContext.getProgramInfoLog(program));
            }

            aRenderingContext.deleteShader(theVertexShader);
            aRenderingContext.deleteShader(theFragmentShader);

            // And finally we need to bind the variables and uniforms
            vertexPositionVariable = aRenderingContext.getAttribLocation(program, "aVertexPosition");
            aRenderingContext.enableVertexAttribArray(vertexPositionVariable);

            uvVariable = aRenderingContext.getAttribLocation(program, "aUv");
            aRenderingContext.enableVertexAttribArray(uvVariable);

            modelViewTransform = aRenderingContext.getUniformLocation(program, "uPMatrix");
            screenPosition = aRenderingContext.getUniformLocation(program, "uScreenPosition");
            drawBorder = aRenderingContext.getUniformLocation(program, "uDrawBorder");

            scale = aRenderingContext.getUniformLocation(program, "uScale");
            rotation = aRenderingContext.getUniformLocation(program, "uRotation");
            textureMap = aRenderingContext.getUniformLocation(program, "uMap");

            // The texture cache with resource name and other studd
            loadedTextures = new HashMap<String, WebGLTexture>();
        }

        public void render(Position aPosition, Size aSize, Angle aRotation, GWTBitmapResource aResource) {

            WebGLTexture theTexture = null;
            if (aResource != null && aResource.isLoaded()) {
                theTexture = loadedTextures.get(aResource.getImage().getSrc());
                if (theTexture == null) {
                    // Create and load a new texture
                    // This is an expensive operation, so do it only once
                    theTexture = renderingContext.createTexture();

                    renderingContext.activeTexture(TextureUnit.TEXTURE0);
                    renderingContext.bindTexture(TextureTarget.TEXTURE_2D, theTexture);
                    renderingContext.texParameteri(TextureTarget.TEXTURE_2D, TextureParameterName.TEXTURE_MIN_FILTER, WebGLConstants.LINEAR);
                    renderingContext.texParameteri(TextureTarget.TEXTURE_2D, TextureParameterName.TEXTURE_WRAP_S, WebGLConstants.CLAMP_TO_EDGE);
                    renderingContext.texParameteri(TextureTarget.TEXTURE_2D, TextureParameterName.TEXTURE_WRAP_T, WebGLConstants.CLAMP_TO_EDGE);
                    renderingContext.pixelStorei(PixelStoreParameter.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 0);
                    renderingContext.texImage2D(TextureTarget.TEXTURE_2D, 0, PixelFormat.RGBA, PixelType.UNSIGNED_BYTE, aResource.getImage());
                    renderingContext.bindTexture(TextureTarget.TEXTURE_2D, null);

                    loadedTextures.put(aResource.getImage().getSrc(), theTexture);
                }
            }

            // Position, Scale and Rotation
            renderingContext.uniform2f(screenPosition, aPosition.x + aSize.width / 2, aPosition.y + aSize.height / 2);
            renderingContext.uniform2f(scale, aSize.width, aSize.height);
            renderingContext.uniform1f(rotation, aRotation.toRadians());

            // 3 elements per vertex
            renderingContext.bindBuffer(BufferTarget.ARRAY_BUFFER, vertexBuffer);
            renderingContext.vertexAttribPointer(vertexPositionVariable, 2, DataType.FLOAT, false, 2 * 8, 0);
            renderingContext.vertexAttribPointer(uvVariable, 2, DataType.FLOAT, false, 2 * 8, 8);
            renderingContext.bindBuffer(BufferTarget.ELEMENT_ARRAY_BUFFER, elementBuffer);

            if (theTexture != null) {
                renderingContext.enable(EnableCap.BLEND);
                renderingContext.activeTexture(TextureUnit.TEXTURE0, 0);
                renderingContext.bindTexture(TextureTarget.TEXTURE_2D, theTexture);
                renderingContext.uniform1i(textureMap, 0);
                renderingContext.uniform1f(drawBorder, 0);

                renderingContext.drawElements(BeginMode.TRIANGLES, 6, DrawElementsType.UNSIGNED_SHORT, 0);
            } else {
                renderingContext.disable(EnableCap.BLEND);
                renderingContext.uniform1i(textureMap, 0);
                renderingContext.uniform1f(drawBorder, 0);

                renderingContext.drawArrays(BeginMode.LINE_LOOP, 0, 4);
            }
        }
    }

    private final GameRuntime gameRuntime;
    private final WebGLRenderingContext webGLRenderingContext;
    private final Canvas canvas;
    private final CameraBehavior cameraComponent;
    private final GLSprite sprite;
    private final GestureDetector gestureDetector;

    private Matrix4 transform;

    public GWTWebGLGameView(GameRuntime aRuntime, WebGLRenderingContext aWebGLRenderingContext, Canvas aCanvas, CameraBehavior aCameraComponent) {
        gameRuntime = aRuntime;
        webGLRenderingContext = aWebGLRenderingContext;
        cameraComponent = aCameraComponent;
        canvas = aCanvas;
        gestureDetector = new DefaultGestureDetector(aRuntime.getEventManager());

        sprite = new GLSprite(aWebGLRenderingContext);
    }

    @Override
    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    @Override
    public void setSize(Size aSize) {
        super.setSize(aSize);
        webGLRenderingContext.viewport(0, 0, aSize.width, aSize.height);

        Matrix4 theMat4 = new Matrix4();
        theMat4.makeOrthographic(0, aSize.width, 0, aSize.height, -10, 10);

        transform = theMat4;
    }

    @Override
    public void renderGame(long aGameTime, long aElapsedTimeSinceLastLoop, GameScene aScene) {
        Size theCurrentSize = getSize();

        Context2d theContext = canvas.getContext2d();

        Color theBGColor = aScene.backgroundColorProperty().get();

        // Fill the context with transparent background
        CssColor theCssBackground = CssColor.make("rgba(" + theBGColor.r + "," + theBGColor.g + "," + theBGColor.b + ",0)");
        theContext.setFillStyle(theCssBackground);
        theContext.setStrokeStyle(theCssBackground);
        theContext.fillRect(0, 0, theCurrentSize.width, theCurrentSize.height);

        webGLRenderingContext.clearColor(theBGColor.r / 255, theBGColor.g / 255, theBGColor.b / 255, 1);
        webGLRenderingContext.clear(WebGLConstants.COLOR_BUFFER_BIT | WebGLConstants.DEPTH_BUFFER_BIT);
        webGLRenderingContext.disable(EnableCap.DEPTH_TEST);

        webGLRenderingContext.blendFunc(BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE_MINUS_SRC_ALPHA);
        webGLRenderingContext.enable(EnableCap.BLEND);
        webGLRenderingContext.depthMask(true);

        webGLRenderingContext.useProgram(sprite.program);

        // Setup model world transform
        webGLRenderingContext.uniformMatrix4fv(sprite.modelViewTransform, false, transform.getArray());

        for (GameObjectInstance theInstance : cameraComponent.getObjectsToDrawInRightOrder(aScene)) {

            Position thePosition = cameraComponent.transformToScreenPosition(theInstance);

            Size theSize = theInstance.getOwnerGameObject().sizeProperty().get();

            boolean theSomethingRendered = false;

            Sprite theSpriteComponent = theInstance.getComponent(SpriteBehavior.class);
            if (theSpriteComponent != null) {
                try {
                    GWTBitmapResource theBitmap = gameRuntime.getResourceCache().getResourceFor(theSpriteComponent.resourceNameProperty().get());

                    sprite.render(thePosition, theSize, theInstance.rotationAngleProperty().get(), theBitmap);
                    theSomethingRendered = true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Text theTextComponent = theInstance.getComponent(TextBehavior.class);
            if (theTextComponent != null) {
                ExpressionParser theExpressionParser = aScene.get(theTextComponent.textExpressionProperty().get());
                drawText(theContext, thePosition, theTextComponent.fontProperty().get(), theTextComponent.colorProperty().get(), theExpressionParser.evaluateToString(), theSize);
                theSomethingRendered = true;
            }

            if (!theSomethingRendered) {
                sprite.render(thePosition, theSize, theInstance.rotationAngleProperty().get(), null);
            }
        }
    }
}