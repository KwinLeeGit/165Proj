package tage;

import org.joml.*;

public class HUDController {
    
    private Engine engine;

    private Vector3f hud1Color = new Vector3f(1f, 0f, 0f);
    private Vector3f hud2Color = new Vector3f(0f, 0f, 1f);

    public HUDController(Engine engine) {
        this.engine = engine;
    }

    public void update(double elapsedTime, String gameState, int score) {
        String timeText = "Time = " + Integer.toString((int) elapsedTime);
        String scoreText = "Score = " + Integer.toString(score);

        if (gameState.equals("lose")) {
            timeText = "Game Over!";
        }

        if (gameState.equals("win")) {
            scoreText = "You Win!";
        }

        engine.getHUDmanager().setHUD1(timeText, hud1Color, 15, 15);
        engine.getHUDmanager().setHUD2(scoreText, hud2Color, 500, 15);
    }
    
}
