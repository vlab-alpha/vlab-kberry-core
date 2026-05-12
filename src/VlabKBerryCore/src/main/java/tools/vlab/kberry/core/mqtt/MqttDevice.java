package tools.vlab.kberry.core.mqtt;

import tools.vlab.kberry.core.PositionPath;

public abstract class MqttDevice<TCommand, TDataPoint> {

    protected String deviceId;
    protected MqttWriter<TCommand, TDataPoint> writer;

    public void init(String deviceId, MqttWriter<TCommand, TDataPoint> writer) {
        this.deviceId = deviceId;
        this.writer = writer;
    }

    protected void set(TCommand command, TDataPoint dataPoint) {
        writer.write(deviceId, command, dataPoint);
    }

    protected void get(TCommand command) {
        writer.write(deviceId, command);
    }

    public abstract void load();
    public abstract PositionPath getPositionPath();
}
