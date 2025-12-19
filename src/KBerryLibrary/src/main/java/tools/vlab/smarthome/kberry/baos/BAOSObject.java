package tools.vlab.smarthome.kberry.baos;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPointId;
import tools.vlab.smarthome.kberry.devices.Command;

public record BAOSObject(DataPointId dataPointId, String type, Command command, PositionPath positionPath, KnxDatapointType datapointType) {

    public String getName() {
        return String.format("%s [%s] %s", type(), command.getId(), positionPath.getId());
    }

}
