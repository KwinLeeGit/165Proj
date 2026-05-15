package tage.ai;

import tage.*;
import tage.physics.PhysicsObject;
import org.joml.*;

public class NPCController {

    private GameObject npc;
    private PhysicsObject npcP;
    private GameObject target;

    public NPCController(GameObject npc, PhysicsObject npcP, GameObject target) {
        this.npc = npc;
        this.npcP = npcP;
        this.target = target;
    }

    public void update() {
        Vector3f toTarget = new Vector3f(target.getWorldLocation())
            .sub(npc.getWorldLocation());

        float distance = toTarget.length();

        if (distance > 5f) {
            toTarget.normalize();

            Vector3f npcForward = npc.getWorldForwardVector();
            float turn = npcForward.cross(toTarget).y;

            npcP.applyTorque(0, turn * 5f, 0);
            npcP.applyForce(
                npcForward.x() * 10f,
                0,
                npcForward.z() * 10f,
                0, 0, 0
            );
        }
    }
    
}
