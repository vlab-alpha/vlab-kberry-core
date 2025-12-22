package tools.vlab.kberry.core.baos;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.baos.messages.os.DataPointId;
import tools.vlab.kberry.core.devices.Command;

public record BAOSObject(DataPointId dataPointId, String type, Command command, PositionPath positionPath, KnxDatapointType datapointType) {

    public String getName() {
        return String.format("%s [%s] %s", type(), command.getId(), positionPath.getId());
    }

}
