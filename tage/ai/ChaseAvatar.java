package tage.ai;

import tage.ai.behaviortrees.*;

public class ChaseAvatar extends BTAction {
    private NPCController controller;

    public ChaseAvatar(NPCController controller) {
        this.controller = controller;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        controller.chaseAvatar();
        return BTStatus.BH_SUCCESS;
    }
}