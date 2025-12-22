package tools.vlab.kberry.core.baos.messages.os;

import java.util.HashMap;
import java.util.Map;

/**
 * BAOS SubService Types
 * <p>
 * Coding rules:
 * - Request:    0x00 – 0x3F
 * - Indication: 0x40 – 0x7F
 * - Response:   0x80 – 0xBF
 */
public enum Indicator {

    // ---- ServerItem ----
    SERVER_ITEM_IND(0xC2),
    // ---- Datapoint Value ----
    DP_VALUE_IND(0xC1),

    UNKNOWN(0x00);

    public final int code;

    Indicator(int code) {
        this.code = code;
    }

    // ---------------------------------------------------------------------
    // Lookup
    // ---------------------------------------------------------------------
    private static final Map<Integer, Indicator> LOOKUP = new HashMap<>();

    static {
        for (Indicator t : values()) {
            LOOKUP.put(t.code, t);
        }
    }

    public static Indicator from(int code) {
        return LOOKUP.getOrDefault(code, UNKNOWN);
    }


    @Override
    public String toString() {
        return name() + "(0x" + Integer.toHexString(code).toUpperCase() + ")";
    }
}