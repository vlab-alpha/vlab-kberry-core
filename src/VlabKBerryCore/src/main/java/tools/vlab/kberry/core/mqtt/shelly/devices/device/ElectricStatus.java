package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.mqtt.shelly.devices.StatusListener;

public interface ElectricStatus extends StatusListener {
    void totalKwh(ElectricitySensor sensor, double kwh);
    void currentWh(ElectricitySensor sensor, double wh);
}
