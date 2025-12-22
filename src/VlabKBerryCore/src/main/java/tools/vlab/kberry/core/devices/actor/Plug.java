package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;

public class Plug extends OnOffDevice {

    private Plug(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData,"Plug");
    }

    public static Plug at(PositionPath positionPath) {
        return new Plug(positionPath, null);
    }

}
