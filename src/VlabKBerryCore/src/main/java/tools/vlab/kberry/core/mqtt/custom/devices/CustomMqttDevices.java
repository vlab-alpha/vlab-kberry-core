package tools.vlab.kberry.core.mqtt.custom.devices;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.mqtt.MqttDevices;
import tools.vlab.kberry.core.mqtt.custom.CustomDeviceType;

import java.util.*;

public class CustomMqttDevices extends MqttDevices<CustomMqttDevice, CustomCommand, CustomDataPoint> {

    private static final Logger Log = LoggerFactory.getLogger(CustomMqttDevices.class);

    public CustomMqttDevices(String serverUrl) {
        super(serverUrl);
    }

    public CustomMqttDevices(String serverUrl, boolean autoId) {
        super(serverUrl, autoId);
    }

    @Override
    protected String clientId() {
        return "vlab-kberry-core-custom-" + UUID.randomUUID();
    }

    @Override
    protected Class<? extends CustomMqttDevice> resolveDeviceClass(String type) {
        return CustomDeviceType.of(type).getClazz();
    }

    @Override
    protected void subscribeEvents(CustomMqttDevice device, String deviceId) throws MqttException {
        client.subscribe("custom/" + deviceId + "/events", (topic, msg) -> {
            device.received(CustomCommand.NOTIFY, new CustomDataPoint(msg.getPayload()));
        });
    }

    @Override
    protected void subscribeResponse(CustomMqttDevice device, String deviceId) throws MqttException {
        client.subscribe("custom/response/" + deviceId + "/+", (topicName, msg) -> {
            var topic = topicName.split("/");
            if (topic.length != 4) {
                Log.error("Invalid topic {}", topicName);
                return;
            }
            var cmd = Integer.parseInt(topic[2]);
            var command = CustomCommand.from(cmd);
            var dataPoint = new CustomDataPoint(msg.getPayload());
            device.received(command,dataPoint);
        });
    }

    @Override
    public void write(String deviceId, CustomCommand command, CustomDataPoint dataPoint) {
        try {
            client.publish(
                    String.format("custom/request/%s/%d", deviceId, command.getId()),
                    dataPoint.payload(), 0, false
            );
        } catch (MqttException e) {
            Log.error("Failed to write!", e);
        }
    }

    @Override
    public void write(String deviceId, CustomCommand command) {
        try {
            client.publish(
                    String.format("custom/request/%s/%d", deviceId, command.getId()),
                    new byte[0], 0, false
            );
        } catch (MqttException e) {
            Log.error("Failed to write!", e);
        }
    }

}
