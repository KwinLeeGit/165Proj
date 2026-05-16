package tage;

import org.joml.*;

public class HUDController {
    
    private Engine engine;

    private Vector3f hud1Color = new Vector3f(0f, 1f, 0f);
    private Vector3f hud2Color = new Vector3f(0f, 0f, 1f);

    public HUDController(Engine engine) {
        this.engine = engine;
    }

    public void update(int health, String gameState, int score) {
        String healthText = "Health = " + health;
        String scoreText = "Score = " + score;

        if (gameState.equals("lose")) {
            scoreText = "Game Over!";
        }

        if (gameState.equals("win")) {
            scoreText = "You Win!";
        }

        engine.getHUDmanager().setHUD1(healthText, hud1Color, 15, 15);
        engine.getHUDmanager().setHUD2(scoreText, hud2Color, 500, 15);
    }
    
}
