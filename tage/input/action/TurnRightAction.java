package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class TurnRightAction extends AbstractInputAction {
    private MyGame game;

    public TurnRightAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        if(val > 0.2f)
            game.setTurnRight(true);
        else
            game.setTurnRight(false);
    }
}
