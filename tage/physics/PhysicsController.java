package tage.physics;

import tage.*;
import tage.physics.*;
import org.joml.*;
import java.util.*;
import tage.networking.client.*;

public class PhysicsController {

    private Engine engine;
    private PhysicsEngine physicsEngine;

    private PhysicsObject avatarP;
    private PhysicsObject floorP;
    private ArrayList<PhysicsObject> npcPhysicsObjects = new ArrayList<>();

    public PhysicsController(Engine engine) {
        this.engine = engine;
    }

    public void initializePhysicsObjects(
        GameObject avatar,
        GameObject floor,
        TextureImage heightMap
    ) {
        float[] gravity = {0f, -15f, 0f};

        physicsEngine = engine.getSceneGraph().getPhysicsEngine();
        physicsEngine.setGravity(gravity);

        float mass = 1f;
        float[] angularFactor = {0f, 1f, 0f};
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
        //engine.enablePhysicsWorldRender();
    }

    public PhysicsObject addNPCPhysics(GameObject npc) {
        float mass = 1f;
        float[] up = {0f, 1f, 0f};

        Vector3f loc = npc.getWorldLocation();
        Quaternionf rot = new Quaternionf();
        npc.getWorldRotation().getNormalizedRotation(rot);

        float radius = 1.25f;
        float height = 3.5f;

        PhysicsObject npcP = engine.getSceneGraph().addPhysicsCapsule(
            mass,
            loc,
            rot,
            2,
            radius,
            height
        );

        npcP.setFriction(0.2f);
        npcP.setBounciness(0f);
        npcP.setDamping(0.6f, 0.8f);
        npcP.setAngularFactor(up);
        npcP.disableSleeping();

        npc.setPhysicsObject(npcP);
        npcPhysicsObjects.add(npcP);

        return npcP;
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
        if (go instanceof GhostAvatar) return;
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

    public PhysicsObject getFloorPhysics() {
        return floorP;
    }

    public PhysicsObject addGhostPhysics(GameObject ghost) {
        float mass = 0f; // static/kinematic-style ghost hitbox
        float radius = 1.25f;
        float height = 3.5f;

        Vector3f loc = ghost.getWorldLocation();
        Quaternionf rot = new Quaternionf();
        ghost.getWorldRotation().getNormalizedRotation(rot);

        PhysicsObject ghostP = engine.getSceneGraph().addPhysicsCapsule(
            mass,
            loc,
            rot,
            2,
            radius,
            height
        );

        ghostP.setFriction(0.5f);
        ghostP.setBounciness(0f);

        ghost.setPhysicsObject(ghostP);

        return ghostP;
    }
    
}
