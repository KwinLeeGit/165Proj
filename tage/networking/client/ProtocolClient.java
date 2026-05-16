package tage.networking.client;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.networking.client.GameConnectionClient;
import a2.MyGame;

public class ProtocolClient extends GameConnectionClient
{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	
	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException 
	{	super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	}
	
	public UUID getID() { return id; }
	
	@Override
	protected void processPacket(Object message)
	{	String strMessage = (String)message;
		System.out.println("message received -->" + strMessage);
		String[] messageTokens = strMessage.split(",");
		
		// Game specific protocol to handle the message
		if(messageTokens.length > 0)
		{
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
			if(messageTokens[0].compareTo("join") == 0)
			{	if(messageTokens[1].compareTo("success") == 0)
				{	System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				}
				if(messageTokens[1].compareTo("failure") == 0)
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
			}	}
			
			// Handle BYE message
			// Format: (bye,remoteId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}
			
			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0))
			{	// create a new ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));

				int bike = Integer.parseInt(messageTokens[5]);
				String color = messageTokens[6];

				try
				{	if (!ghostManager.hasGhostAvatar(ghostID)) {
						ghostManager.createGhostAvatar(ghostID, ghostPosition, bike, color);
					} else {
						ghostManager.updateGhostAvatar(ghostID, ghostPosition);
					}
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
			}
			
			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0)
			{
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			}
			
			// Handle MOVE message
			// Format: (move,remoteId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0)
			{

				if (messageTokens.length < 7) return;
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				if (ghostID.equals(id)) return;
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4])
				);
				
				int bike = Integer.parseInt(messageTokens[5]);
				String color = messageTokens[6];

				if (!ghostManager.hasGhostAvatar(ghostID)) {
					try {
						ghostManager.createGhostAvatar(ghostID, ghostPosition, bike, color);
					} catch (IOException e) {
						System.out.println("error creating ghost avatar from move packet");
					}
				} 
				else {
					ghostManager.updateGhostAvatar(ghostID, ghostPosition);
				}
				
			}
			
						// Handle HIT message
			// Format: hit,targetId,shooterId,damage
			if (messageTokens[0].compareTo("hit") == 0) {
				UUID targetId = UUID.fromString(messageTokens[1]);
				UUID shooterId = UUID.fromString(messageTokens[2]);
				int damage = Integer.parseInt(messageTokens[3]);

				if (targetId.equals(id)) {
					boolean died = game.damagePlayer(damage);

					if (died) {
						game.respawn();
						sendDeathMessage(shooterId);
					}
				}
			}

			// Handle DEAD message
			// Format: dead,victimId,killerId
			if (messageTokens[0].compareTo("dead") == 0) {
				UUID victimId = UUID.fromString(messageTokens[1]);
				UUID killerId = UUID.fromString(messageTokens[2]);

				if (killerId.equals(id)) {
					game.addScore(1);
					System.out.println("Score = " + game.getScore());

					if (game.isGameOver()) {
						sendGameOverMessage(victimId);
					}
				}
			}

			if (messageTokens[0].compareTo("gameover") == 0) {
				UUID winnerId = UUID.fromString(messageTokens[1]);

				if (winnerId.equals(id)) {
					game.addScore(0);
				} else {
					game.loseGame();
				}
			}
		}	
	}
	
	// The initial message from the game client requesting to join the 
	// server. localId is a unique identifier for the client. Recommend 
	// a random UUID.
	// Message Format: (join,localId)
	
	public void sendJoinMessage()
	{	try 
		{	sendPacket(new String("join," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the client is leaving the server. 
	// Message Format: (bye,localId)

	public void sendByeMessage()
	{	try 
		{	sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the clients Avatars position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position)
	{	try 
		{	String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + game.getSelectedBike();
			message += "," + game.getSelectedColor();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the local avatar's position. The server then 
	// forwards this message to the client with the ID value matching remoteId. 
	// This message is generated in response to receiving a WANTS_DETAILS message 
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position)
	{	try 
		{	String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + game.getSelectedBike();
			message += "," + game.getSelectedColor();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the local avatar has changed position.  
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessage(Vector3f position)
	{	try 
		{	String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + game.getSelectedBike();
        	message += "," + game.getSelectedColor();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	public void sendHitMessage(UUID targetId, int damage) {
		try {
			String message = "hit," + targetId.toString();
			message += "," + id.toString();
			message += "," + damage;

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDeathMessage(UUID killerId) {
		try {
			String message = "dead," + id.toString();
			message += "," + killerId.toString();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendGameOverMessage(UUID loserId) {
		try {
			String message = "gameover," + id.toString() + "," + loserId.toString();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
