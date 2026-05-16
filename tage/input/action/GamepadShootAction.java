package tage.input.action;

import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import tage.physics.BulletController;

public class GamepadShootAction extends AbstractInputAction {
    private BulletController bulletController;
    private GameObject avatar;

    public GamepadShootAction(BulletController bulletController, GameObject avatar) {
        this.bulletController = bulletController;
        this.avatar = avatar;
    }

    @Override
    public void performAction(float time, Event e) {
        if (e.getValue() > 0.2f) {
            bulletController.shoot(avatar);
        }
    }
}