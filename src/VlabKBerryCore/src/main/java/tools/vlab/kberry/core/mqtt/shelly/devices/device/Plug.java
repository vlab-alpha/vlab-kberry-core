package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.PersistentValue;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDataPoint;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDevice;

import java.util.List;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand.GET_SWITCH_STATUS;
import static tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand.SET_SWITCH_STATUS;

public class Plug extends ShellyDevice {

    private final PersistentValue<Boolean> status;

    public Plug(PositionPath positionPath, Integer refreshIntervalMs) {
        super(positionPath, refreshIntervalMs, "switch:0");
        this.status = new PersistentValue<>(positionPath, "plugStatus", false, Boolean.class);
    }

    public boolean isOn() {
        return status.get();
    }

    public void on() {
        set(SET_SWITCH_STATUS, true);
    }

    public void off() {
        set(SET_SWITCH_STATUS, false);
    }

    @Override
    public void load() {
        get(GET_SWITCH_STATUS);
    }

    private List<PlugStatus> getListener() {
        return this.listeners.stream()
                .filter(l -> l instanceof PlugStatus)
                .map(l -> (PlugStatus) l)
                .collect(Collectors.toList());
    }

    @Override
    protected void received(ShellyCommand command, ShellyDataPoint dataPoint) {
        switch (command) {
            case SET_SWITCH_STATUS, NOTIFY_STATUS -> dataPoint.getBoolean("output").ifPresent(value -> {
                this.status.set(value);
                getListener().forEach(status -> status.isOnChanged(this, value));
            });
        }
    }
}
