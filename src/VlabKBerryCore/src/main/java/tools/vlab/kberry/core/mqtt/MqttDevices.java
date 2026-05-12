package tools.vlab.kberry.core.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.CsvReader;
import tools.vlab.kberry.core.CsvWriter;
import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.mqtt.shelly.devices.IndexedStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MqttDevices<TDevice extends MqttDevice<TCommand, TDataPoint>, TCommand, TDataPoint>
        implements MqttWriter<TCommand, TDataPoint> {

    private static final Logger Log = LoggerFactory.getLogger(MqttDevices.class);

    protected MqttClient client;
    protected final List<TDevice> registerDevices = new ArrayList<>();
    protected final List<TDevice> devices = new ArrayList<>();
    protected final IndexedStore<TCommand> cmdMap = new IndexedStore<>();
    private final String serverUrl;
    private final boolean generateId;

    protected MqttDevices(String serverUrl, boolean generateId) {
        this.serverUrl = serverUrl;
        this.generateId = generateId;
    }

    protected MqttDevices(String serverUrl) {
        this(serverUrl, false);
    }

    public void start(Path filePath) throws MqttException {
        client = new MqttClient(serverUrl, clientId());
        client.connect();
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.warn("MQTT connection lost", cause);
                reconnect(filePath);
            }
            @Override public void messageArrived(String topic, MqttMessage message) {}
            @Override public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        initDevices(filePath);
    }

    public void stop() throws MqttException {
        client.disconnect();
    }

    public <T extends TDevice> void register(T device) {
        this.registerDevices.add(device);
    }

    public <T extends TDevice> Optional<T> getDevice(Class<T> clazz, PositionPath positionPath) {
        return devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(d -> d.getPositionPath().isSame(positionPath))
                .findFirst();
    }

    public <T extends TDevice> List<T> getDevices(Class<T> clazz) {
        return devices.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public <T extends TDevice> List<T> getDevicesByRoom(Class<T> clazz, String location) {
        return devices.stream()
                .filter(clazz::isInstance)
                .filter(d -> d.getPositionPath().getLocation().equalsIgnoreCase(location))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public <T extends TDevice> List<T> getDevicesByFloor(Class<T> clazz, String floor) {
        return devices.stream()
                .filter(clazz::isInstance)
                .filter(d -> d.getPositionPath().getFloor().equalsIgnoreCase(floor))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    protected abstract String clientId();
    protected abstract Class<? extends TDevice> resolveDeviceClass(String type);
    protected abstract void subscribeEvents(TDevice device, String deviceId) throws MqttException;
    protected abstract void subscribeResponse(TDevice device, String deviceId) throws MqttException;


    // --- Private ---

    private void reconnect(Path filePath) {
        while (!client.isConnected()) {
            try {
                Thread.sleep(2000);
                client.connect();
                devices.clear();
                initDevices(filePath);
                Log.info("Reconnected MQTT");
            } catch (Exception e) {
                Log.error("Reconnect failed", e);
            }
        }
    }

    private void initDevices(Path filePath) {
        try {
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
                CsvWriter.build(filePath, "id", "device", "positionPath").writeLine();
            }

            CsvReader reader = CsvReader.build(Files.readString(filePath));
            List<TDevice> found = new ArrayList<>();
            List<TDevice> missingId = new ArrayList<>(); // neu

            while (reader.nextRow()) {
                String positionPath = reader.getCol("positionPath");
                if (positionPath == null || positionPath.isBlank()) continue;

                String type = reader.getCol("device");
                Class<? extends TDevice> clazz = resolveDeviceClass(type);
                if (clazz == null) continue;

                var mqttDevice = registerDevices.stream()
                        .filter(clazz::isInstance)
                        .filter(d -> d.getPositionPath().isSamePath(positionPath))
                        .findFirst();
                mqttDevice.ifPresent(found::add);

                String deviceId = reader.getCol("id");

                // ID leer + generateId → ID generieren
                if (deviceId == null || deviceId.isBlank()) {
                    if (generateId) {
                        mqttDevice.ifPresent(missingId::add);
                    }
                    continue;
                }

                mqttDevice.ifPresent(device -> {
                    device.init(deviceId, this);
                    try {
                        subscribeEvents(device, deviceId);
                        subscribeResponse(device, deviceId);
                        device.load();
                        devices.add(device);
                    } catch (Exception e) {
                        Log.error("Subscription failed for device {} [Error:{}]", type, e.getMessage());
                    }
                });
            }

            // fehlende Geräte (noch nie in CSV) anhängen
            CsvWriter writer = CsvWriter.build(filePath, "id", "device", "positionPath");
            registerDevices.stream()
                    .filter(d -> !found.contains(d))
                    .forEach(device -> writer
                            .setCol("device", device.getClass().getSimpleName())
                            .setCol("positionPath", device.getPositionPath().getPath())
                            .writeLine());

            // Geräte mit leerer ID → ID generieren und in CSV schreiben
            if (generateId && !missingId.isEmpty()) {
                updateIds(filePath, missingId);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateIds(Path filePath, List<TDevice> missingId) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(filePath));

        for (TDevice device : missingId) {
            String generatedId = device.getClass().getSimpleName().toLowerCase()
                    + "-" + Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000);

            // Zeile mit leerem ID-Feld für dieses Gerät finden und ersetzen
            for (int i = 1; i < lines.size(); i++) {
                String[] cols = lines.get(i).split(",", -1);
                if (cols.length >= 3
                        && cols[0].isBlank()
                        && cols[2].trim().equals(device.getPositionPath().getPath())) {
                    cols[0] = generatedId;
                    lines.set(i, String.join(",", cols));

                    // Gerät initialisieren und subscriben
                    device.init(generatedId, this);
                    try {
                        subscribeEvents(device, generatedId);
                        subscribeResponse(device, generatedId);
                        device.load();
                        devices.add(device);
                    } catch (Exception e) {
                        Log.error("Subscription failed for generated device [Error:{}]", e.getMessage());
                    }
                    break;
                }
            }
        }

        Files.write(filePath, lines);
    }
}