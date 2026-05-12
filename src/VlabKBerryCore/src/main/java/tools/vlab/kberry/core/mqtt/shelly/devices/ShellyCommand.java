package tools.vlab.kberry.core.mqtt.shelly.devices;

public enum ShellyCommand {
    SET_SWITCH_STATUS("Switch.Set"),
    GET_SWITCH_STATUS("Switch.GetStatus"),
    SET_RGBW_STATUS("RGBW.Set"),
    GET_RGBW_STATUS("RGBW.GetStatus"),
    NOTIFY_STATUS("NotifyStatus");

    private final String name;

    ShellyCommand(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
