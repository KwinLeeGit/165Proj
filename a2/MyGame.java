package a2;

import tage.*;
import tage.shapes.*;
import tage.nodeControllers.RotationController;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.input.*;
import tage.input.action.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.networking.client.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;
import tage.audio.*;

import java.net.UnknownHostException;
import javax.swing.*;
import org.joml.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;


public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;
	private boolean mouseInitiated = false;
	private boolean paused=false;
	private boolean riding=true;
	private boolean initPhys = false;
	private boolean moveForward, moveBackward, turnLeft, turnRight;
	private String gameState = "play";
	private int counter=0;
	private int walls;
	private double lastFrameTime, currFrameTime, elapsTime, frameTime;

	private GameObject avatar, npc, xAxis, yAxis, zAxis, floor;
	private AnimatedShape avatarS, npcS;
	private ObjShape ghostS, xAxisS, yAxisS, zAxisS, floorS;
	private TextureImage avatartx, npctx, floortx, boundaries, ghosttx;
	private Light light1, light2, light3, light4;
	private PhysicsObject avatarP, npcP, floorP;
	private PhysicsEngine physicsEngine;
	private float currentSpeed = 0;
	private float accel = 5f;
	private float maxSpeed = 20f;
	private float drag = 2f;
	private float adj;
	private IAudioManager audioManager;
	private Sound engSound, engSound2;


	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private float avatarRadius = 3.0f;
	private float yaw;
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

	public MyGame(String serverAddress, int serverPort, String protocol) { 
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;

		else
			this.serverProtocol = ProtocolType.UDP; 
		}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();
	}

	@Override
	public void loadShapes()
	{	avatarS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
		avatarS.loadAnimation("driveForward", "forward.rka");
		avatarS.loadAnimation("driveBackward", "backward.rka");
		npcS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
		npcS.loadAnimation("driveForward", "forward.rka");
		npcS.loadAnimation("driveBackward", "backward.rka");
		ghostS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
		xAxisS = new Line(origin, new Vector3f(200,0,0));
		yAxisS = new Line(origin, new Vector3f(0,200,0));
		zAxisS = new Line(origin, new Vector3f(0,0,200));
		floorS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures()
	{	avatartx = new TextureImage("BikeTxt.jpg");
		npctx = new TextureImage("Bike2Txt.jpg");
		ghosttx = new TextureImage("Bike3Txt.jpg");
		floortx = new TextureImage("grid.jpg");
		boundaries = new TextureImage("boundaries.jpg");
	}

	@Override
	public void loadSounds() {
		audioManager = engine.getAudioManager();

		AudioResource engineRes = audioManager.createAudioResource("engine.wav", AudioResourceType.AUDIO_SAMPLE);
		AudioResource engineRes2 = audioManager.createAudioResource("engine.wav", AudioResourceType.AUDIO_SAMPLE);


		engSound = new Sound(engineRes, SoundType.SOUND_EFFECT, 0, true);
		engSound2 = new Sound(engineRes2, SoundType.SOUND_EFFECT, 100, true);

		engSound.initialize(audioManager);
		engSound2.initialize(audioManager);

		engSound.setMaxDistance(50f);
		engSound.setMinDistance(0.5f);
		engSound.setRollOff(5f);

		engSound2.setMaxDistance(1000f);
		engSound2.setMinDistance(5f);
		engSound2.setRollOff(1f);
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		SceneGraph sg = engine.getSceneGraph();

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		initialTranslation = (new Matrix4f()).translation(0,5,0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);

		npc = new GameObject(GameObject.root(), npcS, npctx);
		initialTranslation = (new Matrix4f()).translation(10,5,10);
		initialScale = (new Matrix4f()).scaling(1.0f);
		npc.setLocalTranslation(initialTranslation);
		npc.setLocalScale(initialScale);

		xAxis = new GameObject(GameObject.root(), xAxisS);
		yAxis = new GameObject(GameObject.root(), yAxisS);
		zAxis = new GameObject(GameObject.root(), zAxisS);

		floor = new GameObject(GameObject.root(), floorS, floortx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(100.0f, 5.0f, 100.0f);
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

		setupNetworking();

		engSound.setLocation(avatar.getWorldLocation());
		engSound2.setLocation(npc.getWorldLocation());
		setEarParameters();
		//engSound.play();	
		engSound2.play();	
		
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

	public void setEarParameters()
	{ Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioManager.getEar().setLocation(camera.getLocation());
		audioManager.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	public void loadSkyBoxes() {

		walls = (engine.getSceneGraph()).loadCubeMap("newWalls");

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
		adj = (float)(frameTime/1000.0);

		float camSpeed = (float)(frameTime * 0.005f);
		float moveSpeed = (float)(frameTime * 2.5f);
		float turnSpeed = (float)(frameTime * 1f);
		adj = (float)(frameTime/1000.0);

		Vector3f fwd = avatar.getWorldForwardVector();

		if (!paused) elapsTime += (frameTime) / 1000.0;
		if (riding) {

			if (moveForward) {

				avatarP.applyForce(fwd.x() * moveSpeed, 0, fwd.z() * moveSpeed, 0, 0, 0);
			}

    		if (moveBackward) {

				avatarP.applyForce(-fwd.x() * moveSpeed, 0, -fwd.z() * moveSpeed, 0, 0, 0);
			}

			if (turnLeft) {
				avatarP.applyTorque(0, turnSpeed, 0);
			}

			if (turnRight) {
				avatarP.applyTorque(0, -turnSpeed, 0);
			}


			avatarS.updateAnimation();
			updateRidingCamera();
			
		}

		else {

			Vector3f camLoc = cam.getLocation();

			if (moveForward)
				camLoc.add(new Vector3f(fwd).mul(camSpeed));

			if (moveBackward)
				camLoc.add(new Vector3f(fwd).mul(-camSpeed));

			if (turnLeft)
				camLoc.add(new Vector3f(right).mul(-camSpeed));

			if (turnRight)
				camLoc.add(new Vector3f(right).mul(camSpeed));

    		cam.setLocation(camLoc);
			updateFreeCamera();
		}

		Vector3f toPlayer = new Vector3f(avatar.getWorldLocation())
		.sub(npc.getWorldLocation());

		float distance = toPlayer.length();

		if (distance > 5f) {
			toPlayer.normalize();

			// Turn toward player
			Vector3f npcForward = npc.getWorldForwardVector();
			float turn = npcForward.cross(toPlayer).y;

			npcP.applyTorque(0, turn * 5f, 0);

			// Move forward
			npcP.applyForce(npcForward.x() * 10f, 0, npcForward.z() * 10f, 0, 0, 0);
		}

		physicsEngine.update((float)frameTime/1000f);
		for(GameObject go:engine.getSceneGraph().getGameObjects()) {
			if(go.getPhysicsObject() != null) {
				Vector3f loc = go.getPhysicsObject().getLocation();
				Matrix4f locMat = new Matrix4f();
				locMat.set(3,0,loc.x);
				locMat.set(3,1,loc.y);
				locMat.set(3,2,loc.z);
				go.setLocalTranslation(locMat);

				Quaternionf rot = go.getPhysicsObject().getRotation();
				Matrix4f rotMat = new Matrix4f();
				rot.get(rotMat);
				go.setLocalRotation(rotMat);
					
			};
		}

		//engSound.setLocation(avatar.getWorldLocation());
		engSound2.setLocation(npc.getWorldLocation());
		setEarParameters();

		if (!mouseInitiated) initMouseMode();

		// build and set HUD
		String counterStr = Integer.toString(counter);
		String elapsTimeStr = Integer.toString((int)elapsTime);
		String dispStr1 = "Time = " + elapsTimeStr;
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

		processNetworking((float)elapsTime);

		if ((moveForward || moveBackward || turnLeft || turnRight)
		&& protClient != null && isClientConnected) {
			protClient.sendMoveMessage(avatar.getWorldLocation());
		}
		

		
	}

	@Override
	public void initializePhysicsObjects() {
		float[] gravity = {0f, -15f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		float mass = 1f;
		float up[] = {0,1,0};
		float radius = .75f;
		float size[] = {2,2,5};
		Vector3f loc;
		Quaternionf rot;

		loc = avatar.getWorldLocation(); rot = new Quaternionf();
		(avatar.getWorldRotation()).getNormalizedRotation(rot);
		avatarP = (engine.getSceneGraph()).addPhysicsBox(mass, loc, rot, size);
		avatarP.setFriction(.5f);
		avatarP.disableSleeping();
		avatarP.setDamping(.6f, .8f);
		avatarP.setBounciness(0);
		avatarP.setAngularFactor(up);
		avatar.setPhysicsObject(avatarP);

		loc = npc.getWorldLocation(); rot = new Quaternionf();
		(npc.getWorldRotation()).getNormalizedRotation(rot);
		npcP = (engine.getSceneGraph()).addPhysicsBox(mass, loc, rot, size);
		npcP.setFriction(1f);
		npcP.disableSleeping();
		npc.setPhysicsObject(npcP);

		loc = floor.getWorldLocation(); rot = new Quaternionf();
		(floor.getWorldRotation()).getNormalizedRotation(rot);
		floorP = (engine.getSceneGraph()).addPhysicsStaticTerrainMesh(loc, rot, boundaries, 100f, 5f, 100);
		floorP.setFriction(.5f);
		floorP.setBounciness(0);
		floorP.disableSleeping();
		floor.setPhysicsObject(floorP);
		
		engine.enableGraphicsWorldRender();
		engine.enablePhysicsWorldRender();
	}

	public GameObject getAvatar() {return avatar;}
	public AnimatedShape getAnimatedAvatar() {return avatarS;}

	public void toggleCameraMode() {
		riding = !riding;
	}

	private void updateRidingCamera() {

		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());

		fwd = avatar.getWorldForwardVector();
		loc = avatar.getWorldLocation();
		up = avatar.getWorldUpVector();
		right = avatar.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.lookAt(avatar);
		Vector3f camLoc = new Vector3f(loc).add(new Vector3f(up).mul(5f))
		.add(new Vector3f(fwd).mul(-9f));

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

	public ObjShape getGhostShape() {return ghostS;}
	public TextureImage getGhostTexture() {return ghosttx;}
	public GhostManager getGhostManager() {return gm;}
	public Engine getEngine () {return engine;}

	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
}