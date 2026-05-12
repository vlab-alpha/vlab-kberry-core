package tools.vlab.kberry.core.mqtt.shelly.devices;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.mqtt.MqttDevices;
import tools.vlab.kberry.core.mqtt.shelly.ShellyDeviceType;

import java.util.UUID;

public class ShellyDevices extends MqttDevices<ShellyDevice, ShellyCommand, ShellyDataPoint> {

    private static final Logger Log = LoggerFactory.getLogger(ShellyDevices.class);

    private final IndexedStore<ShellyCommand> cmdMap = new IndexedStore<>();

    public ShellyDevices(String serverUrl) {
        super(serverUrl);
    }

    public ShellyDevices(String serverUrl, boolean autoId) {
        super(serverUrl, autoId);
    }

    @Override
    protected String clientId() {
        return "vlab-kberry-core-shelly-" + UUID.randomUUID();
    }

    @Override
    protected Class<? extends ShellyDevice> resolveDeviceClass(String type) {
        return ShellyDeviceType.of(type).getClazz();
    }

    @Override
    protected void subscribeEvents(ShellyDevice device, String deviceId) throws MqttException {
        client.subscribe(deviceId + "/events/rpc", (topic, msg) -> {
            var rawJson = new String(msg.getPayload());
            ShellyDataPoint params = ShellyDataPoint.from(rawJson).getDataPoint("params");
            if (params != null) {
                ShellyDataPoint dataPoint = params.getDataPoint(device.eventKey);
                if (dataPoint != null) {
                    device.received(ShellyCommand.NOTIFY_STATUS, dataPoint);
                }
            }
        });
    }

    @Override
    protected void subscribeResponse(ShellyDevice device, String deviceId) throws MqttException {
        client.subscribe("shelly-kberry-core", (topic, msg) -> {
            var rawJson = new String(msg.getPayload());
            int commandId = Integer.parseInt(rawJson.split("\"id\":")[1].split("[,}]")[0].trim());
            ShellyCommand command = cmdMap.get(commandId);
            ShellyDataPoint dataPoint = ShellyDataPoint.from(rawJson).getDataPoint("result");
            device.received(command, dataPoint);
        });
    }

    @Override
    public void write(String deviceId, ShellyCommand command, ShellyDataPoint dataPoint) {
        try {
            var commandId = cmdMap.add(command);
            var payload = String.format("""
                    {"id":%d,"src":"shelly-kberry-core","method":"%s",%s}
                    """, commandId, command, dataPoint.toJson());
            client.publish(deviceId + "/rpc", payload.getBytes(), 0, false);
        } catch (MqttException e) {
            Log.error("Failed to write!", e);
        }
    }

    @Override
    public void write(String deviceId, ShellyCommand command) {
        try {
            var commandId = cmdMap.add(command);
            var payload = String.format("""
                    {"id":%d,"src":"shelly-kberry-core","method":"%s","params":{"id":0}}
                    """, commandId, command);
            client.publish(deviceId + "/rpc", payload.getBytes(), 0, false);
        } catch (MqttException e) {
            Log.error("Failed to write!", e);
        }
    }

}
