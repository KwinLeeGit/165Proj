package tage.input.action;
import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import java.util.Vector;
import tage.input.*;
import org.joml.*;

import a2.MyGame;

public class FwdAction extends AbstractInputAction {
    private MovementController moveController;

    public FwdAction(MovementController moveController) {
        this.moveController = moveController;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        moveController.setMoveForward(val > 0.2f);

    }
}
