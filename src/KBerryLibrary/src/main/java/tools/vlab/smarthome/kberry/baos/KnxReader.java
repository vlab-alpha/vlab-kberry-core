package tools.vlab.smarthome.kberry.baos;

import java.util.function.Consumer;

public interface KnxReader {
    void onFrame(int objectId, Consumer<byte[]> listener);
}
