package tools.vlab.kberry.core.mqtt.custom.devices.actor;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.PersistentValue;
import tools.vlab.kberry.core.mqtt.custom.devices.CustomCommand;
import tools.vlab.kberry.core.mqtt.custom.devices.CustomDataPoint;
import tools.vlab.kberry.core.mqtt.custom.devices.CustomMqttDevice;

import java.util.List;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.mqtt.custom.devices.CustomCommand.*;

public class Fan extends CustomMqttDevice {

    private final PersistentValue<Boolean> currentStatus;
    private final PersistentValue<Integer> currentSpeed;

    protected Fan(PositionPath positionPath, Integer refreshIntervalMs) {
        super(positionPath, refreshIntervalMs, STATUS, GET_STATUS, GET_SPEED, SPEED);
        this.currentStatus = new PersistentValue<>(positionPath, "fanStatus", false, Boolean.class);
        this.currentSpeed = new PersistentValue<>(positionPath, "speed", 0, Integer.class);
    }

    public static Fan at(PositionPath positionPath) {
        return new Fan(positionPath, null);
    }

    public static Fan at(PositionPath positionPath, int intervalMS) {
        return new Fan(positionPath, intervalMS);
    }


    public boolean isOn() {
        return this.currentStatus.get();
    }

    public void setOn(boolean on) {
        this.set(STATUS, true);
    }

    public int getSpeed() {
        return this.currentSpeed.get();
    }

    public void setSpeed(int speed) {
        this.set(SPEED, speed);
    }

    @Override
    public void load() {
        this.get(GET_STATUS);
        this.get(GET_SPEED);
    }

    private List<FanStatus> getListener() {
        return this.listeners.stream()
                .filter(l -> l instanceof FanStatus).map(l -> (FanStatus) l)
                .collect(Collectors.toList());
    }

    @Override
    protected void received(CustomCommand command, CustomDataPoint dataPoint) {
        switch (command) {
            case STATUS -> dataPoint.getBoolean().ifPresent(onOff -> {
                this.currentStatus.set(onOff);
                getListener().forEach(listener -> listener.fanStatusChanged(this, onOff));
            });
            case SPEED -> dataPoint.getInt().ifPresent(value -> {
                this.currentSpeed.set(value);
                getListener().forEach(listener -> listener.fanSpeedChanged(this, value));
            });
        }
    }
}
