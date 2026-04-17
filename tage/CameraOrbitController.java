package tage;

import java.lang.Math;

import net.java.games.imput.Event;

import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.rml.Vector3f;

public class CameraOrbitController {
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target
    public CameraOrbitController(Camera cam, GameObject av, String gpName, Engine e)
    { engine = e;
    camera = cam;
    avatar = av;
    cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
    cameraElevation = 20.0f; // elevation is in degrees
    cameraRadius = 2.0f; // distance from camera to avatar
    setupInputs(gpName);
    updateCameraPosition();
    }

    private void setupInputs(String gp)
    { 
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        OrbitElevationAction elevAction = new OrbitElevationAction();
        OrbitRadiusAction zoomAction = new OrbitRadiusAction();
        InputManager im = engine.getInputManager();
        im.associateAction(gp,
        net.java.games.input.Component.Identifier.Axis.RX, azmAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.LEFT,
        azmAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.RIGHT,
        azmAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    // UP / DOWN arrows = change elevation
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.UP,
        elevAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.DOWN,
        elevAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    // Optional zoom
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.PAGEUP,
        zoomAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.PAGEDOWN,
        zoomAction,
        InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    // Compute the camera’s azimuth, elevation, and distance, relative to
    // the target in spherical coordinates, then convert to world Cartesian
    // coordinates and set the camera position from that.
    public void updateCameraPosition()
    { Vector3f avatarRot = avatar.getWorldForwardVector();
    double avatarAngle = Math.toDegrees((double)
    avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
    float totalAz = cameraAzimuth - (float)avatarAngle;
    double theta = Math.toRadians(totalAz);
    double phi = Math.toRadians(cameraElevation);
    float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
    float y = cameraRadius * (float)(Math.sin(phi));
    float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
    camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
    camera.lookAt(avatar);
    }

    private class OrbitAzimuthAction extends AbstractInputAction
    { public void performAction(float time, Event event)
    { float rotAmount;
    if (event.getValue() < -0.2)
    { rotAmount=-0.02f; }
    else
    { if (event.getValue() > 0.2)
    { rotAmount=0.02f; }
    else
    { rotAmount=0.0f; }
    }
    cameraAzimuth += rotAmount;
    cameraAzimuth = cameraAzimuth % 360;
    updateCameraPosition();
    } }

    private class OrbitElevationAction extends AbstractInputAction
    {
        public void performAction(float time, Event event)
        {
            float elevAmount;

            if (event.getValue() < -0.2)
            { elevAmount = -0.02f; }
            else if (event.getValue() > 0.2)
            { elevAmount = 0.02f; }
            else
            { elevAmount = 0.0f; }

            cameraElevation += elevAmount;

            if (cameraElevation > 89.0f) cameraElevation = 89.0f;
            if (cameraElevation < 1.0f) cameraElevation = 1.0f;

            updateCameraPosition();
        }   
    }
    private class OrbitRadiusAction extends AbstractInputAction
    {
        public void performAction(float time, Event event)
        {
            float zoomAmount;

            if (event.getValue() < -0.2)
            { zoomAmount = -0.05f; }
            else if (event.getValue() > 0.2)
            { zoomAmount = 0.05f; }
            else
            { zoomAmount = 0.0f; }

            cameraRadius += zoomAmount;

            if (cameraRadius < 1.0f) cameraRadius = 1.0f;
            if (cameraRadius > 10.0f) cameraRadius = 10.0f;

            updateCameraPosition();
        }
    }

    public void adjustAzimuth(float amount)
    {
        cameraAzimuth += amount;
        cameraAzimuth = cameraAzimuth % 360;
    }
}
