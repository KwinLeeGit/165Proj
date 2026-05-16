package tage.input;

import tage.GameObject;
import tage.physics.PhysicsObject;
import tage.shapes.AnimatedShape;
import org.joml.*;

public class MovementController {

    private GameObject bike;
    private AnimatedShape bikeShape;
    private PhysicsObject bikePhysics;

    private boolean moveForward;
    private boolean moveBackward;
    private boolean turnLeft;
    private boolean turnRight;
    private boolean forwardAnimPlaying = false;
    private boolean backwardAnimPlaying = false;    

    private float moveForce = 2.5f;
    private float turnSpeed = 1.5f;

    public MovementController(GameObject bike, AnimatedShape bikeShape, PhysicsObject bikePhysics) {
        this.bike = bike;
        this.bikeShape = bikeShape;
        this.bikePhysics = bikePhysics;
    }

    public void update(double frameTimeMillis) {
        float moveAmount = (float)(frameTimeMillis * moveForce);
        float yawSpeed = turnSpeed;

        Vector3f fwd = bike.getWorldForwardVector();

        if (moveForward) {
            bikePhysics.applyForce(
                fwd.x() * moveAmount,
                0,
                fwd.z() * moveAmount,
                0, 0, 0
            );

            playForwardAnimation();
        }

        else if (moveBackward) {
            bikePhysics.applyForce(
                -fwd.x() * moveAmount,
                0,
                -fwd.z() * moveAmount,
                0, 0, 0
            );

            playBackwardAnimation();
        }

        else {
            stopMovementAnimation();
        }

        if (turnLeft) {
            bikePhysics.setAngularVelocity(new float[] {0f, yawSpeed, 0f});
        }
        else if (turnRight) {
            bikePhysics.setAngularVelocity(new float[] {0f, -yawSpeed, 0f});
        }
        else {
            bikePhysics.setAngularVelocity(new float[] {0f, 0f, 0f});

        }
            
        bikeShape.updateAnimation();
    }

    public void setMoveForward(boolean value) {
        moveForward = value;
    }

    public void setMoveBackward(boolean value) {
        moveBackward = value;
    }

    public void setTurnLeft(boolean value) {
        turnLeft = value;
    }

    public void setTurnRight(boolean value) {
        turnRight = value;
    }

    public boolean isMovingOrTurning() {
        return moveForward || moveBackward || turnLeft || turnRight;
    }

    public GameObject getBike() {
        return bike;
    }

    public PhysicsObject getBikePhysics() {
        return bikePhysics;
    }

    private void playForwardAnimation() {
    if (!forwardAnimPlaying) {
        bikeShape.stopAnimation();
        bikeShape.playAnimation(
            "driveForward",
            .25f,
            AnimatedShape.EndType.LOOP,
            0
        );

        forwardAnimPlaying = true;
        backwardAnimPlaying = false;
    }
}

private void playBackwardAnimation() {
    if (!backwardAnimPlaying) {
        bikeShape.stopAnimation();
        bikeShape.playAnimation(
            "driveBackward",
            .25f,
            AnimatedShape.EndType.LOOP,
            0
        );

        backwardAnimPlaying = true;
        forwardAnimPlaying = false;
    }
}

private void stopMovementAnimation() {
    if (forwardAnimPlaying || backwardAnimPlaying) {
        bikeShape.stopAnimation();

        forwardAnimPlaying = false;
        backwardAnimPlaying = false;
    }
}
}
