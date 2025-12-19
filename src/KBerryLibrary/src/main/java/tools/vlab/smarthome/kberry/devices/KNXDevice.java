package tools.vlab.smarthome.kberry.devices;

import lombok.Getter;
import tools.vlab.smarthome.kberry.Log;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.*;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPointId;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXME: Es gibt Sensoren, die nicht von sich aus senden sondern es wird ein pull erwartet
 */
public abstract class KNXDevice {

    @Getter
    private final PositionPath positionPath;
    private SerialBAOSConnection connection;
    private final Command[] cmd;
    private final ConcurrentHashMap<Command, BAOSObject> BAOMap = new ConcurrentHashMap<>();

    protected KNXDevice(PositionPath positionPath, Command... cmd) {
        this.positionPath = positionPath;
        this.cmd = cmd;
    }

    protected List<Command> getCommands() {
        return Arrays.asList(cmd);
    }

    protected void register(SerialBAOSConnection connection, List<BAOSObject> baoObjects) {
        this.connection = connection;
        this.BAOMap.clear();
        baoObjects.forEach(bao -> {
            BAOMap.put(bao.command(), bao);
            if (bao.command().communication == Communication.READ || bao.command().communication == Communication.READWRITE) {
                connection.onValueChanged(bao.dataPointId(), (dataPoint) -> this.received(bao.command(), dataPoint));
            }
        });
    }

    protected abstract void received(Command command, DataPoint dataPoint);

    private void validate(Command command) {
        if (!BAOMap.containsKey(command)) {
            throw new UnknownBAOCommandException(String.format("Unknown command %s for device %s", command.name(), this.getClass().getSimpleName()));
        }
        var bao = BAOMap.get(command);
        if (bao.command().communication != Communication.WRITE && bao.command().communication != Communication.READWRITE) {
            throw new InvalidCommandException(String.format("Command %s cannot be written!", bao.command().dataType));
        }
    }

    protected void set(Command command, boolean value) {
        this.validate(command);
        var bao = BAOMap.get(command);
        if (Objects.requireNonNull(bao.datapointType()) == KnxDatapointType.BOOLEAN) {
            this.writeWithRetry(DataPoint.bool(bao.dataPointId(), value));
        } else {
            throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, int value) {
        this.validate(command);
        var bao = BAOMap.get(command);
        switch (bao.datapointType()) {
            case INT8 -> this.writeWithRetry(DataPoint.int8(bao.dataPointId(), value));
            case SINT8 -> this.writeWithRetry(DataPoint.sInt8(bao.dataPointId(), value));
            case SINT16 -> this.writeWithRetry(DataPoint.sint16(bao.dataPointId(), value));
            case SINT32 -> this.writeWithRetry(DataPoint.sint32(bao.dataPointId(), value));
            case UINT8 -> this.writeWithRetry(DataPoint.uInt8(bao.dataPointId(), value));
            case UINT16 -> this.writeWithRetry(DataPoint.uInt16(bao.dataPointId(), value));
            case UINT32 -> this.writeWithRetry(DataPoint.uint32(bao.dataPointId(), value));
            default -> throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, float value) {
        this.validate(command);
        var bao = BAOMap.get(command);
        switch (bao.datapointType()) {
            case FLOAT9 -> this.writeWithRetry(DataPoint.float9(bao.dataPointId(), value));
            case FLOAT32 -> this.writeWithRetry(DataPoint.float32(bao.dataPointId(), value));
            default -> throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, RGB value) {
        this.validate(command);
        var bao = BAOMap.get(command);
        if (Objects.requireNonNull(bao.datapointType()) == KnxDatapointType.RGB) {
            this.writeWithRetry(DataPoint.rgb(bao.dataPointId(), value));
        } else {
            throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    public Optional<DataPoint> get(Command command) throws BAOSReadException {
        var bao = BAOMap.get(command);
        Log.debug("Get command %s with id %s", command.name(), bao.dataPointId().id());
        return readWithRetry(bao.dataPointId());
    }

    protected Optional<DataPoint> readWithRetry(DataPointId id) {
        for (int retry = 2; retry > 0; retry--) {
            try {
                return Optional.of(this.connection.read(id));
            } catch (BAOSReadException e) {
                Log.debug(e, "Retry %s [%s]", retry, id);
            }
        }
        Log.error("Failed to write data point!");
        return Optional.empty();
    }

    public abstract void load() throws BAOSReadException;


    public void writeWithRetry(DataPoint dataPoint) throws BAOSWriteException {
        for (int retry = 10; retry > 0; retry--) {
            try {
                this.connection.write(dataPoint);
                Thread.sleep(10);
                return;
            } catch (BAOSWriteException e) {
                Log.debug("Retry %s", retry);
            } catch (InterruptedException e) {
                return;
            }
        }
        Log.error("Failed to write data point!");
    }

}
