package tage.input.action;
import tage.GameObject;
import tage.input.MovementController;
import tage.input.action.AbstractInputAction;

import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class TurnRightAction extends AbstractInputAction {
    private MovementController moveController;;

    public TurnRightAction(MovementController moveController) {
        this.moveController = moveController;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        moveController.setTurnRight(val > 0.2f);
    }
}
