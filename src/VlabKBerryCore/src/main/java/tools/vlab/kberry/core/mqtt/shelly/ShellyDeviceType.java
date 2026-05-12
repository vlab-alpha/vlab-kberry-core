package tools.vlab.kberry.core.mqtt.shelly;

import lombok.Getter;
import tools.vlab.kberry.core.mqtt.shelly.devices.ShellyDevice;
import tools.vlab.kberry.core.mqtt.shelly.devices.device.ElectricitySensor;
import tools.vlab.kberry.core.mqtt.shelly.devices.device.Led;
import tools.vlab.kberry.core.mqtt.shelly.devices.device.Plug;

@Getter
public enum ShellyDeviceType {
    PLUG(Plug.class), LED(Led.class), ELECTRICITY(ElectricitySensor.class);

    private final Class<? extends ShellyDevice> clazz;

    ShellyDeviceType(Class<? extends ShellyDevice> clazz) {
        this.clazz = clazz;
    }

    public static ShellyDeviceType of(String name) {
        return valueOf(name.toUpperCase());
    }
}
