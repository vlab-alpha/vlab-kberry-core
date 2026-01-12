package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;
import tools.vlab.kberry.core.devices.Command;
import tools.vlab.kberry.core.devices.HeaterMode;
import tools.vlab.kberry.core.devices.KNXDevice;
import tools.vlab.kberry.core.devices.PersistentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.devices.Command.*;

public class FloorHeater extends KNXDevice {

    private final PersistentValue<Integer> currentPosition;
    private final PersistentValue<Integer> currentMode;
    private final PersistentValue<Float> currentSetpoint;

    private FloorHeater(PositionPath positionPath,Integer refreshData) {
        super(positionPath,
                refreshData,
                HVAC_SETPOINT_TEMPERATURE_SET,
                HVAC_SETPOINT_TEMPERATURE_ACTUAL,
                HVAC_OPERATING_MODE_SET,
                HVAC_OPERATING_MODE_ACTUAL,
                HVAC_ACTUATOR_POSITION_ACTUAL
        );
        this.currentPosition = new PersistentValue<>(positionPath, "floorHeaterPosition", 0, Integer.class);
        this.currentMode = new PersistentValue<>(positionPath, "currentMode", 0, Integer.class);
        this.currentSetpoint = new PersistentValue<>(positionPath, "currentSetpoint", 0.0f, Float.class);
    }

    public static FloorHeater at(PositionPath positionPath) {
        return new FloorHeater(positionPath, null);
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

    public int getActuatorPosition() {
        return this.currentPosition.get();
    }

    public int getActuatorPositionPercent() {
        return Math.round(this.currentPosition.get() * 100f / 255f);
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case HVAC_SETPOINT_TEMPERATURE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentSetpoint.set(value);
                this.getListener().forEach(status -> status.setPointTemperatureChanged(this, value));
            });
            case HVAC_OPERATING_MODE_ACTUAL -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentMode.set(value);
                this.getListener().forEach(status -> status.setModeChanged(this, HeaterMode.valueOf(value)));
            });
            case HVAC_ACTUATOR_POSITION_ACTUAL -> dataPoint.getUInt8().ifPresent(value -> {
                this.currentPosition.set(value);
                this.getListener().forEach(status -> status.actuatorPositionChanged(this, value));
            });
        }
    }

    private List<FloorHeaterStatus> getListener() {
        return this.listeners.stream().filter(l -> l instanceof FloorHeaterStatus).map(l -> (FloorHeaterStatus) l).collect(Collectors.toList());
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(HVAC_SETPOINT_TEMPERATURE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentSetpoint::set);
        this.get(HVAC_OPERATING_MODE_ACTUAL).flatMap(DataPoint::getUInt8).ifPresent(currentMode::set);
        this.get(HVAC_ACTUATOR_POSITION_ACTUAL).flatMap(DataPoint::getUInt8).ifPresent(currentPosition::set);
    }
}
