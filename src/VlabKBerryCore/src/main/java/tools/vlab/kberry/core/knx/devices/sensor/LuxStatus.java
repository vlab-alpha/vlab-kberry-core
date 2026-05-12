package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface LuxStatus extends StatusListener {

    void luxChanged(LuxSensor sensor, float lux);
}
