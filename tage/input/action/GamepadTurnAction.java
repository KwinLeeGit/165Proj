package tage.input.action;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;
import tage.input.MovementController;

public class GamepadTurnAction extends AbstractInputAction {
    private MovementController moveController;

    public GamepadTurnAction(MovementController moveController) {
        this.moveController = moveController;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        if (val < -0.2f) {
            moveController.setTurnLeft(true);
            moveController.setTurnRight(false);
        }
        else if (val > 0.2f) {
            moveController.setTurnLeft(false);
            moveController.setTurnRight(true);
        }
        else {
            moveController.setTurnLeft(false);
            moveController.setTurnRight(false);
        }
    }
}