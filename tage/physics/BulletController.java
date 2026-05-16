package tage.physics;

import tage.*;
import tage.networking.client.GhostAvatar;
import tage.shapes.Sphere;

import org.joml.*;

import a2.MyGame;

import java.util.*;

public class BulletController {
    private Engine engine;
    private PhysicsEngine physicsEngine;

    private ArrayList<GameObject> bullets = new ArrayList<>();
    private HashMap<PhysicsObject, GameObject> bulletMap = new HashMap<>();

    private float bulletSpeed = 80f;
    private int damage = 10;

    private MyGame game;
    private ObjShape bulShape;
    private TextureImage bulTxt;

    public BulletController(Engine engine, MyGame myGame) {
        this.engine = engine;
        this.game = myGame;
        this.physicsEngine = engine.getSceneGraph().getPhysicsEngine();

        bulShape = new Sphere();
        bulTxt = new TextureImage("bullet.jpg");
    }

    public void shoot(GameObject shooter) {
        Vector3f start = new Vector3f(shooter.getWorldLocation());
        Vector3f fwd = new Vector3f(shooter.getWorldForwardVector()).normalize();

        start.add(new Vector3f(fwd).mul(4f));
        start.y += 1.5f;

        GameObject bullet = new GameObject(GameObject.root(), bulShape, bulTxt);
        bullet.setLocalScale(new Matrix4f().scale(0.25f));
        bullet.setLocalLocation(start);

        PhysicsObject bulletP = engine.getSceneGraph().addPhysicsSphere(
            1f,
            start,
            new Quaternionf(),
            0.25f
        );

        bulletP.setBounciness(0f);
        bulletP.setFriction(0f);
        bulletP.setLinearVelocity(new float[] {
            fwd.x * bulletSpeed,
            fwd.y * bulletSpeed,
            fwd.z * bulletSpeed
        });

        bullet.setPhysicsObject(bulletP);

        bullets.add(bullet);
        bulletMap.put(bulletP, bullet);
    }

    public void update() {
        physicsEngine.detectCollisions();

        ArrayList<GameObject> bulletsToRemove = new ArrayList<>();

        for (GameObject bullet : bullets) {
            PhysicsObject bulletP = bullet.getPhysicsObject();

            Vector3f loc = bulletP.getLocation();

            Matrix4f locMat = new Matrix4f();
            locMat.translation(loc);

            bullet.setLocalTranslation(locMat);


            Quaternionf rot = bulletP.getRotation();

            Matrix4f rotMat = new Matrix4f();
            rot.get(rotMat);

            bullet.setLocalRotation(rotMat);

            HashSet<PhysicsObject> collisions = bulletP.getNewlyCollidedSet();

            if (collisions.size() > 0) {
                for (PhysicsObject hit : collisions) {
                    handleHit(hit);
                }

                bulletsToRemove.add(bullet);
            }
        }

        for (GameObject bullet : bulletsToRemove) {
            removeBullet(bullet);
        }
    }

    private void handleHit(PhysicsObject hit) {
        GameObject hitObject = findGameObjectByPhysics(hit);

        if (hitObject == null) return;

        if (hitObject instanceof GhostAvatar) {
            GhostAvatar ghost = (GhostAvatar) hitObject;
            ghost.damage(damage);

            if (game.getProtocolClient() != null && game.getIsConnected()) {
                game.getProtocolClient().sendHitMessage(ghost.getID(), damage);
            }
        }

    }

    private GameObject findGameObjectByPhysics(PhysicsObject po) {
        for (GameObject go : engine.getSceneGraph().getGameObjects()) {
            if (go.getPhysicsObject() == po) {
                return go;
            }
        }

        return null;
    }

    private void removeBullet(GameObject bullet) {
        bullets.remove(bullet);
        bulletMap.remove(bullet.getPhysicsObject());

        engine.getSceneGraph().removeGameObject(bullet);

    }
}