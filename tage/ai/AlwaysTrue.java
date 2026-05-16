package tage.ai;

import tage.ai.behaviortrees.*;

public class AlwaysTrue extends BTCondition {
    public AlwaysTrue(NPCController controller) {
        super(false);
    }

    @Override
    protected boolean check() {
        return true;
    }
}