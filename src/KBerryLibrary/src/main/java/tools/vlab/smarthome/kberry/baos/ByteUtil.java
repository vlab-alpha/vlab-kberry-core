package tools.vlab.smarthome.kberry.baos;

import tools.vlab.smarthome.kberry.devices.RGB;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ByteUtil {

    public static byte[] concatByteArrays(byte[]... arrays) {
        // Berechne die Gesamtlänge
        int totalLength = 0;
        for (byte[] arr : arrays) {
            totalLength += arr.length;
        }

        // Neues Array anlegen
        byte[] result = new byte[totalLength];
        int currentPos = 0;

        // Alle Arrays nacheinander kopieren
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, currentPos, arr.length);
            currentPos += arr.length;
        }

        return result;
    }

    public static String toHex(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        for (byte b : frame) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    public static String toHex(byte frame) {
        return String.format("0x%02X", frame);
    }

    public static int uInt16(byte[] payload) {
        return uInt16(payload, 0);
    }

    public static int uInt16(byte[] payload, int index) {
        var bytes = Arrays.copyOfRange(payload, index, index + 2);
        return ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
    }

    public static int uInt8(byte[] payload) {
        return uInt8(payload, 0);
    }

    public static int uInt8(byte[] payload, int index) {
        return payload[index] & 0xFF;
    }

    public static float toFloat32(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getFloat();
    }

    public static float toFloat9(byte[] payload) {
        if (payload.length != 2) throw new IllegalArgumentException("Float9 payload must be 2 bytes");

        int hi = payload[0] & 0xFF; // erstes Byte
        int lo = payload[1] & 0xFF; // zweites Byte

        boolean negative = (hi & 0x80) != 0;       // Vorzeichen-Bit
        int exponent = (hi >> 3) & 0x0F;           // 4 Bit Exponent
        int mantissa = ((hi & 0x07) << 8) | lo;   // 11 Bit Mantisse

        float value = (float) (mantissa * Math.pow(2, exponent) * 0.01);
        if (negative) value = -value;

        return value;
    }

    public static RGB rgb(byte[] payload) {
        return new RGB(payload[0] & 0xFF, payload[1] & 0xFF, payload[2] & 0xFF);
    }

    public static byte[] copy(byte[] payload, int itemIndex, int dataLength) {
        return Arrays.copyOfRange(payload, itemIndex, itemIndex + dataLength);
    }

    public static boolean bool(byte[] payload, int index) {
        return (payload[index] & 0xFF) == 1;
    }

    public static boolean bit(byte value, int position) {
        return (value & (1 << position)) != 0;
    }

    public static Boolean bool(byte[] payload) {
        return bool(payload, 0);
    }

    public static byte[] uInt8(int value) {
        return new byte[]{(byte) (value & 0xFF)};
    }

    public static byte[] int8(int value) {
        return new byte[]{(byte) value};
    }

    public static byte[] sInt8(int value) {
        return new byte[]{(byte) value};
    }

    public static byte[] sint16(int value) {
        return uint16(value); // signed 16bit, Big Endian
    }

    public static byte[] sint32(int value) {
        return uint32(value);
    }

    public static byte[] uint16(int value) {
        return new byte[]{(byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
    }

    public static byte[] uint32(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    public static byte[] float9(float value) {
        boolean negative = value < 0;
        if (negative) value = -value;

        // Suche geeigneten Exponent
        int exponent = 0;
        int mantissa = 0;
        for (exponent = 0; exponent < 16; exponent++) {
            mantissa = Math.round(value / (float)(Math.pow(2, exponent) * 0.01f));
            if (mantissa <= 0x7FF) break; // 11 Bit
        }

        int hi = ((negative ? 0x80 : 0x00) | ((exponent & 0x0F) << 3) | ((mantissa >> 8) & 0x07)) & 0xFF;
        int lo = mantissa & 0xFF;

        return new byte[]{(byte) hi, (byte) lo};
    }

    public static byte[] float32(float value) {
        int intBits = Float.floatToIntBits(value);
        return uint32(intBits);
    }

    public static byte[] rgb(RGB value) {
        return new byte[]{
                (byte) value.red(),
                (byte) value.green(),
                (byte) value.blue()
        };
    }

    public static byte[] bool(boolean value) {
        return new byte[]{(byte) (value ? 1 : 0)};
    }
}
