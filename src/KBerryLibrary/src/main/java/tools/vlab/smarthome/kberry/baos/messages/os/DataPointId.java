package tools.vlab.smarthome.kberry.baos.messages.os;

public record DataPointId(int id) {

    public static DataPointId id(int andIncrement) {
        return new DataPointId(andIncrement);
    }

//    public static DataPointId from(FT12Frame.Data data) {
//        int id = ((data.getPayload()[2] & 0xFF) << 8) |
//                (data.getPayload()[3] & 0xFF);
//        return new DataPointId(id);
//    }

    public boolean isSame(int objectId) {
        return id() == objectId;
    }
}
