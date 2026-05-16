package tage.ai;

import tage.ai.behaviortrees.*;

public class SetChaseState extends BTAction {
    private NPCController controller;

    public SetChaseState(NPCController controller) {
        this.controller = controller;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        controller.setChasing();
        return BTStatus.BH_SUCCESS;
    }
}