package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;

public class Light extends OnOffDevice {

    private Light(PositionPath positionPath,Integer refreshData) {
        super(positionPath,refreshData,"Light");
    }


    public static Light at(PositionPath positionPath) {
        return new Light(positionPath, null);
    }
}
