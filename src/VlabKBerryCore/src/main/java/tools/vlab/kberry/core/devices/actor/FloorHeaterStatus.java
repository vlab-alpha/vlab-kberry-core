package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.HeaterMode;

public interface FloorHeaterStatus {

    void actuatorPositionChanged(FloorHeater floorHeater, int position);

    void setPointTemperatureChanged(FloorHeater floorHeater, float temperature);

    void setModeChanged(FloorHeater floorHeater, HeaterMode comfort);
}
