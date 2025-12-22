package tools.vlab.kberry.core.baos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.SerialPort;
import tools.vlab.kberry.core.SerialPortListener;
import tools.vlab.kberry.core.baos.messages.FT12Frame;
import tools.vlab.kberry.core.baos.messages.os.DataFramePayload;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class BAOSReader implements SerialPortListener {

    private static final Logger Log = LoggerFactory.getLogger(BAOSReader.class);

    private final SerialPort port;
    private final FT12StreamParser parser = new FT12StreamParser();

    private final AtomicLong ackTS = new AtomicLong(0);
    private final ConcurrentHashMap<String, CompletableFuture<FT12Frame.Data>> responseFrames = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<FT12Frame.Data> indicatorFrames = new ConcurrentLinkedDeque<>();
    private final AckWriter ackWriter;

    public BAOSReader(SerialPort port, AckWriter ackWriter) {
        this.port = port;
        this.ackWriter = ackWriter;
    }

    public void start() {
        port.addListener(this);
    }

    public void stop() {
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
                throw new TimeoutException("Timeout " + timeoutMS() + "ms [" + payload.getId() + "]");
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

    @Override
    public void dataReceived(byte[] serialData) {
        parser.feed(serialData);
        byte[] frame;
        while ((frame = parser.pollFrame()) != null) {

            if (FT12Frame.Ack.is(frame)) {
                ackTS.set(System.currentTimeMillis());
                continue;
            }
            if (FT12Frame.Data.is(frame)) {
                ackWriter.ack();
                FT12Frame.Data data = FT12Frame.Data.of(frame);
                if (data.isIndicator()) {
                    Log.debug("IND: ?: {}", data.toHex());
                    indicatorFrames.add(data);
                } else if (data.isResponse()) {
                    getFuture(data).complete(data);
                } else {
                    Log.error("No Datapoint or server Item: {}", data.toHex());
                }
                continue;
            }
            Log.error("Unknown frame: {}", ByteUtil.toHex(frame));
        }
    }

    private CompletableFuture<FT12Frame.Data> getFuture(DataFramePayload payload) {
        var id = id(payload);
        return this.responseFrames.get(id);
    }

    private CompletableFuture<FT12Frame.Data> getFuture(FT12Frame.Data data) {
        return this.responseFrames.computeIfAbsent(String.format("%s@%s", data.getService().getResponseCode(), data.getId()), k -> new CompletableFuture<>());
    }

    private void putFuture(DataFramePayload payload, CompletableFuture<FT12Frame.Data> future) {
        var id = id(payload);
        this.responseFrames.put(id, future);
    }

    private boolean containsFuture(DataFramePayload payload) {
        return this.responseFrames.containsKey(String.format("%s@%s", payload.getService().getResponseCode(), payload.getId()));
    }

    private static String id(DataFramePayload payload) {
        return String.format("%s@%s", payload.getService().getResponseCode(), payload.getId());
    }


}