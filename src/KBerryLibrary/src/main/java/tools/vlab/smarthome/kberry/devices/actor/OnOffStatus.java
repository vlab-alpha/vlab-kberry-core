package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;

public interface OnOffStatus {

    void onOffStatusChanged(OnOffDevice onOffDevice, boolean isOn);
}
