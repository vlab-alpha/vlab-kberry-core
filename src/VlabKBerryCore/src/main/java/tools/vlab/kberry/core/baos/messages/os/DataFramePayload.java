package tools.vlab.kberry.core.baos.messages.os;

public interface DataFramePayload extends OSPayload {

    byte[] toByteArray();

    int getId();

    Service getService();

}
