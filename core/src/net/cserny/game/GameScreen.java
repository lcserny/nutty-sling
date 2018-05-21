package net.cserny.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float UNITS_PER_METER = 32f;
    private static final float UNITS_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static final float UNITS_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private final NuttyGame game;

    private World world;
    private Box2DDebugRenderer debugRenderer;
//    private Body body;
    private OrthographicCamera camera;
    private OrthographicCamera box2dCamera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    public GameScreen(NuttyGame game) {
        this.game = game;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        world = new World(new Vector2(0, -10f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
//        body = createBody();
//        body.setTransform(100, 120, 0);

        camera = new OrthographicCamera();
        box2dCamera = new OrthographicCamera(UNITS_WIDTH, UNITS_HEIGHT);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        tiledMap = game.getAssetManager().get("nutty.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        tiledMapRenderer.setView(camera);

        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildFloorBodies(tiledMap, world);
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void update(float delta) {
        world.step(delta, 6, 2);
//        body.setAwake(true);
        box2dCamera.position.set(UNITS_WIDTH / 2, UNITS_HEIGHT / 2, 0);
        box2dCamera.update();
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        tiledMapRenderer.render();
    }

    private void drawDebug() {
        debugRenderer.render(world, box2dCamera.combined);
    }

//    private Body createBody() {
//        BodyDef def = new BodyDef();
//        def.type = BodyDef.BodyType.DynamicBody;
//        Body box = world.createBody(def);
//        PolygonShape poly = new PolygonShape();
//        poly.setAsBox(60/UNITS_PER_METER, 60/UNITS_PER_METER);
//        box.createFixture(poly, 1);
//        poly.dispose();
//        return box;
//    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.SKY.r, Color.SKY.g, Color.SKY.b, Color.SKY.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
