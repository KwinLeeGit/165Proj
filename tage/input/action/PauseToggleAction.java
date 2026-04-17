package tage.input.action;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

import net.java.games.input.Event;
import org.joml.*;

import a2.MyGame;
public class PauseToggleAction extends AbstractInputAction {
    private MyGame game;

    public PauseToggleAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        game.pauseGame();
    }
}
