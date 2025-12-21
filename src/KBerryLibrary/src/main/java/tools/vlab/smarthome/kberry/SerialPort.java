package tools.vlab.smarthome.kberry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.smarthome.kberry.baos.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SerialPort {

    private static final Logger Log = LoggerFactory.getLogger(SerialPort.class);

    private final String device;
    private final int baudRate;
    private FileOutputStream out;
    private FileInputStream in;
    private Thread readThread;
    private volatile boolean running = false;

    // Liste für mehrere Listener (Thread-sicher)
    private final List<SerialPortListener> listeners = new CopyOnWriteArrayList<>();

    public SerialPort(String device, int baudRate) {
        this.device = device;
        this.baudRate = baudRate;
    }

    public void addListener(SerialPortListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(SerialPortListener listener) {
        this.listeners.remove(listener);
    }

    public boolean openPort() {
        try {
            String sttyCmd = String.format("stty -F %s %d parenb -parodd cs8 -cstopb raw -echo", device, baudRate);
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", sttyCmd});
            if (p.waitFor() != 0) return false;

            this.out = new FileOutputStream(device);
            this.in = new FileInputStream(device);

            this.running = true;
            startReading();
            return true;
        } catch (Exception e) {
            closePort();
            return false;
        }
    }

    private void startReading() {
        readThread = new Thread(() -> {
            ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();

            while (running) {
                try {
                    int available = in.available();
                    if (available > 0) {
                        byte[] chunk = new byte[available];
                        int len = in.read(chunk);
                        if (len > 0) {
                            packetBuffer.write(chunk, 0, len);
                        }
                    } else if (packetBuffer.size() > 0) {
                        // Paket-Erkennung: Wenn 40ms keine neuen Bytes kommen,
                        // betrachten wir das Telegramm als vollständig.
                        Thread.sleep(40);
                        if (in.available() == 0) {
                            byte[] fullPacket = packetBuffer.toByteArray();
                            for (SerialPortListener l : listeners) {
                                l.dataReceived(fullPacket);
                            }
                            packetBuffer.reset();
                        }
                    } else {
                        Thread.sleep(10); // CPU schonen
                    }
                } catch (IOException | InterruptedException e) {
                    if (running) System.err.println("Read Error: " + e.getMessage());
                }
            }
        }, "serial-reader");
        readThread.setDaemon(true);
        readThread.start();
    }

    public synchronized void writeBytes(byte[] data) {
        if (out == null) return;
        try {
            Log.debug("Write Data: {}", ByteUtil.toHex(data));
            out.write(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("Write Error: " + e.getMessage());
        }
    }

    public void closePort() {
        running = false;
        try {
            if (readThread != null) readThread.interrupt();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (Exception e) { /* ignore */ }
    }
}
