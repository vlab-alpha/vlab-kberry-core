package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface TemperatureStatus extends StatusListener {

    void temperatureChanged(TemperatureSensor sensor, float celsius);
}
