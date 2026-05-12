package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.mqtt.shelly.devices.StatusListener;

public interface PlugStatus extends StatusListener {
    void isOnChanged(Plug plug, Boolean value);
}
