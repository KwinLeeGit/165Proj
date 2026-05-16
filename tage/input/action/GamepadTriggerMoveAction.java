package tage.input.action;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;
import tage.input.MovementController;

public class GamepadTriggerMoveAction extends AbstractInputAction {
    private MovementController moveController;

    public GamepadTriggerMoveAction(MovementController moveController) {
        this.moveController = moveController;
    }

    @Override
    public void performAction(float time, Event e) {
        float val = e.getValue();

        // Usually LT = negative, RT = positive
        if (val > 0.2f) {
            moveController.setMoveForward(true);
            moveController.setMoveBackward(false);
        }
        else if (val < -0.2f) {
            moveController.setMoveForward(false);
            moveController.setMoveBackward(true);
        }
        else {
            moveController.setMoveForward(false);
            moveController.setMoveBackward(false);
        }
    }
}