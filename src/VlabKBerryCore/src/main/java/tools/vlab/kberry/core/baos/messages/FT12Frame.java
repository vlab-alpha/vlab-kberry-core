package tools.vlab.kberry.core.baos.messages;

import tools.vlab.kberry.core.baos.ByteUtil;
import tools.vlab.kberry.core.baos.messages.os.DataFramePayload;
import tools.vlab.kberry.core.baos.messages.os.Indicator;
import tools.vlab.kberry.core.baos.messages.os.Service;

public class FT12Frame {

    public static class Data extends Frame {

        private final static byte HOST_ODD = 0x73;
        private final static byte HOST_EVENT = 0x53;
        private final static byte OS_ODD = (byte) 0xF3;
        private final static byte OS_EVENT = (byte) 0xD3;
        private final static byte START = 0x68;
        private final static byte END = 0x16;
        private final static int CONTROL_BYTE_SIZE = 1;

        private final byte[] header;
        private final byte[] control;
        private final byte[] tail;

        private Data(byte[] header, byte control, byte[] payload, byte[] tail) {
            super(payload);
            this.header = header;
            this.control = new byte[]{control};
            this.tail = tail;
        }

        public static boolean is(byte[] frame) {
            return frame.length > 0 && frame[0] == 0x68;
        }

        public boolean isIndicator() {
            return getIndicator() != Indicator.UNKNOWN;
        }

        public boolean isResponse() {
            return getService() != Service.UNKNOWN;
        }

        public Indicator getIndicator() {
            return Indicator.from(payload[1] & 0xFF);
        }

//        public int getResponseCode() {
//            return Service.from(payload[1] & 0xFF).responseCode;
//        }

        public Service getService() {
            return Service.from(payload[1] & 0xFF);
        }

        public boolean isDataPoint() {
            return payload[1] == (byte) 0x85 || payload[1] == (byte) 0x86;
        }

        public boolean isServerItem() {
            return payload[1] == (byte) 0x81;
        }

        public int getId() {
            return ((getPayload()[2] & 0xFF) << 8) |
                    (getPayload()[3] & 0xFF);
        }


        @Override
        public byte[] toByteArray() {
            return ByteUtil.concatByteArrays(header, control, payload, tail);
        }

        public static Data request(DataFramePayload osPayload, boolean isOdd) {
            byte[] payload = osPayload.toByteArray();
            byte[] header = new byte[]{
                    START,
                    (byte) (payload.length + 1),
                    (byte) (payload.length + 1),
                    START
            };
            byte CR = isOdd ? HOST_ODD : HOST_EVENT;
            byte checksum = ft12Checksum(CR, payload);
            byte[] tail = new byte[]{
                    checksum,
                    END
            };
            return new Data(header, CR, payload, tail);
        }

        public static Data of(byte[] frame) {
            if (frame == null || frame.length < 7) // Minimal: Header + Control + CS + Tail
                throw new InvalidFormatException(String.format("Frame length %d is less than 7", frame != null ? frame.length : -1));

            // --- Header extrahieren ---
            byte start1 = frame[0];
            byte len1 = frame[1];
            byte len2 = frame[2];
            byte start2 = frame[3];

            if (start1 != START || start2 != START)
                throw new InvalidFormatException(String.format("Frame starts with invalid 0x68 S1:%s S2:%s", ByteUtil.toHex(start1), ByteUtil.toHex(start2)));

            byte control = frame[4];

            if (control != OS_EVENT && control != OS_ODD) {
                throw new InvalidFormatException(String.format("Invalid control byte %s", ByteUtil.toHex(control)));
            }

            int payloadLen = (len1 & 0xFF) - CONTROL_BYTE_SIZE; // minus Control Byte
            if (payloadLen < 0 || frame.length < 5 + payloadLen + 2)
                throw new InvalidFormatException(String.format("Frame length %d is less than 5", payloadLen));

            // --- Payload extrahieren ---
            byte[] payload = new byte[payloadLen];
            System.arraycopy(frame, 5, payload, 0, payloadLen);

            // --- Tail extrahieren ---
            byte checksum = frame[5 + payloadLen];
            byte tailByte = frame[6 + payloadLen];

            if (tailByte != END)
                throw new InvalidFormatException(String.format("Invalid tail byte %s", ByteUtil.toHex(tailByte)));

            // --- Checksumme prüfen ---
            byte calcChecksum = ft12Checksum(control, payload);
            if (checksum != calcChecksum)
                throw new InvalidFormatException(String.format(
                        "Invalid checksum! Expected=0x%02X, Found=0x%02X", calcChecksum, checksum
                ));

            // --- Data-Objekt erzeugen ---
            byte[] header = new byte[]{start1, len1, len2, start2};
            byte[] tail = new byte[]{checksum, tailByte};
            return new Data(header, control, payload, tail);
        }

        private static byte ft12Checksum(byte cr, byte[] payload) {
            int sum = cr & 0xFF; // CR einbeziehen
            for (byte b : payload) {
                sum += b & 0xFF;
            }
            return (byte) (sum & 0xFF); // modulo 256
        }

        @Override
        public String toHex() {
            return ByteUtil.toHex(toByteArray());
        }

    }

    public static class Reset extends Frame {

        private static final byte CONTROL_BYTE = 0x10;

        private Reset(byte[] data) {
            super(data);
        }

        @Override
        public byte[] toByteArray() {
            return payload;
        }

        public static Reset request() {
            return new Reset(new byte[]{CONTROL_BYTE, (byte) 0x40, (byte) 0x40, (byte) 0x16});
        }
    }

    public static class Ack extends Frame {

        private Ack(byte[] data) {
            super(data);
        }

        @Override
        public byte[] toByteArray() {
            return this.payload;
        }

        public static Ack of(byte[] frame) {
            if (frame.length < 1) {
                throw new InvalidFormatException(String.format("[ACK] Frame to short (%s)!", frame.length));
            }
            if (frame[0] != (byte) 0xE5) {
                throw new InvalidFormatException(String.format("[ACK] Invalid frame format (%s)!", frame[0]));
            }
            return new Ack(frame);
        }

        public static boolean is(byte[] frame) {
            return frame.length > 0 && frame[0] == (byte) 0xE5;
        }

        public static Ack ack() {
            return new Ack(new byte[]{(byte) 0xE5});
        }
    }
}