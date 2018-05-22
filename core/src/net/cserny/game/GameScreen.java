package net.cserny.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {

    public static final String ENEMY = "enemy";

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float UNITS_PER_METER = 32f;
    private static final float UNITS_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static final float UNITS_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;
    private static final float MAX_STRENGTH = 15;
    private static final float MAX_DISTANCE = 100;
    private static final float UPPER_ANGLE = 3 * MathUtils.PI / 2f;
    private static final float LOWER_ANGLE = MathUtils.PI / 2f;

    private final NuttyGame game;
    private final Vector2 anchor = new Vector2(convertMetersToUnits(3), convertMetersToUnits(6));
    private final Vector2 firingPosition = anchor.cpy();

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
    private Array<Body> toRemove = new Array<Body>();
    private float distance, angle;

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
        TiledObjectBodyBuilder.buildBirdBodies(tiledMap, world);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                calculateAngleAndDistanceForBullet(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                createBullet();
                firingPosition.set(anchor.cpy());
                return true;
            }
        });

        world.setContactListener(new NuttyContactListener());
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void clearDeadBodies() {
        for (Body body : toRemove) {
            world.destroyBody(body);
        }
        toRemove.clear();
    }

    private float convertUnitsToMeters(float pixels) {
        return pixels / UNITS_PER_METER;
    }

    private float convertMetersToUnits(float meters) {
        return meters * UNITS_PER_METER;
    }

    private float angleBetweenTwoPoints() {
        float angle = MathUtils.atan2(anchor.y - firingPosition.y, anchor.x - firingPosition.x);
        angle %= 2 * MathUtils.PI;
        if (angle < 0) {
            angle += 2 * MathUtils.PI;
        }
        return angle;
    }

    private float distanceBetweenTwoPoints() {
        return (float) Math.sqrt(((anchor.x - firingPosition.x) * (anchor.x  - firingPosition.x))
                + ((anchor.y - firingPosition.y) * (anchor.y - firingPosition.y)));
    }

    private void calculateAngleAndDistanceForBullet(int screenX, int screenY) {
        firingPosition.set(screenX, screenY);
        viewport.unproject(firingPosition);
        distance = distanceBetweenTwoPoints();
        angle = angleBetweenTwoPoints();

        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }

        if (angle > LOWER_ANGLE) {
            if (angle > UPPER_ANGLE) {
                angle = 0;
            } else {
                angle = LOWER_ANGLE;
            }
        }

        firingPosition.set(anchor.x + (distance * -MathUtils.cos(angle)),
                anchor.y + (distance * -MathUtils.sin(angle)));
    }

    private void update(float delta) {
        clearDeadBodies();
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

    private void createBullet() {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(0.5f);
        circleShape.setPosition(new Vector2(convertUnitsToMeters(firingPosition.x), convertUnitsToMeters(firingPosition.y)));

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;

        Body bullet = world.createBody(def);
        bullet.createFixture(circleShape, 1);
        float velX = Math.abs((MAX_STRENGTH * -MathUtils.cos(angle) * (distance / 100f)));
        float velY = Math.abs((MAX_STRENGTH * -MathUtils.sin(angle) * (distance / 100f)));
        bullet.setLinearVelocity(velX, velY);

        circleShape.dispose();
    }

    private void drawDebug() {
        debugRenderer.render(world, box2dCamera.combined);
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(anchor.x - 5, anchor.y - 5, 10, 10);
        shapeRenderer.rect(firingPosition.x - 5, firingPosition.y - 5, 10, 10);
        shapeRenderer.line(anchor.x, anchor.y, firingPosition.x, firingPosition.y);
        shapeRenderer.end();
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

    private class NuttyContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            if (contact.isTouching()) {
                Fixture attacker = contact.getFixtureA();
                Fixture defender = contact.getFixtureB();
                WorldManifold worldManifold = contact.getWorldManifold();

                if (ENEMY.equals(defender.getUserData())) {
                    Vector2 vel1 = attacker.getBody().getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 vel2 = defender.getBody().getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 impactVelocity = vel1.sub(vel2);

                    if (Math.abs(impactVelocity.x) > 1) {
                        toRemove.add(defender.getBody());
                    }
                }
            }
        }

        @Override
        public void endContact(Contact contact) { }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) { }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) { }
    }
}
