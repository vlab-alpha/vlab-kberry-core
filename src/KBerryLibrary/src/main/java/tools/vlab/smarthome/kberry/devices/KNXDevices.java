package tools.vlab.smarthome.kberry.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.ReloadDevice;
import tools.vlab.smarthome.kberry.baos.*;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPointId;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class KNXDevices implements ReloadDevice {

    private static final Logger Log = LoggerFactory.getLogger(KNXDevices.class);

    private final Vector<KNXDevice> devices = new Vector<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Vector<BAOSObject> allBAOList = new Vector<>();
    private final SerialBAOSConnection connection;

    public KNXDevices(SerialBAOSConnection connection) {
        this.connection = connection;
        this.connection.setReloadDevice(this);
    }


    public Optional<BAOSObject> getBao(int objectId) {
        return this.allBAOList.stream().filter(bao -> bao.dataPointId().isSame(objectId)).findFirst();
    }

    /**
     * Registers a KNX device and initializes its associated BAOSObjects.
     * The method creates BAOSObjects for each command of the device and links them
     * with the device using the specified writer and reader. Additionally, the created
     * BAOSObjects are added to an internal global list for further management.
     *
     * @param <T>    the type of the KNXDevice, which must extend KNXDevice
     * @param device the KNX device to be registered
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

    public <T extends KNXDevice> List<T> getKNXDevicesByLocation(Class<T> clazz, String location) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getLocation().equalsIgnoreCase(location))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public <T extends KNXDevice> List<T> getKNXDevicesByFloor(Class<T> clazz, String floor) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getFloor().equalsIgnoreCase(floor))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public <T extends KNXDevice> List<T> getKNXDevicesByRoom(Class<T> clazz, String room) {
        return this.devices.stream()
                .filter(d -> d.getPositionPath().getRoom().equalsIgnoreCase(room))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public <T extends KNXDevice> Optional<T> getKNXDevice(Class<T> clazz, PositionPath positionPath) {
        return this.devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(d -> d.getPositionPath().isSame(positionPath))
                .findFirst();
    }

    public <T extends KNXDevice> List<T> getKNXDevices(Class<T> clazz) {
        return this.devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public List<KNXDevice> getAllDevices() {
        return this.devices;
    }

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
