package tools.vlab.smarthome.kberry.devices;

import lombok.Getter;

import java.util.stream.Stream;

public enum HeaterMode {
    COMFORT(0), STANDBY(1), NIGHT(2), FROST_PROTECTION(3);

    @Getter
    private final int mode;

    HeaterMode(int mode) {
        this.mode = mode;
    }

    public static HeaterMode valueOf(int mode) {
        return Stream.of(HeaterMode.values()).filter(e -> e.mode == mode).findFirst().orElse(null);
    }
}
