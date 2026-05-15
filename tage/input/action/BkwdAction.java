package tage.input.action;
import tage.GameObject;
import tage.input.MovementController;
import tage.input.action.AbstractInputAction;
import tage.shapes.AnimatedShape;
import net.java.games.input.Event;

import java.util.Vector;

import org.joml.*;

import a2.MyGame;

public class BkwdAction extends AbstractInputAction {
    private MovementController moveController;

    public BkwdAction(MovementController moveController) {
        this.moveController = moveController;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        moveController.setMoveBackward(val > 0.2f);
    }
}
