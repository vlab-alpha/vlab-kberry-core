package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;

public class Light extends OnOffDevice {

    private Light(PositionPath positionPath) {
        super(positionPath);
    }


    public static Light at(PositionPath positionPath) {
        return new Light(positionPath);
    }
}
