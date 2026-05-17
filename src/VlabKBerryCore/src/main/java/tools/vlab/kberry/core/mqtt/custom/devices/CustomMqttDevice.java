package tools.vlab.kberry.core.mqtt.custom.devices;

import lombok.Getter;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.RGB;
import tools.vlab.kberry.core.knx.devices.Communication;
import tools.vlab.kberry.core.InvalidCommandException;
import tools.vlab.kberry.core.mqtt.MqttDevice;

import java.util.List;
import java.util.Vector;

public abstract class CustomMqttDevice extends MqttDevice<CustomCommand, CustomDataPoint> {
    
    private final List<CustomCommand> cmd;
    public Vector<StatusListener> listeners = new Vector<>();
    @Getter
    private final Integer refreshIntervalMs;
    @Getter
    private final PositionPath positionPath;

    private MqttCommandWriter writer;

    protected CustomMqttDevice(PositionPath positionPath, Integer refreshIntervalMs, CustomCommand... cmd) {
        this.positionPath = positionPath;
        this.refreshIntervalMs = refreshIntervalMs;
        this.cmd = List.of(cmd);
    }

    public <T extends tools.vlab.kberry.core.mqtt.custom.devices.StatusListener> void addListener(T listener) {
        listeners.add(listener);
    }

    public <T extends tools.vlab.kberry.core.mqtt.custom.devices.StatusListener> void removeListener(T listener) {
        listeners.remove(listener);
    }

    public abstract void load();

    protected abstract void received(CustomCommand command, CustomDataPoint datapoint);

    // GETTER

    protected void get(CustomCommand mqttCommand) {
        this.writer.write(this.deviceId, mqttCommand);
    }

    // SETTER

    protected void set(CustomCommand command, boolean value) {
        validate(command);
        if (command.getDatapoint() != CustomDataPointType.INT) {
            throw new InvalidCommandException(String.format("Invalid DataType for command %s", command));
        }
        writer.write(this.deviceId, command, CustomDataPoint.from(value));
    }

    protected void set(CustomCommand command, int value) {
        validate(command);
        if (command.getDatapoint() != CustomDataPointType.INT) {
            throw new InvalidCommandException(String.format("Invalid DataType for command %s", command));
        }
        writer.write(this.deviceId, command, CustomDataPoint.from(value));
    }

    protected void set(CustomCommand command, RGB value) {
        validate(command);
        if (command.getDatapoint() != CustomDataPointType.RGB) {
            throw new InvalidCommandException(String.format("Invalid DataType for command %s", command));
        }
        writer.write(this.deviceId, command, CustomDataPoint.from(value));
    }

    private void validate(CustomCommand command) {
        if (!cmd.contains(command)) {
            throw new InvalidCommandException(String.format("Unknown command %s for device %s", command.name(), this.getClass().getSimpleName()));
        }
        if (command.getCommunication() == Communication.READ) {
            throw new InvalidCommandException(String.format("Command %s cannot be written!", command.getDatapoint().name()));
        }
    }
}
