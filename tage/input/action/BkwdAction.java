package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class BkwdAction extends AbstractInputAction {
    private MyGame game;

    public BkwdAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        if(val > 0.2f)
            game.setMoveBackward(true);
        else
            game.setMoveBackward(false);
    }
}
