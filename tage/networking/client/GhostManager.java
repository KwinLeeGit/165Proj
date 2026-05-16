package tage.networking.client;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.shapes.AnimatedShape;
import a2.MyGame;

public class GhostManager
{
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();

	public GhostManager(VariableFrameRateGame vfrg)
	{	game = (MyGame)vfrg;
	}
	
	public void createGhostAvatar(UUID id, Vector3f position, int bike, String color) throws IOException
	{	if (hasGhostAvatar(id)) return;
		System.out.println("adding ghost with ID --> " + id);
		AnimatedShape s = game.getGhostShape(bike);
		TextureImage t = game.getBikeTxt(bike, color);
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
	}
	
	public void removeGhostAvatar(UUID id)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		{	game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		}
		else
		{	System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	private GhostAvatar findAvatar(UUID id)
	{	GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext())
		{	ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0)
			{	return ghostAvatar;
			}
		}		
		return null;
	}
	
	public boolean updateGhostAvatar(UUID id, Vector3f position)
	{	GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null)
		{	ghostAvatar.setPosition(position);
			return true;
		}
		else
		{	System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
			return false;
		}
	}

	public void updateGhostAnimations(){
		for (GhostAvatar ghost : ghostAvatars) {
			ghost.updateAnimation();
		}
	}

	public boolean hasGhostAvatar(UUID id) {
    return findAvatar(id) != null;
	}
}
