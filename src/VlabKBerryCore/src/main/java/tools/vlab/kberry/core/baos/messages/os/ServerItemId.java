package tools.vlab.kberry.core.baos.messages.os;

import lombok.Getter;

import java.util.Arrays;

public enum ServerItemId {
    HARDWARE_TYPE(1,"KNX Hardware Type"),
    HARDWARE_VERSION(2,"Version of the ObjectServer hardware"),
    FIRMWARE_VERSION(3,"Version of the ObjectServer firmware"),
    KNX_MANUFACTURE_DEV(4,"KNX manufacturer code of the device, not modified by ETS"),
    KNX_MANUFACTURE_APP(5,"KNX manufacturer code loaded by ETS"),
    APP_ID(6,"ID of application loaded by ETS"),
    APP_VERSION(7,"Version of application loaded by ETS"),
    SERIAL_NUMBER(8,"Serial number of device."),
    TIME_SINCE_RESET(9,"Time since reset"),
    BUS_CONNECTION_STATE(10, true,"Bus connection state"),
    UNKNOWN(99,"Unknown start item id!")
    ;

    @Getter
    private final int id;
    @Getter
    private final boolean indication;
    @Getter
    private final String description;

    ServerItemId(int id, boolean indication, String description) {
        this.id = id;
        this.indication = indication;
        this.description = description;
    }

    ServerItemId(int id, String description) {
        this(id, false, description);
    }

    public static ServerItemId withCode(int id) {
        return Arrays.stream(values()).filter(e -> e.id == id).findFirst().orElse(UNKNOWN);
    }

    public static ServerItemId first() {
        return ServerItemId.HARDWARE_TYPE;
    }

    public int numberOfItems() {
        return 11 - id;
    }

    public static ServerItemId All() {
        return  ServerItemId.HARDWARE_TYPE;
    }
}
