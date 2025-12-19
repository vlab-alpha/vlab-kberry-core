package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;

public class Plug extends OnOffDevice {

    private Plug(PositionPath positionPath) {
        super(positionPath);
    }

    public static Plug at(PositionPath positionPath) {
        return new Plug(positionPath);
    }

}
