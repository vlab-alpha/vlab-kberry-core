package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDataPoint;

import java.util.List;
import java.util.stream.Collectors;

import static tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand.GET_SWITCH_STATUS;
import static tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand.SET_SWITCH_STATUS;

public class Plug extends ShellySwitch {

    private Plug(PositionPath positionPath, Integer refreshIntervalMs) {
        super(positionPath, refreshIntervalMs, "switch:0");
    }

    public static Plug at(PositionPath positionPath) {
        return new Plug(positionPath, null);
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
            case NOTIFY_STATUS -> dataPoint.getBoolean("output").ifPresent(value -> {
                if (this.status.get() != value) {
                    if(this.status.get()) on(); else off();
                }
            });
            case SET_SWITCH_STATUS, GET_SWITCH_STATUS -> dataPoint.getBoolean("output").ifPresent(value -> {
                this.status.set(value);
                getListener().forEach(status -> status.isOnChanged(this, value));
            });
        }
    }
}
