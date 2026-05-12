package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface PresenceStatus extends StatusListener {

    void presenceChanged(PresenceSensor sensor, boolean available);
}
