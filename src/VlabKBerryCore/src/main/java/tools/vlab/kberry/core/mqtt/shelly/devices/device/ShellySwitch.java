package tools.vlab.kberry.core.mqtt.shelly.devices.device;

import tools.vlab.kberry.core.PersistentValue;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDevice;

public abstract class ShellySwitch extends ShellyDevice {

    protected final PersistentValue<Boolean> status;

    public ShellySwitch(PositionPath positionPath, Integer refreshIntervalMs, String eventKey) {
        super(positionPath, refreshIntervalMs, eventKey);
        this.status = new PersistentValue<>(positionPath, eventKey, false, Boolean.class);
    }

    public boolean isOn() {
        return status.get();
    }

    public abstract void on();

    public abstract void off();





}
