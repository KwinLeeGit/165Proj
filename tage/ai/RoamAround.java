package tage.ai;

import tage.ai.behaviortrees.*;

public class RoamAround extends BTAction {
    private NPCController controller;

    public RoamAround(NPCController controller) {
        this.controller = controller;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        controller.roamAround();
        return BTStatus.BH_SUCCESS;
    }
}