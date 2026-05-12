package tools.vlab.kberry.core.mqtt.custom;

import lombok.Getter;
import tools.vlab.kberry.core.mqtt.custom.devices.CustomMqttDevice;
import tools.vlab.kberry.core.mqtt.custom.devices.actor.Fan;

@Getter
public enum CustomDeviceType {
    FAN(Fan.class);

    private final Class<? extends CustomMqttDevice> clazz;

    CustomDeviceType(Class<? extends CustomMqttDevice> clazz) {
        this.clazz = clazz;
    }

    public static CustomDeviceType of(String name) {
        return valueOf(name.toUpperCase());
    }
}
