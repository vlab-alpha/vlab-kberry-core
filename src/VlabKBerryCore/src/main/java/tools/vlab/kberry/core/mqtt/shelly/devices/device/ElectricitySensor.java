package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.PersistentValue;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyCommand;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDataPoint;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDevice;

import java.util.List;
import java.util.stream.Collectors;

public class ElectricitySensor extends ShellyDevice {

    private final PersistentValue<Double> status;

    public ElectricitySensor(PositionPath positionPath, Integer refreshIntervalMs) {
        super(positionPath, refreshIntervalMs, "switch:0");
        this.status = new PersistentValue<>(positionPath, "ElectricityStatus", 0.0, Double.class);
    }

    @Override
    public void load() {
    }

    private List<ElectricStatus> getListener() {
        return this.listeners.stream()
                .filter(l -> l instanceof ElectricStatus)
                .map(l -> (ElectricStatus) l)
                .collect(Collectors.toList());
    }

    @Override
    protected void received(ShellyCommand command, ShellyDataPoint datapoint) {
        switch (command) {
            case NOTIFY_STATUS -> {
                var wh = datapoint.getDouble("aenergy.total").orElse(0.0);
                var current = datapoint.getDouble("apower").orElse(0.0);
                double kwh = wh / 1000.0;
                this.status.set(kwh);
                getListener().forEach(status -> {
                    status.totalKwh(this, kwh);
                    status.currentWh(this, current);
                });
            }
        }
    }
}
