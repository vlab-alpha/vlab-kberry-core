package tools.vlab.smarthome.kberry.baos;

import tools.vlab.smarthome.kberry.devices.RGB;

public interface KnxWriter {

    void resendAll();

    void writeValue(int objectId, float value);

    void writeValue(int objectId, RGB value);

    void writeValue(int objectId, boolean value);

    void writeValue(int objectId, int value);

    void readRequest(int objectId);

}
