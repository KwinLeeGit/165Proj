package tage.ai;

import tage.ai.behaviortrees.*;

public class AvatarNear extends BTCondition {
    private NPCController controller;


    public AvatarNear(NPCController controller) {
        super(false);
        this.controller = controller;
    }

    @Override
    protected boolean check() {
        return controller.isAvatarNear();
    }
}
