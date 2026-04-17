package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class BkwdAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector3f oldPosition, newPosition;
    private Vector4f bkwdDirection;
    private float moveSpeed;

    public BkwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        av = game.getAvatar();
        moveSpeed = (float)(time * 0.1);
        oldPosition = av.getWorldLocation();
        bkwdDirection = new Vector4f(0f,0f,-1f,1f);
        bkwdDirection.mul(av.getWorldRotation());
        bkwdDirection.mul(moveSpeed);
        newPosition = oldPosition.add(bkwdDirection.x(),bkwdDirection.y(),bkwdDirection.z());
        av.setLocalLocation(newPosition);
    }
}
