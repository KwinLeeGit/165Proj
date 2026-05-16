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
	private boolean paused=false;
	private String gameState = "play";
	private boolean muliplayer;
	private int selectedBike;
	private String selectedColor;
	private int counter=0;
	private int walls;
	private double lastFrameTime, currFrameTime, elapsTime, frameTime;

	private GameObject avatar, npc, xAxis, yAxis, zAxis, floor;
	private AnimatedShape avatarS, npcS, ghostBike1S, ghostBike2S;
	private ObjShape ghostS, xAxisS, yAxisS, zAxisS, floorS;
	private TextureImage avatartx, npctx, floortx, boundaries, ghosttx;
	private TextureImage bike1BlueTx, bike1GreenTx, bike1OrangeTx;
	private TextureImage bike2BlueTx, bike2GreenTx, bike2OrangeTx;
	private Light light1, light2, light3, light4;


	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private CameraController avatarCam;
	private AudioController audioController;
	private NPCController npcController;
	private MovementController moveController;
	private HUDController hudController;
	private PhysicsController physController;

	Vector3f origin = new Vector3f(0f,0f,0f);

	public MyGame(String serverAddress, int serverPort, String protocol, boolean muliplayer, int selectedBike, String selectedColor) { 
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.muliplayer = muliplayer;
		this.selectedBike = selectedBike;
		this.selectedColor = selectedColor;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;

		else
			this.serverProtocol = ProtocolType.UDP; 
		}

	public static void main(String[] args)
	{	
		String[] modes = {"Single Player", "Multiplayer"};
		int modeChoice = JOptionPane.showOptionDialog(
			null, 
			"Choose game mode", 
			"Bike Bash", 
			JOptionPane.DEFAULT_OPTION, 
			JOptionPane.QUESTION_MESSAGE, 
			null, 
			modes, 
			modes[0]
		);

		boolean muliplayer = modeChoice == 1;

		String[] bikes = {"Bike 1", "Bike 2"};
   	 	int bikeChoice = JOptionPane.showOptionDialog(
			null,
			"Choose your bike:",
			"Bike Select",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			bikes,
			bikes[0]
    	);

		int selectedBike = bikeChoice == 1 ? 2 : 1;
		
		String[] colors = {"Blue", "Green", "Orange"};
		int colorChoice = JOptionPane.showOptionDialog(
			null,
			"Choose your bike color:",
			"Color Select",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			colors,
			colors[0]
		);

		String selectedColor = colors[colorChoice < 0 ? 0 : colorChoice];
		
		
		
		
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2], muliplayer, selectedBike, selectedColor);
		engine = new Engine(game);
		engine.initializeSystem();
		game.buildGame();
		game.startGame();
	}

	@Override
	public void loadShapes()
	{	
		if (selectedBike == 1) {
        avatarS = new AnimatedShape("Bike1.rkm", "Bike1.rks");
        avatarS.loadAnimation("driveForward", "Bike1Fwd.rka");
        avatarS.loadAnimation("driveBackward", "Bike1Bkwd.rka");
    	} 
		
		else {
        avatarS = new AnimatedShape("Bike2.rkm", "Bike2.rks");
        avatarS.loadAnimation("driveForward", "Bike2Fwd.rka");
        avatarS.loadAnimation("driveBackward", "Bike2Bkwd.rka");
    	}

		ghostBike1S = new AnimatedShape("Bike1.rkm", "Bike1.rks");
        ghostBike1S.loadAnimation("driveForward", "Bike1Fwd.rka");
        ghostBike1S.loadAnimation("driveBackward", "Bike1Bkwd.rka");

		ghostBike2S = new AnimatedShape("Bike2.rkm", "Bike2.rks");
        ghostBike2S.loadAnimation("driveForward", "Bike2Fwd.rka");
        ghostBike2S.loadAnimation("driveBackward", "Bike2Bkwd.rka");
		
		npcS = new AnimatedShape("Bike1.rkm", "Bike1.rks");
		npcS.loadAnimation("driveForward", "Bike1Fwd.rka");
		npcS.loadAnimation("driveBackward", "Bike1Bkwd.rka");
		ghostS = new AnimatedShape("Bike1.rkm", "Bike1.rks");
		xAxisS = new Line(origin, new Vector3f(200,0,0));
		yAxisS = new Line(origin, new Vector3f(0,200,0));
		zAxisS = new Line(origin, new Vector3f(0,0,200));
		floorS = new TerrainPlane(1500);
	}

	@Override
	public void loadTextures()
	{	

		bike1BlueTx = new TextureImage("Bike1Blu.jpg");
		bike1GreenTx = new TextureImage("Bike1Grn.jpg");
		bike1OrangeTx = new TextureImage("Bike1Org.jpg");

		bike2BlueTx = new TextureImage("Bike2Blu.jpg");
		bike2GreenTx = new TextureImage("Bike2Grn.jpg");
		bike2OrangeTx = new TextureImage("Bike2Org.jpg");
		npctx = new TextureImage("Bike1Grn.jpg");
		ghosttx = new TextureImage("Bike1Org.jpg");
		floortx = new TextureImage("grid.jpg");
		boundaries = new TextureImage("boundaries.jpg");

		avatartx = getBikeTxt(selectedBike, selectedColor);
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



		if (!paused) {
			elapsTime += (frameTime) / 1000.0;
			moveController.update(frameTime);
			npcController.update();

		} 

		physController.update(frameTime);
		physController.syncGameObjectsToPhysics();

		avatarCam.updateRidingCamera();
		
		audioController.updateSound();

		hudController.update(elapsTime, gameState, counter);

		processNetworking((float)elapsTime);

		if ((moveController.isMovingOrTurning())
		&& protClient != null && isClientConnected) {
			protClient.sendMoveMessage(avatar.getWorldLocation());
		}

		gm.updateGhostAnimations();

		
	}

	public TextureImage getBikeTxt(int bike, String color) {
		if (bike == 1){
			if (color.equals("Blue")) return bike1BlueTx;
			if (color.equals("Green")) return bike1GreenTx;
			if (color.equals("Orange")) return bike1OrangeTx;
		}

		if (bike == 2){
			if (color.equals("Blue")) return bike2BlueTx;
			if (color.equals("Green")) return bike2GreenTx;
			if (color.equals("Orange")) return bike2OrangeTx;
		}

		return bike1BlueTx;
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

	public void pauseGame() {
		paused = !paused;
	}

	public AnimatedShape getGhostShape(int bike) {
		if (bike == 1) return ghostBike1S;
		return ghostBike2S;
	}
	public TextureImage getGhostTexture() {return ghosttx;}
	public GhostManager getGhostManager() {return gm;}
	public Engine getEngine () {return engine;}
	public int getSelectedBike() {return selectedBike;}
	public String getSelectedColor() {return selectedColor;} 

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