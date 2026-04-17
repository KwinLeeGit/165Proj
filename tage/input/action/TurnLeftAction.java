package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class TurnLeftAction extends AbstractInputAction {
    private MyGame game;
    private GameObject av;
    private Vector4f turnLeftDirection;
    private float moveSpeed;

    public TurnLeftAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        av = game.getAvatar();
        moveSpeed = (float)(time * 0.01);
        Matrix4f rot = av.getWorldRotation();
        av.setLocalRotation(rot.rotate(moveSpeed,0,1,0));
    }
}
