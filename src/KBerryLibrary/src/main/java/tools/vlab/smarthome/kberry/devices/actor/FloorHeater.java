package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.AtomicFloat;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.HeaterMode;
import tools.vlab.smarthome.kberry.devices.KNXDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tools.vlab.smarthome.kberry.devices.Command.*;

public class FloorHeater extends KNXDevice {

    private final AtomicInteger currentPosition = new AtomicInteger(0);
    private final AtomicInteger currentMode = new AtomicInteger(0);
    private final AtomicFloat currentSetpoint = new AtomicFloat(0.0f);
    private final List<FloorHeaterStatus> listener = new ArrayList<>();

    private FloorHeater(PositionPath positionPath) {
        super(positionPath,
                HVAC_SETPOINT_TEMPERATURE_SET,
                HVAC_SETPOINT_TEMPERATURE_ACTUAL,
                HVAC_OPERATING_MODE_SET,
                HVAC_OPERATING_MODE_ACTUAL,
                HVAC_ACTUATOR_POSITION_ACTUAL
        );
    }

    public FloorHeater at(PositionPath positionPath) {
        return new FloorHeater(positionPath);
    }

    public void addListener(FloorHeaterStatus status) {
        this.listener.add(status);
    }

    public void setSetpoint(float temperature) {
        this.set(HVAC_SETPOINT_TEMPERATURE_SET, temperature);
    }

    public float getCurrentSetpoint() {
        return this.currentSetpoint.get();
    }

    public void setMode(HeaterMode mode) {
        var value = mode.getMode();
        this.set(HVAC_OPERATING_MODE_SET, value);
    }

    public HeaterMode getCurrentMode() {
        return HeaterMode.valueOf(currentMode.get());
    }

    public int getCurrentActuatorPosition() {
        return this.currentPosition.get();
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case HVAC_SETPOINT_TEMPERATURE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentSetpoint.set(value);
                this.listener.forEach(status -> status.setPointTemperatureChanged(this, value));
            });
            case HVAC_OPERATING_MODE_ACTUAL -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentMode.set(value);
                this.listener.forEach(status -> status.setModeChanged(this, HeaterMode.valueOf(value)));
            });
            case HVAC_ACTUATOR_POSITION_ACTUAL -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentPosition.set(value);
                this.listener.forEach(status -> status.actuatorPositionChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(HVAC_SETPOINT_TEMPERATURE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentSetpoint::set);
        this.get(HVAC_OPERATING_MODE_ACTUAL).flatMap(DataPoint::getUInt8).ifPresent(currentMode::set);
        this.get(HVAC_ACTUATOR_POSITION_ACTUAL).flatMap(DataPoint::getUInt8).ifPresent(currentPosition::set);
    }
}
