package tools.vlab.kberry.core.mqtt.custom.devices.actor;

import tools.vlab.kberry.core.mqtt.custom.devices.StatusListener;

public interface FanStatus extends StatusListener {

    void fanStatusChanged(Fan fan, boolean on);
    void fanSpeedChanged(Fan fan, int speed);
}
