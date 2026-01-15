package tools.vlab.kberry.core.baos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.SerialPort;
import tools.vlab.kberry.core.baos.messages.FT12Frame;
import tools.vlab.kberry.core.baos.messages.os.DataFramePayload;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class BAOSWriter implements AckWriter {

    private static final Logger Log = LoggerFactory.getLogger(BAOSWriter.class);

    private final SerialPort serialPort;
    private volatile boolean running = false;
    private final ConcurrentLinkedDeque<byte[]> frames = new ConcurrentLinkedDeque<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    public BAOSWriter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public void start() {
        running = true;
        Thread t = new Thread(this::writeLoop, "BAOS-Writer");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        running = false;
    }

    public void sendDataFrame(DataFramePayload request) {
        sendDataFrame(request, false);
    }

    public void sendDataFrame(DataFramePayload request, boolean priority) {
        boolean odd = isOddAndNext();
        var data = FT12Frame.Data.request(request, odd);
        if (priority) {
            frames.addFirst(data.toByteArray());
        } else {
            frames.addLast(data.toByteArray());
        }
        Log.debug(
                "Add To Stack: {} seq={} priority={} {}",
                odd ? "ODD" : "EVENT",
                sequence.get(),
                priority,
                data.toHex()
        );
    }

    public void sendAck() {
        var ack = FT12Frame.Ack.ack();
        frames.addLast(ack.toByteArray());
    }

    public void sendReset() {
        var reset = FT12Frame.Reset.request();
        frames.addLast(reset.toByteArray());
        Log.debug(
                "RES: {} seq={} {}",
                isOdd() ? "ODD" : "EVENT",
                sequence.get(),
                reset.toHex()
        );
    }

    public void resetSequence() {
        this.sequence.set(1);
    }

    private boolean isOddAndNext() {
        return sequence.getAndIncrement() % 2 == 1;
    }

    private boolean isOdd() {
        return sequence.get() % 2 == 1;
    }


    private void writeLoop() {
        try {
            while (running) {
                byte[] frame = frames.pollFirst();
                if (frame != null) {
                    serialPort.writeBytes(frame);
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void ack() {
        sendAck();
    }
}