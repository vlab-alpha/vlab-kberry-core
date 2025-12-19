package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.PositionPath;

public interface PresenceStatus {

    void presenceChanged(PresenceSensor sensor, boolean available);
}
