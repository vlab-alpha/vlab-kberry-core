package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.devices.HeaterMode;

public interface FloorHeaterStatus {

    void actuatorPositionChanged(FloorHeater floorHeater, int position);

    void setPointTemperatureChanged(FloorHeater floorHeater, float temperature);

    void setModeChanged(FloorHeater floorHeater, HeaterMode comfort);
}
