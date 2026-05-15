package tage.physics;

import tage.*;
import tage.physics.*;
import org.joml.*;

public class PhysicsController {

    private Engine engine;
    private PhysicsEngine physicsEngine;

    private PhysicsObject avatarP;
    private PhysicsObject npcP;
    private PhysicsObject floorP;

    public PhysicsController(Engine engine) {
        this.engine = engine;
    }

    public void initializePhysicsObjects(
        GameObject avatar,
        GameObject npc,
        GameObject floor,
        TextureImage heightMap
    ) {
        float[] gravity = {0f, -15f, 0f};

        physicsEngine = engine.getSceneGraph().getPhysicsEngine();
        physicsEngine.setGravity(gravity);

        float mass = 1f;
        float[] angularFactor = {0f, 1f, 0f};
        float[] bikeSize = {2f, 2f, 5f};
        float radius = 1.25f;
        float height = 3.5f;

        Vector3f loc;
        Quaternionf rot;


        loc = avatar.getWorldLocation();
        rot = new Quaternionf();
        avatar.getWorldRotation().getNormalizedRotation(rot);

        avatarP = engine.getSceneGraph().addPhysicsCapsule(mass, loc, rot, 2, radius, height);
        avatarP.setFriction(0.2f);
        avatarP.disableSleeping();
        avatarP.setDamping(0.6f, 0.8f);
        avatarP.setBounciness(0f);
        avatarP.setAngularFactor(angularFactor);
        avatar.setPhysicsObject(avatarP);

        loc = npc.getWorldLocation();
        rot = new Quaternionf();
        npc.getWorldRotation().getNormalizedRotation(rot);

        npcP = engine.getSceneGraph().addPhysicsCapsule(mass, loc, rot, 2, radius, height);
        npcP.setFriction(0f);
        npcP.disableSleeping();
        npcP.setDamping(0.6f, 0.8f);
        npcP.setBounciness(0f);
        npcP.setAngularFactor(angularFactor);
        npc.setPhysicsObject(npcP);

        loc = floor.getWorldLocation();
        rot = new Quaternionf();
        floor.getWorldRotation().getNormalizedRotation(rot);

        floorP = engine.getSceneGraph().addPhysicsStaticTerrainMesh(
            loc,
            rot,
            heightMap,
            500f,
            10f,
            500
        );

        floorP.setFriction(0.2f);
        floorP.setBounciness(0f);
        floorP.disableSleeping();
        floor.setPhysicsObject(floorP);

        engine.enableGraphicsWorldRender();
        engine.enablePhysicsWorldRender();
    }

    public void update(double frameTimeMillis) {
        if (physicsEngine == null) {
            return;
        }

        physicsEngine.update((float)frameTimeMillis / 1000f);
    }

    public void syncGameObjectsToPhysics() {
        for (GameObject go : engine.getSceneGraph().getGameObjects()) {
            if (go.getPhysicsObject() != null) {
                syncGameObject(go);
            }
        }
    }

    private void syncGameObject(GameObject go) {
        PhysicsObject physicsObject = go.getPhysicsObject();

        Vector3f loc = physicsObject.getLocation();

        Matrix4f locMat = new Matrix4f();
        locMat.set(3, 0, loc.x);
        locMat.set(3, 1, loc.y);
        locMat.set(3, 2, loc.z);

        go.setLocalTranslation(locMat);

        Quaternionf rot = physicsObject.getRotation();

        Matrix4f rotMat = new Matrix4f();
        rot.get(rotMat);

        go.setLocalRotation(rotMat);
    }

    public PhysicsObject getAvatarPhysics() {
        return avatarP;
    }

    public PhysicsObject getNpcPhysics() {
        return npcP;
    }

    public PhysicsObject getFloorPhysics() {
        return floorP;
    }
    
}
