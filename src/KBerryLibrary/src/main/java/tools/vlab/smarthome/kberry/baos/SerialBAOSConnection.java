package tools.vlab.smarthome.kberry.baos;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Setter;
import lombok.SneakyThrows;
import tools.vlab.smarthome.kberry.Log;
import tools.vlab.smarthome.kberry.ReloadDevice;
import tools.vlab.smarthome.kberry.baos.messages.FT12Frame;
import tools.vlab.smarthome.kberry.baos.messages.os.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SerialBAOSConnection {

    private final SerialPort port;
    private final int timeout;
    private final ConcurrentHashMap<ServerItemId, Consumer<GetServerItem.Response.ServerItem>> statusListener = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Consumer<DataPoint>> valueChangeListener = new ConcurrentHashMap<>();

    private final BAOSWriter writer;
    private final BAOSReader reader;
    private volatile boolean running = false;
    private final Object writeLock = new Object();
    private final int retries;
    @Setter
    private ReloadDevice reloadDevice;

    public SerialBAOSConnection(String device, int timeout, int retries) {
        port = SerialPort.getCommPort(device);
        this.timeout = timeout;
        this.retries = retries;
        this.writer = new BAOSWriter(port);
        this.reader = new BAOSReader(port);
    }

    public void connect() {
        port.setComPortParameters(19200, 8, SerialPort.ONE_STOP_BIT, SerialPort.EVEN_PARITY);
        if (this.timeout < 50) {
            throw new RuntimeException("Timeout should be more than 50!");
        }
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 50, 0); // Timeout 5s
        if (port.openPort()) {
            writer.start();
            reader.start();
            writer.sendReset();
//            reader.waitForAck(2000); // Anscheinend wird das nicht gesendet!
            startObserver();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (reloadDevice != null) {
                reloadDevice.load();
            }
        } else {
            throw new RuntimeException("Failed to start serial port");
        }
    }

    public void disconnect() {
        stopObserver();
        writer.stop();
        reader.stop();
        port.closePort();
    }


    @SneakyThrows
    private void startObserver() {
        running = true;
        Thread t = new Thread(this::indicatorLoop, "BAOS-Inidicator");
        t.setDaemon(true);
        t.start();
    }

    private void stopObserver() {
        running = false;
    }

    private void indicatorLoop() {
        try {
            while (running) {
                var indicator = reader.nextIndicator();
                if (indicator.isPresent()) {
                    writer.sendAck();
                    switch (indicator.get().getIndicator()) {
                        case SERVER_ITEM_IND -> GetServerItem.Indicator
                                .frameData(indicator.get())
                                .getItems()
                                .forEach(serverItem -> Optional
                                        .ofNullable(statusListener.get(serverItem.id()))
                                        .ifPresent(listener -> listener.accept(serverItem)));
                        case DP_VALUE_IND -> GetDatapointValue.Indicator
                                .frameData(indicator.get())
                                .getDataPoints()
                                .forEach(dp -> {
//                                    Log.debug("Listener: %d  %s", dp.id().id(), valueChangeListener.containsKey(dp.id().id()));
                                    Optional
                                            .ofNullable(valueChangeListener.get(dp.id().id()))
                                            .ifPresent(listener -> listener.accept(dp));
                                });
                    }
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Log.error(e, "Interrupted");
        }

    }

    public void onValueChanged(DataPointId objectId, Consumer<DataPoint> listener) {
        valueChangeListener.put(objectId.id(), listener);
    }

    public void onStatusChanged(ServerItemId id, Consumer<GetServerItem.Response.ServerItem> listener) {
        statusListener.put(id, listener);
    }

    public void write(DataPoint dataPoint) throws BAOSWriteException {
        synchronized (writeLock) {
            try {
                var request = SetDatapointValue.Request.setCacheAndBus(dataPoint);
                var future = reader.responseOf(request, timeout);
                writer.sendDataFrame(request);
                var frameData = future.waitForResult();
//                reader.waitForAck(timeout); // TODO: Test
//                var frameData = reader.waitForNextResponse(dataPoint.id(), timeout);
                var response = SetDatapointValue.Response.frameData(frameData);
                if (response.isFailed()) {
                    throw new BAOSWriteException("BAOS cannot be set [ERROR: " + response.error().getDescription() + "]");
                }
            } catch (TimeoutException e) {
                throw new BAOSWriteException("Timeout [ERROR: " + e.getMessage() + "]");
            }
        }
    }

    public DataPoint read(DataPointId id) throws BAOSReadException {
        synchronized (writeLock) {
            try {
                var dp = readCache(id);
                if (dp.isPresent()) {
                    return dp.get();
                }
                updateCacheViaBus(id);
                throw new BAOSReadException("Value not found for [" + id.id() + "]. Trigger update cache successfully!");
            } catch (TimeoutException e) {
                throw new BAOSReadException("Timeout!", e);
            }
        }
    }

    public void reset() {
        synchronized (writeLock) {
            writer.sendReset();
            writer.resetSequence();
        }
    }

    public void reset(DataPointId id) throws TimeoutException {
        synchronized (writeLock) {
            writer.sendDataFrame(SetDatapointValue.Request.clearDPStatus(id));
            reader.waitForAck(timeout);
        }
    }

    public List<String> getAllStatus() {
        synchronized (writeLock) {
            try {
                var request = GetServerItem.Request
                        .create()
                        .serverItem(ServerItemId.All());

                var future = reader.responseOf(request, timeout);
                writer.sendDataFrame(request);
                //reader.waitForAck(timeout);
                var frameData = future.waitForResult();
                var response = GetServerItem.Response.frameData(frameData);
                if (response.isSuccess()) {
                    return response.getItems().stream().map(serverItem -> switch (serverItem.id()) {
                        case HARDWARE_TYPE ->
                                String.format("%s: %s", serverItem.id().getDescription(), serverItem.stringData());
                        case BUS_CONNECTION_STATE ->
                                String.format("%s: %s", serverItem.id().getDescription(), (serverItem.boolData() ? "available" : "not available"));
                        case HARDWARE_VERSION, FIRMWARE_VERSION ->
                                String.format("%s v%d", serverItem.id().getDescription(), serverItem.intData());
                        default -> String.format("%s %s", serverItem.id().getDescription(), serverItem.toHex());
                    }).toList();
                }
                Log.error("No Success response for get all status status [ServerItemId.All]");
                return new ArrayList<>();
            } catch (Exception e) {
                Log.error(e, "Get All Status Error");
                return new ArrayList<>();
            }
        }
    }

    private Optional<DataPoint> readCache(DataPointId id) throws BAOSReadException, TimeoutException {
        var request = GetDatapointValue.Request.getDP(id);
        var future = reader.responseOf(request, timeout);
        writer.sendDataFrame(request);
        FT12Frame.Data firstGetDataPointframeData = future.waitForResult();
        var response = GetDatapointValue.Response.frameData(firstGetDataPointframeData);
        Log.debug("Get DP [" + id.id() + "] response [" + response.getStartDatapoint().id() + "] " + firstGetDataPointframeData.toHex());

        if (!response.isSuccess()) {
            throw new BAOSReadException("Object server read failed [ERROR:" + response.getError().getDescription() + "]!");
        }
        if (!response.isValid()) {
            throw new BAOSReadException("Invalid BAOS message [M:" + response.getMainService() + " | S:" + response.getSubService() + "]!");
        }
        if (response.foundInOSCache() && !response.anyProgress()) {
            return response.getFirstDataPoint();
        }
        if (response.foundInOSCache()) {
            var inProgress = true;
            int retriesLeft = this.retries;
            while (inProgress && retriesLeft > 0) {
                var data = reader.nextResponse(request);
                writer.sendAck();
                var progresResponse = GetDatapointValue.Response.frameData(data);
                inProgress = progresResponse.anyProgress();
                var newDP = progresResponse.getFirstDataPoint();
                if (newDP.isPresent()) {
                    return newDP;
                }
                retriesLeft--;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new BAOSReadException("Sleep Interrupted", e);
                }
            }
            if (inProgress) reset(id);
        }
        Log.debug("FAILED: Cache:%s; Success:%s; progress:%s", response.foundInOSCache(), response.isSuccess(), response.anyProgress());
        return Optional.empty();
    }

    private void updateCacheViaBus(DataPointId id) throws TimeoutException, BAOSReadException {
        var request = SetDatapointValue.Request.updateCache(id);
        Log.info("Update object server cache via bus for id %s!", id.id());
        var future = reader.responseOf(request, timeout);
        writer.sendDataFrame(request);
        var setDataPointFrame = future.waitForResult();
        writer.sendAck();
        var setDP = SetDatapointValue.Response.frameData(setDataPointFrame);
        Log.info("Receive setDataPointFrame response!! ID:%s Fail:%s Hex:%s", id.id(), setDP.isFailed(), setDataPointFrame.toHex());
        if (setDP.isFailed()) {
            throw new BAOSReadException("Update cache failed [ERROR:" + setDP.error().getDescription() + "]");
        }
    }

}