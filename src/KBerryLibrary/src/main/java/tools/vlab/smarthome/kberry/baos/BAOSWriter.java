package tools.vlab.smarthome.kberry.baos;

import com.fazecast.jSerialComm.SerialPort;
import tools.vlab.smarthome.kberry.Log;
import tools.vlab.smarthome.kberry.baos.messages.FT12Frame;
import tools.vlab.smarthome.kberry.baos.messages.os.DataFramePayload;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class BAOSWriter {

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
        boolean odd = isOddAndNext();
        var data = FT12Frame.Data.request(request, odd);
        frames.addLast(data.toByteArray());

        Log.debug(
                "REQ: %s seq=%d %s",
                odd ? "ODD" : "EVENT",
                sequence.get(),
                data.toHex()
        );
    }

    public void sendAck() {
        var ack = FT12Frame.Ack.ack();
        frames.addLast(ack.toByteArray());
        Log.debug("Ack: %s", ack.toHex());
    }

    public void sendReset() {
        var reset = FT12Frame.Reset.request();
        frames.addLast(reset.toByteArray());
        Log.debug(
                "RES: %s seq=%d %s",
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
                    serialPort.writeBytes(frame, frame.length);
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException ignored) {
        }
    }
}