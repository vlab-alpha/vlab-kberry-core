package tools.vlab.kberry.core.baos;

import lombok.Getter;
import tools.vlab.kberry.core.devices.RGB; // Stellen Sie sicher, dass dies die richtige Klasse ist

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalTime;

public enum KnxDatapointType {
    // Generische Typen (mit KNX DPT Hauptnummer in Klammern)
    BOOLEAN(1),     // DPT 1.xxx (Switch, Status)
    INT8(3),        // DPT 3.xxx (Steuern 4-bit, Richtung/Schrittweite)
    CHAR(4),        // DPT 4.xxx (ASCII Character)
    UINT8(5),       // DPT 5.xxx (Unsigned 8-bit, z.B. Prozent, Helligkeit)
    SINT8(6),       // DPT 6.xxx (Signed 8-bit)
    UINT16(7),      // DPT 7.xxx (Unsigned 16-bit)
    SINT16(8),      // DPT 8.xxx (Signed 16-bit)
    FLOAT9(9),      // DPT 9.xxx (KNX 2-byte float, Temperatur/Lux/VOC)
    TIME(10),       // DPT 10.xxx (Time)
    DATE(11),       // DPT 11.xxx (Date)
    UINT32(12),     // DPT 12.xxx (Unsigned 32-bit)
    SINT32(13),     // DPT 13.xxx (Signed 32-bit)
    FLOAT32(14),    // DPT 14.xxx (Standard IEEE 754 4-byte float)
    STRING_14(16),  // DPT 16.001 (14-Byte String)
    DATETIME(11),   // DPT 19.001 (8-Byte Datum/Zeit)

    // Die folgenden 1-Byte-Enums teilen sich in BAOS die Property ID 20
    SCENE_NUMBER(18),// DPT 17.001 (Szenennummer)
    KNX_ACCESS(20),  // DPT 18.001 (Zugriffskontrolle)
    HVAC_MODE(4),   // DPT 20.102 (1 Byte Heizung/Klima Modus)

    RGB(10);         // DPT 232.600 (3-byte RGB)


    // Der Start-Offset für die Nutzlast (Wert) im cEMI/BAOS Frame
    private static final int VALUE_START_OFFSET = 15;
    @Getter
    private final int type;

    KnxDatapointType(int type) {
        this.type = type;
    }

    // --- DEKODIERUNGSMETHODEN ---

    // Die Prüfungen stellen nur sicher, dass die DPT Hauptnummer übereinstimmt, nicht die Enum-Konstante selbst.

    public boolean decodeBoolean(byte[] frame) {
        if (type != 1) throw new InvalidDatapointType(String.format("%s is not a boolean DPT", name()));
        return (frame[VALUE_START_OFFSET] & 0x01) != 0;
    }

    public char decodeChar(byte[] frame) {
        if (type != 4) throw new InvalidDatapointType(String.format("%s is not a char DPT", name()));
        return (char) frame[VALUE_START_OFFSET];
    }

    public int decodeInt8(byte[] frame) {
        if (type != 5 && type != 6 && type != 17 && type != 20) throw new InvalidDatapointType(String.format("%s is not an 8-bit integer DPT", name()));
        return (frame[VALUE_START_OFFSET] & 0xFF);
    }

    public int decodeInt16(byte[] frame) {
        if (type != 7 && type != 8) throw new InvalidDatapointType(String.format("%s is not a 16-bit integer DPT", name()));
        ByteBuffer valueBuffer = ByteBuffer.wrap(frame, VALUE_START_OFFSET, 2).order(ByteOrder.BIG_ENDIAN);
        return valueBuffer.getShort();
    }

    public long decodeInt32(byte[] frame) {
        if (type != 12 && type != 13) throw new InvalidDatapointType(String.format("%s is not a 32-bit integer DPT", name()));
        ByteBuffer valueBuffer = ByteBuffer.wrap(frame, VALUE_START_OFFSET, 4).order(ByteOrder.BIG_ENDIAN);
        return valueBuffer.getInt();
    }

    public float decodeFloat9(byte[] frame) {
        if (type != 9) throw new InvalidDatapointType(String.format("%s is not a DPT 9 float DPT", name()));
        ByteBuffer valueBuffer = ByteBuffer.wrap(frame, VALUE_START_OFFSET, 2).order(ByteOrder.BIG_ENDIAN);
        short raw = valueBuffer.getShort();
        return convertKnxFloatToDouble(raw);
    }

    public float decodeFloat32(byte[] frame) {
        if (type != 14) throw new InvalidDatapointType(String.format("%s is not a DPT 14 float DPT", name()));
        ByteBuffer valueBuffer = ByteBuffer.wrap(frame, VALUE_START_OFFSET, 4).order(ByteOrder.BIG_ENDIAN);
        return valueBuffer.getFloat();
    }

    public String decodeString14(byte[] frame) {
        if (type != 16) throw new InvalidDatapointType(String.format("%s is not a DPT 16 String DPT", name()));
        return new String(frame, VALUE_START_OFFSET, 14);
    }

    public LocalTime decodeTime(byte[] frame) {
        if (type != 10) throw new InvalidDatapointType(String.format("%s is not a DPT 10 Time DPT", name()));
        return LocalTime.of(
                frame[VALUE_START_OFFSET] & 0x1F,
                frame[VALUE_START_OFFSET+1] & 0x3F,
                frame[VALUE_START_OFFSET+2] & 0x3F
        );
    }

    public LocalDate decodeDate(byte[] frame) {
        if (type != 11) throw new InvalidDatapointType(String.format("%s is not a DPT 11 Date DPT", name()));
        return LocalDate.of(
                (frame[VALUE_START_OFFSET+2] & 0xFF) + 1900,
                frame[VALUE_START_OFFSET+1] & 0x0F,
                frame[VALUE_START_OFFSET] & 0x1F
        );
    }

    public RGB decodeRgb(byte[] frame) {
        if (type != 232) throw new InvalidDatapointType(String.format("%s is not a RGB DPT", name()));
        int r = frame[VALUE_START_OFFSET] & 0xFF;
        int g = frame[VALUE_START_OFFSET + 1] & 0xFF;
        int b = frame[VALUE_START_OFFSET + 2] & 0xFF;
        return new RGB(r, g, b);
    }


    // --- HILFSMETHODEN ---

    private static float convertKnxFloatToDouble(short raw) {
        int sign = (raw >> 15) & 0x1;
        int exponent = (raw >> 11) & 0xF;
        int mantissa = raw & 0x7FF;
        int actualExponent = exponent - 15;
        double actualMantissa;
        if (sign == 1) {
            mantissa = mantissa | 0xF800;
            actualMantissa = (double)mantissa / 2048.0;
        } else {
            actualMantissa = (double)mantissa / 2048.0;
        }
        return (float) (actualMantissa * Math.pow(2, actualExponent));
    }
}
