package net.cserny.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class TiledObjectBodyBuilder {

    private static final float PIXELS_PER_TILE = 32f;
    private static final float HALF = 0.5f;

    public static void buildBuildingBodies(TiledMap tiledMap, World world) {
        MapObjects objects = tiledMap.getLayers().get("Physics_Buildings").getObjects();
        for (MapObject object : objects) {
            PolygonShape rectangle = getRectangle((RectangleMapObject) object);

            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;

            Body body = world.createBody(def);
            body.createFixture(rectangle, 1);

            rectangle.dispose();
        }
    }

    public static void buildFloorBodies(TiledMap tiledMap, World world) {
        MapObjects objects = tiledMap.getLayers().get("Physics_Floor").getObjects();
        for (MapObject object : objects) {
            PolygonShape rectangle = getRectangle((RectangleMapObject) object);

            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.StaticBody;

            Body body = world.createBody(def);
            body.createFixture(rectangle, 1);

            rectangle.dispose();
        }
    }

    public static void buildBirdBodies(TiledMap tiledMap, World world) {
        MapObjects objects = tiledMap.getLayers().get("Physics_Birds").getObjects();
        for (MapObject object : objects) {
            CircleShape circle = getCircle((EllipseMapObject) object);

            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.DynamicBody;

            Body body = world.createBody(def);
            Fixture fixture = body.createFixture(circle, 1);
            fixture.setUserData(GameScreen.ENEMY);

            circle.dispose();
        }
    }

    private static CircleShape getCircle(EllipseMapObject ellipseObject) {
        Ellipse ellipse = ellipseObject.getEllipse();
        CircleShape circle = new CircleShape();
        circle.setRadius(ellipse.width * HALF / PIXELS_PER_TILE);
        circle.setPosition(new Vector2(
                (ellipse.x + ellipse.width * HALF) / PIXELS_PER_TILE,
                (ellipse.y + ellipse.height * HALF) / PIXELS_PER_TILE
        ));
        return circle;
    }

    private static PolygonShape getRectangle(RectangleMapObject rectangleObject) {
        Rectangle rectangle = rectangleObject.getRectangle();
        PolygonShape polygon = new PolygonShape();
        Vector2 size = new Vector2(
                (rectangle.x + rectangle.width * HALF) / PIXELS_PER_TILE,
                (rectangle.y + rectangle.height * HALF) / PIXELS_PER_TILE
        );
        polygon.setAsBox(
                rectangle.width * HALF / PIXELS_PER_TILE,
                rectangle.height * HALF / PIXELS_PER_TILE,
                size, 0.0f
        );
        return polygon;
    }
}
