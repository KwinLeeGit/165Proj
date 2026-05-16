package tage.ai;

import tage.ai.behaviortrees.*;

public class SetRoamState extends BTAction {
    private NPCController controller;

    public SetRoamState(NPCController controller) {
        this.controller = controller;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        controller.setRoaming();
        return BTStatus.BH_SUCCESS;
    }
}