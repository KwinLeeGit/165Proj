package tage.ai;

import tage.*;
import tage.physics.PhysicsObject;
import tage.ai.behaviortrees.*;
import org.joml.*;
import java.util.Random;

public class NPCController {

    private GameObject npc;
    private PhysicsObject npcP;
    private GameObject target;

    private BehaviorTree bt;
    private Random rand = new Random();

    private Vector3f roamTarget = new Vector3f(20, 0, 20);
    private enum NPCState {ROAM, CHASE}
    private NPCState curState = NPCState.ROAM;

    private float chaseRange = 200f;
    private float roamRadius = 800f;

    private float chaseForce = 40f;
    private float roamForce = 30f;
    private float chaseTurn = 15f;
    private float roamTurn = 15f;

    private long lastThinkUpdateTime;
    private float thinkIntervalMs = 150.0f;

    public NPCController(GameObject npc, PhysicsObject npcP, GameObject target) {
        this.npc = npc;
        this.npcP = npcP;
        this.target = target;

        setupBehaviorTree();
        lastThinkUpdateTime = System.nanoTime();
    }

    public void update(float frameTimeMs) {
        long currentTime = System.nanoTime();
        float elapsedThinkMs = (currentTime - lastThinkUpdateTime) / 1000000f;

        if (elapsedThinkMs >= thinkIntervalMs) {
            lastThinkUpdateTime = currentTime;
            bt.update(elapsedThinkMs);
        }

        if (curState == NPCState.CHASE) {
            chaseAvatar();
        }

        else {
            roamAround();
        }
    }

    private void setupBehaviorTree() {
        bt = new BehaviorTree(BTCompositeType.SELECTOR);

        bt.insertAtRoot(new BTSequence(10));
        bt.insertAtRoot(new BTSequence(20));

        // Sequence 10: if player is close, chase
        bt.insert(10, new AvatarNear(this));
        bt.insert(10, new SetChaseState(this));

        // Sequence 20: otherwise roam
        bt.insert(20, new AlwaysTrue(this));
        bt.insert(20, new SetRoamState(this));
    }
    
    public boolean isAvatarNear() {
        float distance = npc.getWorldLocation().distance(target.getWorldLocation());
        return distance < chaseRange;
    }

    public void setChasing(){curState = NPCState.CHASE;}

    public void setRoaming(){curState = NPCState.ROAM;}

    public void chaseAvatar() {
        steerToward(target.getWorldLocation(), chaseForce, chaseTurn);
    }

    public void roamAround() {
        float distance = npc.getWorldLocation().distance(roamTarget);

        if (distance < 10f) {
            float x = rand.nextFloat() * roamRadius - roamRadius / 2f;
            float z = rand.nextFloat() * roamRadius - roamRadius / 2f;
            roamTarget.set(x, 0, z);
        }

        steerToward(roamTarget, roamForce, roamTurn);
    }

    private void steerToward(Vector3f destination, float force, float turnStrength) {
        Vector3f toTarget = new Vector3f(destination).sub(npc.getWorldLocation());
        toTarget.y = 0;

        if (toTarget.length() < 0.01f) return;

        toTarget.normalize();

        Vector3f npcForward = npc.getWorldForwardVector();
        npcForward.y = 0;

        if (npcForward.length() < 0.01f) return;

        npcForward.normalize();

        float turnAmount = new Vector3f(npcForward).cross(toTarget).y;

        npcP.applyTorque(0, turnAmount * turnStrength, 0);

        npcP.applyForce(
            npcForward.x() * force,
            0,
            npcForward.z() * force,
            0, 0, 0
        );
    }
    
}
