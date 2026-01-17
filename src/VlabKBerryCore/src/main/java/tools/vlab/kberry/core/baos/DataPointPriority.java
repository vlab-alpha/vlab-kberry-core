package tools.vlab.kberry.core.baos;

import tools.vlab.kberry.core.baos.messages.os.DataPoint;

public record DataPointPriority(DataPoint dataPoint, boolean priority, int retry) {

    public static DataPointPriority prio(DataPoint dataPoint) {
        return new DataPointPriority(dataPoint, true, 0);
    }

    public static DataPointPriority normal(DataPoint dataPoint) {
        return new DataPointPriority(dataPoint, false, 0);
    }

    public static DataPointPriority retry(DataPoint dataPoint, int retry) {
        return new DataPointPriority(dataPoint, false, retry + 1);
    }

    public String toString() {
        return dataPoint().toString();
    }
}
