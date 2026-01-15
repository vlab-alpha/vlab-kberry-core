package tools.vlab.kberry.core.baos;

import tools.vlab.kberry.core.baos.messages.os.DataPoint;

public record DataPointPriority(DataPoint dataPoint, boolean priority) {

    public static DataPointPriority prio(DataPoint dataPoint) {
        return new DataPointPriority(dataPoint, true);
    }

    public static DataPointPriority normal(DataPoint dataPoint) {
        return new DataPointPriority(dataPoint, false);
    }
}
