package tools.vlab.smarthome.kberry.baos;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class FT12StreamParser {

    private final byte[] buffer = new byte[4096];
    private int writePos = 0;

    // FIXME: das ist nicht Thread Save!
    private final Queue<byte[]> frames = new ArrayDeque<>();

    /**
     * Bytes aus dem Stream zuführen
     */
    public synchronized void feed(byte[] data, int length) {
        if (length <= 0) return;

        if (writePos + length > buffer.length) {
            // Buffer-Overflow → Resync
            writePos = 0;
        }

        System.arraycopy(data, 0, buffer, writePos, length);
        writePos += length;

        parse();
    }

    public synchronized byte[] pollFrame() {
        return frames.poll();
    }

    private void parse() {
        int i = 0;

        while (i < writePos) {

            // -------- ACK --------
            if (buffer[i] == (byte) 0xE5) {
                frames.add(new byte[]{(byte) 0xE5});
                i += 1;
                continue;
            }

            // -------- DATA FRAME --------
            if (buffer[i] == 0x68) {

                // Minimum Header prüfen
                if (i + 3 >= writePos) break;

                byte len1 = buffer[i + 1];
                byte len2 = buffer[i + 2];

                if (len1 != len2 || buffer[i + 3] != 0x68) {
                    i++; // kein gültiger Header → weitersuchen
                    continue;
                }

                int frameLength = 6 + (len1 & 0xFF);

                if (i + frameLength > writePos) {
                    // Frame noch nicht vollständig
                    break;
                }

                if (buffer[i + frameLength - 1] != 0x16) {
                    // kein gültiges Ende → Resync
                    i++;
                    continue;
                }

                byte[] frame = Arrays.copyOfRange(buffer, i, i + frameLength);
                frames.add(frame);

                i += frameLength;
                continue;
            }

            // -------- UNBEKANNTER BYTE → verwerfen --------
            i++;
        }

        // Restbytes nach vorne schieben
        if (i > 0) {
            System.arraycopy(buffer, i, buffer, 0, writePos - i);
            writePos -= i;
        }
    }
}
