package tools.vlab.smarthome.kberry.baos.messages.os;

public interface DataFramePayload extends OSPayload {

    byte[] toByteArray();

    int getId();

    Service getService();

}
