package a2;

import tage.*;
import tage.shapes.*;
import tage.nodeControllers.RotationController;
import tage.physics.PhysicsController;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.input.*;
import tage.input.action.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.networking.client.*;
import tage.CameraController;
import tage.ai.*;

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
	private CameraController avatarCam;
	private AudioController audioController;
	private NPCController npcController;
	private MovementController moveController;
	private HUDController hudController;
	private PhysicsController physController;

	Vector3f newLocation, origin = new Vector3f(0f,0f,0f);
	Vector3f rot, pit;
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
	{	avatarS = new AnimatedShape("Bike3.rkm", "Bike3.rks");
		avatarS.loadAnimation("driveForward", "Bike3Fwd.rka");
		avatarS.loadAnimation("driveBackward", "Bike3Bkwd.rka");
		npcS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
		npcS.loadAnimation("driveForward", "forward.rka");
		npcS.loadAnimation("driveBackward", "backward.rka");
		ghostS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
		xAxisS = new Line(origin, new Vector3f(200,0,0));
		yAxisS = new Line(origin, new Vector3f(0,200,0));
		zAxisS = new Line(origin, new Vector3f(0,0,200));
		floorS = new TerrainPlane(1500);
	}

	@Override
	public void loadTextures()
	{	avatartx = new TextureImage("Bike3BlueTxt.jpg");
		npctx = new TextureImage("Bike2Txt.jpg");
		ghosttx = new TextureImage("Bike3Txt.jpg");
		floortx = new TextureImage("grid.jpg");
		boundaries = new TextureImage("boundaries.jpg");
	}

	@Override
	public void loadSounds() {
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
		initialScale = (new Matrix4f()).scaling(500.0f, 10.0f, 500.0f);
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

		avatarCam = new CameraController(engine, avatar);

		

		setupNetworking();

		audioController = new AudioController(engine, avatar, npc);
		audioController.loadSounds();
		audioController.initSound();

		hudController = new HUDController(engine);

	}

	private void initInputs() {
		im = engine.getInputManager();

		FwdAction fwdAction = new FwdAction(moveController);
		BkwdAction bkwdAction = new BkwdAction(moveController);
		TurnLeftAction turnLeftAction = new TurnLeftAction(moveController);
		TurnRightAction turnRightAction = new TurnRightAction(moveController);
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

		float camSpeed = (float)(frameTime * 0.005f);
		float moveSpeed = (float)(frameTime * 2.5f);
		float turnSpeed = (float)(frameTime * 1f);

		Vector3f fwd = avatar.getWorldForwardVector();
		Vector3f right = avatar.getLocalRightVector();



		if (!paused) elapsTime += (frameTime) / 1000.0;
		if (riding) {

			moveController.update(frameTime);
			avatarCam.updateRidingCamera();
			
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

		npcController.update();

		
		audioController.updateSound();

		if (!mouseInitiated) initMouseMode();

		hudController.update(elapsTime, gameState, counter);

		processNetworking((float)elapsTime);

		if ((moveForward || moveBackward || turnLeft || turnRight)
		&& protClient != null && isClientConnected) {
			protClient.sendMoveMessage(avatar.getWorldLocation());
		}

		physController.update(frameTime);
		physController.syncGameObjectsToPhysics();
	}

	@Override
	public void initializePhysicsObjects() {
		physController = new PhysicsController(engine);
		physController.initializePhysicsObjects(avatar, npc, floor, boundaries);
		npcController = new NPCController(npc, physController.getNpcPhysics(), avatar);
		moveController = new MovementController(avatar, avatarS, physController.getAvatarPhysics());
		initInputs();
	}

	public GameObject getAvatar() {return avatar;}
	public AnimatedShape getAnimatedAvatar() {return avatarS;}

	public void toggleCameraMode() {
		riding = !riding;
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