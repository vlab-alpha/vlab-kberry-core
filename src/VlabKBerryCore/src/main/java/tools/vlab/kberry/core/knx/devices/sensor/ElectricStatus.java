package tools.vlab.kberry.core.knx.devices.sensor;

import tools.vlab.kberry.core.knx.devices.StatusListener;

public interface ElectricStatus extends StatusListener {

    void kwhChanged(ElectricitySensor sensor, float kwh);

    void electricityChanged(ElectricitySensor sensor, int electricity);

}
