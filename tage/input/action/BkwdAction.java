package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import tage.shapes.AnimatedShape;
import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class BkwdAction extends AbstractInputAction {
    private MyGame game;
    private boolean isMoving;

    public BkwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        if(val > 0.2f) {
            game.setMoveBackward(true);
            if(!isMoving){
                game.getAnimatedAvatar().stopAnimation();
                game.getAnimatedAvatar().playAnimation("driveBackward", 0.25f, AnimatedShape.EndType.LOOP, 0);
                isMoving = true;
            }
        }
            
        else {
            game.setMoveBackward(false);
            game.getAnimatedAvatar().stopAnimation();
            isMoving = false;
        }
    }
}
