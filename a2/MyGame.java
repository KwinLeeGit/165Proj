package a2;

import tage.*;
import tage.shapes.*;
import tage.nodeControllers.RotationController;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.rml.Vector3;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;


public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private boolean mouseInitiated = false;
	private boolean paused=false;
	private boolean riding=true;
	private boolean prox = false;
	private boolean pic1 = false;
	private boolean pic2 = false;
	private boolean pic3 = false;
	private boolean moveForward, moveBackward, turnLeft, turnRight;
	private String gameState = "play";
	private int counter=0;
	private int walls;
	private double lastFrameTime, currFrameTime, elapsTime, frameTime;

	private GameObject dol, pyr1, pyr2, pyr3, xAxis, yAxis, zAxis, home, cap1, cap2, cap3, floor;
	private ObjShape dolS, pyrS, xAxisS, yAxisS, zAxisS, homeS, capS, floorS;
	private TextureImage doltx, pyr1tx, pyr2tx, pyr3tx, hometx, cap1tx, cap2tx, cap3tx, floortx, boundaries;
	private Light light1, light2, light3, light4;
	private PhysicsObject dolPhy, pyr1Phy, pyr2Phy, pyr3Phy;
	private float dolRadius = 3.0f;
    private float pyr1Radius = 5.0f;
    private float pyr2Radius = 1.0f;
    private float pyr3Radius = 3.0f;
	private float yawAngle = 0.0f;
	private float pitchAngle = 0.0f;
	private Robot robot;
	private float curMouseX, curMouseY, centerX, centerY, prevMouseX, prevMouseY;
	private boolean isRecentering;
	private Canvas canvas;

	Vector3f loc, fwd, up, right, newLocation, origin = new Vector3f(0,0,0);
	Matrix4f rot, pit;
	float turn = 0, pitch = 0;
	Camera cam;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();
	}

	@Override
	public void loadShapes()
	{	dolS = new ImportedModel("dolphinHighPoly.obj");
		pyrS = new ManualPyramid();
		xAxisS = new Line(origin, new Vector3f(200,0,0));
		yAxisS = new Line(origin, new Vector3f(0,200,0));
		zAxisS = new Line(origin, new Vector3f(0,0,200));
		homeS = new ManualHome();
		capS = new Plane();
		floorS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("Dolphin_HighPolyUV.jpg");
		pyr1tx = new TextureImage("ice.jpg");
		pyr2tx = new TextureImage("brick1.jpg");
		pyr3tx = new TextureImage("earth.jpg");
		hometx = new TextureImage("brick1.jpg");
		cap1tx = pyr1tx;
		cap2tx = pyr2tx;
		cap3tx = pyr3tx;
		floortx = new TextureImage("grid.jpg");
		boundaries = new TextureImage("boundaries.jpg");
		TextureImage [] skyboxtx = new TextureImage[6];
		for (int i = 0; i<6; i++) {
			skyboxtx[i] = new TextureImage("walls.jpg");
		}
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		SceneGraph sg = engine.getSceneGraph();

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(3.0f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);
		dolPhy = sg.addPhysicsSphere(1.0f, new Vector3f(0,0,0), new Quaternionf(), 3.0f);
		dol.setPhysicsObject(dolPhy);

		pyr1 = new GameObject(GameObject.root(), pyrS, pyr1tx);
		initialTranslation = (new Matrix4f()).translation(15,0,0);
		initialScale = (new Matrix4f()).scaling(5.0f);
		pyr1.setLocalTranslation(initialTranslation);
		pyr1.setLocalScale(initialScale);
		pyr1Phy = sg.addPhysicsSphere(0.0f, new Vector3f(15,0,0), new Quaternionf(), 5.0f);
		pyr1.setPhysicsObject(pyr1Phy);
		
		pyr2 = new GameObject(GameObject.root(), pyrS, pyr2tx);
		initialTranslation = (new Matrix4f()).translation(15,0,15);
		initialScale = (new Matrix4f()).scaling(1.0f);
		pyr2.setLocalTranslation(initialTranslation);
		pyr2.setLocalScale(initialScale);
		pyr2Phy = sg.addPhysicsSphere(0.0f, new Vector3f(15,0,15), new Quaternionf(), 1.0f);
		pyr2.setPhysicsObject(pyr2Phy);

		pyr3 = new GameObject(GameObject.root(), pyrS, pyr3tx);
		initialTranslation = (new Matrix4f()).translation(0,0,15);
		initialScale = (new Matrix4f()).scaling(3.0f);
		pyr3.setLocalTranslation(initialTranslation);
		pyr3.setLocalScale(initialScale);
		pyr3Phy = sg.addPhysicsSphere(0.0f, new Vector3f(0,0,15), new Quaternionf(), 3.0f);
		pyr3.setPhysicsObject(pyr3Phy);

		xAxis = new GameObject(GameObject.root(), xAxisS);
		yAxis = new GameObject(GameObject.root(), yAxisS);
		zAxis = new GameObject(GameObject.root(), zAxisS);

		home = new GameObject(GameObject.root(), homeS, hometx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(10.0f);
		home.setLocalTranslation(initialTranslation);
		home.setLocalScale(initialScale);
		home.getRenderStates().hasLighting(true);

		floor = new GameObject(GameObject.root(), floorS, floortx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(50.0f, 1.0f, 50.0f);
		floor.setLocalTranslation(initialTranslation);
		floor.setLocalScale(initialScale);
		floor.setHeightMap(boundaries);
		floor.getRenderStates().setTiling(1);
		floor.getRenderStates().setTileFactor(10);
	
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(0, 20, 0));
		light2 = new Light();
		light2.setLocation(new Vector3f(15,20,0));
		light3 = new Light();
		light3.setLocation(new Vector3f(15,20,15));
		light4 = new Light();
		light4.setLocation(new Vector3f(0,20,15));
		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(light2);
		(engine.getSceneGraph()).addLight(light3);
		(engine.getSceneGraph()).addLight(light4);
	}

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));

		//Input Section
		initInputs();
		
	}

	private void initInputs() {
		im = engine.getInputManager();

		FwdAction fwdAction = new FwdAction(this);
		BkwdAction bkwdAction = new BkwdAction(this);
		TurnLeftAction turnLeftAction = new TurnLeftAction(this);
		TurnRightAction turnRightAction = new TurnRightAction(this);
		CamToggleAction camToggleAction = new CamToggleAction(this);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.W, fwdAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_AND_RELEASE
		);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.S, bkwdAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_AND_RELEASE
		);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.A, turnLeftAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_AND_RELEASE
		);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.D, turnRightAction,
			InputManager.INPUT_ACTION_TYPE.REPEAT_AND_RELEASE
		);

		im.associateActionWithAllKeyboards(
			net.java.games.input.Component.Identifier.Key.SPACE, camToggleAction,
			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
		);

	}

	@Override
	public void loadSkyBoxes() {

		walls = (engine.getSceneGraph()).loadCubeMap("walls");

		(engine.getSceneGraph()).setActiveSkyBoxTexture(walls);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void update()
	{	
		im.update((float)elapsTime);
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		frameTime = currFrameTime - lastFrameTime;

		float moveSpeed = (float)(frameTime * 0.01);
		float turnSpeed = (float)(frameTime * 0.002);

		if (!paused) elapsTime += (frameTime) / 1000.0;
		if (riding) {

			if (moveForward) {
				Vector3f fwd = dol.getWorldForwardVector();
				Vector3f loc = dol.getWorldLocation();
				dol.setLocalLocation(loc.add(new Vector3f(fwd).mul(moveSpeed)));
				heightAdjust();
			}

    		if (moveBackward) {
				Vector3f fwd = dol.getWorldForwardVector();
				Vector3f loc = dol.getWorldLocation();
				dol.setLocalLocation(loc.add(new Vector3f(fwd).mul(-moveSpeed)));
				heightAdjust();
			}

			if (turnLeft) {
				Matrix4f rot = dol.getWorldRotation();
				dol.setLocalRotation(rot.rotate(turnSpeed, 0, 1, 0));
			}

			if (turnRight) {
				Matrix4f rot = dol.getWorldRotation();
				dol.setLocalRotation(rot.rotate(-turnSpeed, 0, 1, 0));
			}
			updateRidingCamera();
		}

		else {

			Vector3f camLoc = cam.getLocation();

			if (moveForward)
				camLoc.add(new Vector3f(fwd).mul(moveSpeed));

			if (moveBackward)
				camLoc.add(new Vector3f(fwd).mul(-moveSpeed));

			if (turnLeft)
				camLoc.add(new Vector3f(right).mul(-moveSpeed));

			if (turnRight)
				camLoc.add(new Vector3f(right).mul(moveSpeed));

    		cam.setLocation(camLoc);
			updateFreeCamera();
		}

		if (!mouseInitiated) initMouseMode();

		// calculate distances
		float dist1 = dol.getWorldLocation().distance(pyr1.getWorldLocation());
		float dist2 = dol.getWorldLocation().distance(pyr2.getWorldLocation());
		float dist3 = dol.getWorldLocation().distance(pyr3.getWorldLocation());

		// check collisions
		if (dist1 < 0.8*(dolRadius + pyr1Radius) ||
			dist2 < 0.8*(dolRadius + pyr2Radius)||
			dist3 < 0.8*(dolRadius + pyr3Radius)) {
			gameState = "lose";
			paused= true;
		}

		if (dist1 < 1.1*(dolRadius + pyr1Radius) ||
			dist2 < 1.1*(dolRadius + pyr2Radius) ||
			dist3 < 1.1*(dolRadius + pyr3Radius)) {
			prox = true;
		}
		else{prox = false;}

		Vector3f dolLoc = dol.getWorldLocation();
		dolPhy.setLocation(new float[] {
        dolLoc.x(),
        dolLoc.y(),
        dolLoc.z()
		});

		if (counter >= 3) {
			gameState = "win";
		}



		// build and set HUD
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Distance: Too Far";
		if (prox == true){
			dispStr1 = "Distance: Close Enough";
		}
		String dispStr2 = "Score = " + counterStr;
		if (gameState.equals("lose")){
			dispStr1 = "Game Over!";
		}
		if (gameState.equals("win")){
			dispStr2 = "You Win!";
		}
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);

		
	}

	public GameObject getAvatar() {return dol;}

	public void toggleCameraMode() {
		riding = !riding;
	}

	public void heightAdjust() {
		Vector3f loc = dol.getWorldLocation();
		float height = floor.getHeight(loc.x(), loc.z());

		dol.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
	}

	private void updateRidingCamera() {

		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());

		fwd = dol.getWorldForwardVector();
		loc = dol.getWorldLocation();
		up = dol.getWorldUpVector();
		right = dol.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		Vector3f camLoc = new Vector3f(loc).add(new Vector3f(up).mul(1.3f))
		.add(new Vector3f(fwd).mul(-2.5f));

		cam.setLocation(camLoc);
	}

	private void updateFreeCamera() {
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();

		//Calculate forward vector from yaw + pitch
		Vector3f forward = new Vector3f(
			(float)(Math.cos(pitchAngle) * Math.sin(yawAngle)),
			(float)(Math.sin(pitchAngle)),
			(float)(Math.cos(pitchAngle) * Math.cos(yawAngle))
		);

		forward.normalize();

		//Calculate right vector
		Vector3f worldUp = new Vector3f(0, 1, 0);
		Vector3f right = new Vector3f();
		forward.cross(worldUp, right).normalize();

		//Calculate true up vector
		Vector3f up = new Vector3f();
		right.cross(forward, up).normalize();

		//Apply to camera
		cam.setN(forward);
		cam.setU(right);
		cam.setV(up);

		this.fwd = forward;
		this.right = right;
		this.up = up;
	}

	public void setMoveForward(boolean val) { moveForward = val; }
	public void setMoveBackward(boolean val) { moveBackward = val; }
	public void setTurnLeft(boolean val) { turnLeft = val; }
	public void setTurnRight(boolean val) { turnRight = val; }

	@Override
	public void mouseMoved(MouseEvent e)
		{ // if robot is recentering and the MouseEvent location is in the center,
		// then this event was generated by the robot
		if (mouseInitiated)
		{ if (isRecentering &&
		centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
		{ // mouse recentered, recentering complete
		isRecentering = false;
		}
		else
		{ // event was due to a user mouse-move, and must be processed
		curMouseX = e.getXOnScreen();
		curMouseY = e.getYOnScreen();
		float mouseDeltaX = prevMouseX - curMouseX;
		float mouseDeltaY = prevMouseY - curMouseY;
		if(!riding) {
			yaw(mouseDeltaX);
			pitch(mouseDeltaY);
		}
		prevMouseX = curMouseX;
		prevMouseY = curMouseY;
		// tell robot to put the cursor to the center (since user just moved it)
		recenterMouse();
		prevMouseX = centerX; // reset prev to center
		prevMouseY = centerY;
		}
		} 
	}

	public void pauseGame() {
		paused = !paused;
	}

	public void tryPic(){

		float dist1 = dol.getWorldLocation().distance(pyr1.getWorldLocation());
		float dist2 = dol.getWorldLocation().distance(pyr2.getWorldLocation());
		float dist3 = dol.getWorldLocation().distance(pyr3.getWorldLocation());

		if (prox == true){
			if (dist1 < 1.25*(dolRadius + pyr1Radius) && pic1 == false){
				counter++;
				pic1 = true;
			}
			else if(dist2 < 1.25*(dolRadius + pyr2Radius) && pic2 == false) {
				counter++;
				pic2 = true;
			}
			else if(dist3 < 1.25*(dolRadius + pyr3Radius) && pic3 == false) {
				counter++;
				pic3 = true;
			}
		}
	}

	private void initMouseMode()
	{ 	
		mouseInitiated = true;
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("MAIN");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		centerX = (int) (left + width/2);
		centerY = (int) (bottom - height/2);
		isRecentering = false;
		try // note that some platforms may not support the Robot class
		{ robot = new Robot(); } catch (AWTException ex)
		{ throw new RuntimeException("Couldn't create Robot!"); }
		recenterMouse();
		prevMouseX = centerX; // 'prevMouse' defines the initial
		prevMouseY = centerY; // mouse position
		// also change the cursor
		Image faceImage = new
		ImageIcon("./assets/textures/face.gif").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().
		createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
	}

	private void recenterMouse()
	{ // use the robot to move the mouse to the center point.
		// Note that this generates one MouseEvent.
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("MAIN");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		int centerX = (int) (left + width/2.0f);
		int centerY = (int) (bottom - height/2.0f);
		isRecentering = true;
		robot.mouseMove((int)centerX, (int)centerY);
	}

	public void pitch(float mouseDeltaY)
	{
		float sensitivity = 0.002f;
		pitchAngle += mouseDeltaY * sensitivity;

		float maxPitch = (float)Math.toRadians(85.0);
		if (pitchAngle > maxPitch) pitchAngle = maxPitch;
    	if (pitchAngle < -maxPitch) pitchAngle = -maxPitch;
	}

	public void yaw(float mouseDeltaX)
	{ 
		float sensitivity = 0.002f;
		yawAngle += mouseDeltaX * sensitivity;
	}
}