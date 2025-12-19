package tools.vlab.smarthome.kberry.baos.messages.os;

import lombok.Getter;

import java.util.Arrays;

public enum Service {
    // ---- ServerItem ----
    GET_SERVER_ITEM(0x81, 0x01),
    SET_SERVER_ITEM(0x82, 0x02),

    // ---- Datapoint Description ----
    GET_DP_DESC(0x83, 0x03),

    // ---- Description String ----
    GET_DESC_STRING(0x84, 0x04),

    // ---- Datapoint Value ----
    GET_DP_VALUE(0x85, 0x05),

    // ---- Set Datapoint Value ----
    SET_DP_VALUE(0x86, 0x06),

    // ---- Parameter Byte ----
    GET_PARAM_BYTE(0x87, 0x07),
    SET_PARAM_BYTE(0x88, 0x08),
    UNKNOWN(0x00, 0x00);

    @Getter
    private final int responseCode;
    private final int requestCode;

    Service(int responseCode, int requestCode) {
        this.responseCode = responseCode;
        this.requestCode = requestCode;
    }

    public static Service from(int requestOrResponseCode) {
        return Arrays.stream(values())
                .filter(entry -> entry.requestCode == requestOrResponseCode || entry.responseCode == requestOrResponseCode)
                .findFirst()
                .orElse(UNKNOWN);
    }


    @Override
    public String toString() {
        return Integer.toHexString(responseCode).toUpperCase() + Integer.toHexString(requestCode).toUpperCase();
    }
}
