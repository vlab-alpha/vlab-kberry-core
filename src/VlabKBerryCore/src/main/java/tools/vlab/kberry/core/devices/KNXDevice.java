package tools.vlab.kberry.core.devices;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.*;
import tools.vlab.kberry.core.baos.messages.os.DataPoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class KNXDevice {

    public Vector<StatusListener> listeners = new Vector<>();

    private static final Logger Log = LoggerFactory.getLogger(KNXDevice.class);

    private Thread updateThread;
    private volatile boolean running = false;

    @Getter
    private final PositionPath positionPath;
    private SerialBAOSConnection connection;
    private final Command[] cmd;
    private final ConcurrentHashMap<Command, BAOSObject> BAOMap = new ConcurrentHashMap<>();
    private final Integer refreshIntervallMs;
    @Getter
    private final String id = UUID.randomUUID().toString();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected KNXDevice(PositionPath positionPath, Integer refreshIntervallMs, Command... cmd) {
        this.positionPath = positionPath;
        this.cmd = cmd;
        this.refreshIntervallMs = refreshIntervallMs;
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
        set(command, value, false);
    }

    protected void set(Command command, boolean value, boolean priority) {
        this.validate(command);
        var bao = BAOMap.get(command);
        if (Objects.requireNonNull(bao.datapointType()) == KnxDatapointType.BOOLEAN) {
            this.write(DataPoint.bool(bao.dataPointId(), value), priority);
        } else {
            throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, HeaterMode mode) {
        this.validate(command);
        var bao = BAOMap.get(command);
        if (Objects.requireNonNull(bao.datapointType()) == KnxDatapointType.HVAC_MODE) {
            this.write(DataPoint.hvac(bao.dataPointId(), mode), false);
        } else {
            throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, int value) {
        set(command, value, false);
    }

    protected void set(Command command, int value, boolean priority) {
        this.validate(command);
        var bao = BAOMap.get(command);
        switch (bao.datapointType()) {
            case INT8 -> this.write(DataPoint.int8(bao.dataPointId(), value), priority);
            case SINT8 -> this.write(DataPoint.sInt8(bao.dataPointId(), value), priority);
            case SINT16 -> this.write(DataPoint.sint16(bao.dataPointId(), value), priority);
            case SINT32 -> this.write(DataPoint.sint32(bao.dataPointId(), value), priority);
            case UINT8, SCENE_NUMBER -> this.write(DataPoint.uInt8(bao.dataPointId(), value), priority);
            case UINT16 -> this.write(DataPoint.uInt16(bao.dataPointId(), value), priority);
            case UINT32 -> this.write(DataPoint.uint32(bao.dataPointId(), value), priority);
            default -> throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, float value) {
        set(command, value, false);
    }

    protected void set(Command command, float value, boolean priority) {
        this.validate(command);
        var bao = BAOMap.get(command);
        switch (bao.datapointType()) {
            case FLOAT9 -> this.write(DataPoint.float9(bao.dataPointId(), value), priority);
            case FLOAT32 -> this.write(DataPoint.float32(bao.dataPointId(), value), priority);
            default -> throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    protected void set(Command command, RGB value) {
        set(command, value, false);
    }

    protected void set(Command command, RGB value, boolean priority) {
        this.validate(command);
        var bao = BAOMap.get(command);
        if (Objects.requireNonNull(bao.datapointType()) == KnxDatapointType.RGB) {
            this.write(DataPoint.rgb(bao.dataPointId(), value), priority);
        } else {
            throw new InvalidCommandException(String.format("Unknown command %s", command));
        }
    }

    public Optional<DataPoint> get(Command command) throws BAOSReadException {
        var bao = BAOMap.get(command);
        Log.debug("Get command {} with id {}", command.name(), bao.dataPointId().id());
        try {
            return Optional.of(this.connection.read(bao.dataPointId()));
        } catch (Exception e) {
            Log.debug("Failed to write data point!", e);
            return Optional.empty();
        }
    }

    public void start() throws BAOSReadException {
        if (refreshIntervallMs != null) {
            running = true;
            refreshDataProcess();
        } else {
            load();
        }
    }

    public void stop() {
        executor.close();
        running = false;
        try {
            if (updateThread != null) {
                updateThread.interrupt();
            }
        } catch (Exception e) {
            /* ignore */
        }
    }

    public abstract void load() throws BAOSReadException;

    private void refreshDataProcess() {
        updateThread = new Thread(() -> {
            while (running) {
                try {
                    load();
                    Thread.sleep(refreshIntervallMs);
                } catch (InterruptedException | BAOSReadException e) {
                    Log.error("Failed to refresh data point!", e);
                }
            }
        }, "Update DataPoint of " + positionPath.getPath());
        updateThread.setDaemon(true);
        updateThread.start();
    }


    public void write(DataPoint dataPoint, boolean priority) {
        try {
            this.connection.write(dataPoint, priority);
        } catch (Exception e) {
            Log.error("Failed to write data point!", e);
        }
    }

    public <T extends StatusListener> void addListener(T listener) {
        listeners.add(listener);
    }

    public <T extends StatusListener> void removeListener(T listener) {
        listeners.remove(listener);
    }

}
