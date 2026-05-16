package tage.input.action;

import net.java.games.input.Event;
import tage.physics.BulletController;
import tage.GameObject;

public class ShootAction extends AbstractInputAction {
    private BulletController bulletController;
    private GameObject avatar;

    public ShootAction(BulletController bulletController, GameObject avatar) {
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