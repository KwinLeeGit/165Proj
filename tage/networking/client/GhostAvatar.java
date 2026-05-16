package tage.networking.client;

import java.util.UUID;

import tage.*;
import tage.rml.Vector3f;
import tage.shapes.AnimatedShape;

import org.joml.*;

// A ghost MUST be connected as a child of the root,
// so that it will be rendered, and for future removal.
// The ObjShape and TextureImage associated with the ghost
// must have already been created during loadShapes() and
// loadTextures(), before the game loop is started.

public class GhostAvatar extends GameObject
{
	private UUID uuid;
	private AnimatedShape animatedShape;
	private boolean moving = false;
	private Vector3f lastPos = null;
	private float moveThreshold = 1f;

	public GhostAvatar(UUID id, AnimatedShape s, TextureImage t, Vector3f p) 
	{	super(GameObject.root(), s, t);
		uuid = id;
		animatedShape = s;
		setPosition(p);
	}
	
	public UUID getID() { return uuid; }
	public void setPosition(Vector3f m) {
		if (lastPos == null) {
			lastPos = new Vector3f(m);
			setLocalLocation(m); 
			return;
		}

		float dist = lastPos.distance(m);

		setLocalLocation(m); 
		
		if (dist > moveThreshold) {
			playMovingAnimation();
		}

		else {
			stopMovingAnimation();
		}

		lastPos.set(m);
	}
	public void updateAnimation() {
		if (moving) {
			animatedShape.updateAnimation();
		}
	}

	public Vector3f getPosition() { return getWorldLocation(); }

	private void playMovingAnimation() {
		if (!moving) {
            animatedShape.stopAnimation();
            animatedShape.playAnimation(
                "driveForward",
                .25f,
                AnimatedShape.EndType.LOOP,
                0
            );
            moving = true;
        }
	}

	public void stopMovingAnimation() {
		if(moving) {
			animatedShape.stopAnimation();
			moving = false;
		}
	}
}
