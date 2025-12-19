package tools.vlab.smarthome.kberry.baos;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import tools.vlab.smarthome.kberry.baos.messages.FT12Frame;
import tools.vlab.smarthome.kberry.Log;
import tools.vlab.smarthome.kberry.baos.messages.os.DataFramePayload;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class BAOSReader {

    private final SerialPort port;
    private volatile boolean running = false;
    private InputStream in;
    private final FT12StreamParser parser = new FT12StreamParser();

    private final AtomicLong ackTS = new AtomicLong(0);
    private final ConcurrentHashMap<String, CompletableFuture<FT12Frame.Data>> responseFrames = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<FT12Frame.Data> indicatorFrames = new ConcurrentLinkedDeque<>();

    public BAOSReader(SerialPort port) {
        this.port = port;
    }

    public void start() {
        running = true;
        in = port.getInputStream();
        Thread t = new Thread(this::readLoop, "BAOS-Reader");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        running = false;
    }

    public FT12Frame.Data nextResponse(DataFramePayload payload) throws BAOSReadException {
        try {
            return getFuture(payload).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BAOSReadException("Reading interrupted!", e);
        }
    }

    public FutureFrame responseOf(DataFramePayload payload, int timeoutMs) {
        CompletableFuture<FT12Frame.Data> future;
        if (!containsFuture(payload)) {
            future = new CompletableFuture<>();
            putFuture(payload, future);
        } else {
            future = getFuture(payload);
        }
        return new FutureFrame(future, payload, timeoutMs, responseFrames);
    }

    public record FutureFrame(CompletableFuture<FT12Frame.Data> future, DataFramePayload payload, int timeoutMS,
                              ConcurrentHashMap<String, CompletableFuture<FT12Frame.Data>> pending) {

        public FT12Frame.Data waitForResult() throws TimeoutException {
            try {
                var buffer = future.get(timeoutMS(), TimeUnit.MILLISECONDS);
                pending.remove(String.format("%s@%d", payload.getService(), payload.getId()));
                return buffer;
            } catch (Exception e) {
                pending.remove(String.format("%s@%d", payload.getService(), payload.getId()));
                throw new TimeoutException("Timeout waiting for " + payload.getId());
            }
        }

    }


    /**
     * Check indicator
     */
    public Optional<FT12Frame.Data> nextIndicator() {
        var frame = indicatorFrames.poll();
        if (frame == null) {
            return Optional.empty();
        }
        return Optional.of(frame);
    }

    /**
     * Wartet auf ein ACK
     */
    public void waitForAck(long timeoutMs) throws TimeoutException {
        long start = System.currentTimeMillis();
        while (ackTS.get() < start) {
            if (System.currentTimeMillis() - start > timeoutMs)
                throw new TimeoutException("Ack Timeout > " + timeoutMs + "ms");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        ackTS.set(0);
    }

    private void readLoop() {
        try {
            while (running) {
                try {
                    byte[] tmp = new byte[256];
                    int n = in.read(tmp);
                    //Log.debug("Read loop byte " + ByteUtil.toHex(tmp) + " bytes");
                    parser.feed(tmp, n);

                    byte[] frame;
                    while ((frame = parser.pollFrame()) != null) {

                        if (FT12Frame.Ack.is(frame)) {
                            ackTS.set(System.currentTimeMillis());
                            continue;
                        }

                        if (FT12Frame.Data.is(frame)) {
                            FT12Frame.Data data = FT12Frame.Data.of(frame);
//                        Log.debug("Received data: %s %s", data.toHex(), data.getSubService().isIndication());
                            if (data.isIndicator()) {
                                Log.debug("IND: ?: %s", data.toHex());
                                indicatorFrames.add(data);
                            } else if (data.isResponse()) {
//                                Log.debug("RES: %s@%s: %s", data.getService().getResponseCode(), data.getId(), data.toHex());
                                getFuture(data).complete(data);
                            } else {
                                Log.error("No Datapoint or server Item: %s", data.toHex());
                            }
                            continue;
                        }

                        Log.error("Unknown frame: %s", ByteUtil.toHex(frame));
                    }
                } catch (SerialPortTimeoutException e) {
                    Thread.sleep(10);
                } catch (Exception e) {
                    Log.error(e, "Error reading BAOS Frames");
                }
            }
        } catch (InterruptedException e) {
            Log.error(e, "Error reading BAOS Frames");
        }
    }

    private CompletableFuture<FT12Frame.Data> getFuture(DataFramePayload payload) {
        var id = id(payload);
        Log.debug("getFuture: %s", id);
        return this.responseFrames.get(id);
    }

    private CompletableFuture<FT12Frame.Data> getFuture(FT12Frame.Data data) {
        return this.responseFrames.computeIfAbsent(String.format("%s@%s", data.getService().getResponseCode(), data.getId()), k -> new CompletableFuture<>());
    }

    private void putFuture(DataFramePayload payload, CompletableFuture<FT12Frame.Data> future) {
        var id = id(payload);
        Log.debug("putFuture: %s", id);
        this.responseFrames.put(id, future);
    }

    private boolean containsFuture(DataFramePayload payload) {
        return this.responseFrames.containsKey(String.format("%s@%s", payload.getService().getResponseCode(), payload.getId()));
    }

    private static String id(DataFramePayload payload) {
        return String.format("%s@%s", payload.getService().getResponseCode(), payload.getId());
    }


}