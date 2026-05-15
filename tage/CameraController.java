package tage;

import tage.*;
import a2.MyGame;
import org.joml.*;

public class CameraController {
    private Engine engine;
    private GameObject target;
    private Vector3f smoothCamLoc = new Vector3f(0f, 5f, -10f);
    private Vector3f smoothLookTarget = new Vector3f(0f,0f,0f);
    private float camSmooth = 0.15f;
    private	float lookSmooth = 0.2f;

    public CameraController(Engine engine, GameObject target) {
        this.engine = engine;
        this.target = target;
    }

    public void updateRidingCamera() {

		Camera cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());

		Vector3f targetFwd = target.getWorldForwardVector();
		Vector3f targetLoc = target.getWorldLocation();
		Vector3f targetUp = target.getWorldUpVector();

		

		Vector3f camLoc = new Vector3f(targetLoc)
        .add(new Vector3f(targetUp).mul(5f))
		.add(new Vector3f(targetFwd).mul(-9f));

		Vector3f desiredLookTarget = new Vector3f(targetLoc)
        .add(new Vector3f(targetUp).mul(1.5f));

		smoothCamLoc.lerp(camLoc, camSmooth);
		smoothLookTarget.lerp(desiredLookTarget, lookSmooth);

		cam.setLocation(smoothCamLoc);
		cam.lookAt(smoothLookTarget);
	}

}
