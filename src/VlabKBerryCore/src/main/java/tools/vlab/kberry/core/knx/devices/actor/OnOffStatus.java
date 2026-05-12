package tools.vlab.kberry.core.knx.devices.actor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface OnOffStatus extends StatusListener {

    void onOffStatusChanged(OnOffDevice onOffDevice, boolean isOn);
}
