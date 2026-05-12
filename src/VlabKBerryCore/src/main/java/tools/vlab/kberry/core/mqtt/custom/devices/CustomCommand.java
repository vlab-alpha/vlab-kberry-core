package tools.vlab.kberry.core.mqtt.custom.devices;

import lombok.Getter;
import tools.vlab.kberry.core.knx.devices.Communication;

import java.util.stream.Stream;

public enum CustomCommand {

    STATUS(Communication.READWRITE, CustomDataPointType.BOOL, 0),
    GET_STATUS(Communication.WRITE, CustomDataPointType.BOOL, 1),
    SPEED(Communication.READWRITE, CustomDataPointType.INT, 2),
    GET_SPEED(Communication.WRITE, CustomDataPointType.INT, 3),
    COLOR(Communication.READWRITE, CustomDataPointType.RGB, 4),
    GET_COLOR(Communication.READWRITE, CustomDataPointType.RGB, 5),
    GET_ENERGY(Communication.WRITE, CustomDataPointType.INT, 6),
    NOTIFY(Communication.WRITE, CustomDataPointType.BOOL, 7)
    ;

    @Getter
    private final Communication communication;
    @Getter
    private final CustomDataPointType datapoint;
    @Getter
    private final int id;

    CustomCommand(Communication communication, CustomDataPointType datapoint, int id) {
        this.communication = communication;
        this.datapoint = datapoint;
        this.id = id;
    }

    public static CustomCommand from(int id) {
        return Stream.of(values()).filter(c -> c.getId() == id).findFirst().orElse(null);
    }





}
