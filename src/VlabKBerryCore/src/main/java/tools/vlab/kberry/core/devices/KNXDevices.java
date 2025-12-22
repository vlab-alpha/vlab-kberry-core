package tools.vlab.kberry.core.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.ReloadDevice;
import tools.vlab.kberry.core.baos.BAOSObject;
import tools.vlab.kberry.core.baos.BAOSReadException;
import tools.vlab.kberry.core.baos.SerialBAOSConnection;
import tools.vlab.kberry.core.baos.messages.os.DataPointId;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Central registry and manager for all KNX devices.
 *
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Registering KNX devices</li>
 *   <li>Creating and managing BAOS datapoint objects</li>
 *   <li>Providing lookup and filtering mechanisms (by location, floor, room)</li>
 *   <li>Exporting BAOS configuration for the Weinzierl CSV importer</li>
 *   <li>Reloading device state after startup or reconnect</li>
 * </ul>
 *
 * <p>
 * Each registered {@link KNXDevice} is mapped to one or more {@link BAOSObject}s.
 * Datapoint IDs are assigned sequentially and managed internally.
 *
 * <p>
 * The class implements {@link ReloadDevice} and is automatically triggered
 * by the {@link SerialBAOSConnection} after reconnect or reset.
 */
public class KNXDevices implements ReloadDevice {

    private static final Logger Log = LoggerFactory.getLogger(KNXDevices.class);

    private final Vector<KNXDevice> devices = new Vector<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Vector<BAOSObject> allBAOList = new Vector<>();
    private final SerialBAOSConnection connection;

    /**
     * Creates a new KNXDevices registry bound to a BAOS serial connection.
     *
     * <p>
     * The registry automatically registers itself as reload handler on the
     * provided connection.
     *
     * @param connection active {@link SerialBAOSConnection}
     */
    public KNXDevices(SerialBAOSConnection connection) {
        this.connection = connection;
        this.connection.setReloadDevice(this);
    }

    /**
     * Returns a BAOS object by its datapoint ID.
     *
     * @param objectId datapoint ID
     * @return optional BAOSObject if present
     */
    public Optional<BAOSObject> getBao(int objectId) {
        return this.allBAOList.stream().filter(bao -> bao.dataPointId().isSame(objectId)).findFirst();
    }

    /**
     * Registers a KNX device and initializes all associated BAOS datapoints.
     *
     * <p>
     * For each command defined by the device, a new {@link BAOSObject} is created
     * with a unique datapoint ID. The device is then connected to the BAOS
     * connection and receives its datapoint mapping.
     *
     * <p>
     * All created BAOS objects are added to a global list for lookup,
     * CSV export and state handling.
     *
     * @param <T>    concrete KNX device type
     * @param device device instance to register
     */
    public <T extends KNXDevice> void register(T device) {
        this.devices.add(device);
        var baoList = new ArrayList<BAOSObject>();
        for (var cmd : device.getCommands()) {
            baoList.add(new BAOSObject(DataPointId.id(this.counter.getAndIncrement()), device.getClass().getSimpleName(), cmd, device.getPositionPath(), cmd.dataType));
        }
        device.register(this.connection, baoList);
        this.allBAOList.addAll(baoList);
    }

    /**
     * Exports the current BAOS datapoint configuration as CSV.
     *
     * <p>
     * The generated file is compatible with the Weinzierl BAOS ETS importer.
     *
     * @param filePath destination CSV file path
     * @throws IOException if file writing fails
     */
    public void exportCSV(Path filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.append("""
                    Csv version;1.2;;;;
                    Project name;Familie Radle;;;;
                    Device type;BAOS Module;;;;
                    Device name;KNX BAOS 830;;;;
                    ;;;;;
                    """);
            writer.append("Type;ID;DPT;Description;Addr/Val\n"); // Header ohne GA
            for (BAOSObject bao : allBAOList) {
                writer.append(String.format("%s;%d;DPT-%s;%s;\n",
                        "DP",
                        bao.dataPointId().id(),
                        bao.datapointType().getType(),
                        bao.getName()));
            }
        }
        System.out.println("Konfiguration exportiert nach: " + filePath);
    }

    /**
     * Returns all KNX devices of a given type located at a specific location.
     *
     * @param clazz    device class
     * @param location location name (top category of PositionPath)
     * @return list of matching devices
     */
    public <T extends KNXDevice> List<T> getKNXDevicesByLocation(Class<T> clazz, String location) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getLocation().equalsIgnoreCase(location))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns all KNX devices of a given type on a specific floor.
     *
     * @param clazz device class
     * @param floor floor name
     * @return list of matching devices
     */
    public <T extends KNXDevice> List<T> getKNXDevicesByFloor(Class<T> clazz, String floor) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getFloor().equalsIgnoreCase(floor))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns all KNX devices of a given type in a specific room.
     *
     * @param clazz device class
     * @param room  room name
     * @return list of matching devices
     */
    public <T extends KNXDevice> List<T> getKNXDevicesByRoom(Class<T> clazz, String room) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getRoom().equalsIgnoreCase(room))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns a specific KNX device by type and exact position path.
     *
     * @param clazz        device class
     * @param positionPath full position path
     * @return optional device
     */
    public <T extends KNXDevice> Optional<T> getKNXDevice(Class<T> clazz, PositionPath positionPath) {
        return this.devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(d -> d.getPositionPath().isSame(positionPath))
                .findFirst();
    }

    /**
     * Returns all registered KNX devices of a given type.
     *
     * @param clazz device class
     * @return list of devices
     */
    public <T extends KNXDevice> List<T> getKNXDevices(Class<T> clazz) {
        return this.devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns all registered KNX devices.
     *
     * @return list of all devices
     */
    public List<KNXDevice> getAllDevices() {
        return this.devices;
    }

    /**
     * Reloads data all registered devices.
     *
     * <p>
     * Each device is responsible for restoring its state from cache or by
     * querying the Object Server.
     */
    @Override
    public void load() {
        this.getAllDevices().forEach(device -> {
            try {
                device.load();
            } catch (BAOSReadException e) {
                Log.error("Failed to load device {}", device.getClass().getSimpleName());
                Log.error("Failed to load device", e);
            }
        });
    }
}
