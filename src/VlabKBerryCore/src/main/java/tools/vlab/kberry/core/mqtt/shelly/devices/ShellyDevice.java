package tools.vlab.kberry.core.mqtt.shelly.devices;

import lombok.Getter;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.MqttDevice;

import java.util.Vector;

public abstract class ShellyDevice extends MqttDevice<ShellyCommand, ShellyDataPoint> {

    @Getter
    protected final PositionPath positionPath;
    protected final String eventKey;

    public Vector<StatusListener> listeners = new Vector<>();
    protected ShellyWriter writer;

    protected ShellyDevice(PositionPath positionPath, Integer refreshIntervalMs, String eventKey) {
        this.positionPath = positionPath;
        this.eventKey = eventKey;
    }

    public <T extends StatusListener> void addListener(T listener) {
        listeners.add(listener);
    }

    public <T extends StatusListener> void removeListener(T listener) {
        listeners.remove(listener);
    }


    public abstract void load();

    protected abstract void received(ShellyCommand command, ShellyDataPoint datapoint);

    protected void set(ShellyCommand mqttCommand, boolean b) {
        writer.write(this.deviceId, mqttCommand, ShellyDataPoint.bool(b));
    }

    protected void set(ShellyCommand mqttCommand, ShellyDataPoint dataPoint) {
        writer.write(this.deviceId, mqttCommand, dataPoint);
    }

    protected void get(ShellyCommand mqttCommand) {
        writer.write(this.deviceId, mqttCommand);
    }
}
