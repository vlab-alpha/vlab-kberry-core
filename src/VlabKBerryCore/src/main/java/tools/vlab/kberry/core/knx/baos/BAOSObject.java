package tools.vlab.kberry.core.knx.baos;

import tools.vlab.kberry.core.PositionPath;
import tools.vlab.kberry.core.knx.baos.messages.os.DataPointId;
import tools.vlab.kberry.core.knx.devices.KnxCommand;

public record BAOSObject(DataPointId dataPointId, String type, KnxCommand command, PositionPath positionPath, KnxDatapointType datapointType) {

    public String getName() {
        return String.format("%s [%s] %s", type(), command.getId(), positionPath.getId());
    }

}
