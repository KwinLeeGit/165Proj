package tage.input.action;
import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class FwdAction extends AbstractInputAction {
    private MyGame game;
    private boolean isMoving;

    public FwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        if(val > 0.2f) {
            game.setMoveForward(true);
            if(!isMoving){
                game.getAnimatedAvatar().stopAnimation();
                game.getAnimatedAvatar().playAnimation("driveForward", 0.25f, AnimatedShape.EndType.LOOP, 0);
                isMoving = true;
            }
            
        }
            
        else {
            game.setMoveForward(false);
            game.getAnimatedAvatar().stopAnimation();
            isMoving = false;
        }
            

    }
}
