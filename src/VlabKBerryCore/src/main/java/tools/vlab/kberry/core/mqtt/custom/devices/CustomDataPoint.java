package tools.vlab.kberry.core.mqtt.custom.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.RGB;
import tools.vlab.kberry.core.knx.baos.ByteUtil;

import java.nio.ByteBuffer;
import java.util.Optional;

public record CustomDataPoint(byte[] payload) {

    private static final Logger Log = LoggerFactory.getLogger(CustomDataPoint.class);

    public static CustomDataPoint from(boolean value) {
        return new CustomDataPoint(ByteUtil.bool(value));
    }

    public static CustomDataPoint from(int value) {
        return new CustomDataPoint(ByteUtil.int8(value));
    }

    public static CustomDataPoint from(RGB value) {
        return new CustomDataPoint(ByteUtil.rgb(value));
    }

    public Optional<Boolean> getBoolean() {
        try {
            return Optional.of(payload[0] == 1);
        } catch (Exception e) {
            Log.error("Invalid Datatype Int", e);
            return Optional.empty();
        }
    }

    public Optional<RGB> getRGB() {
        try {
            return Optional.of(ByteUtil.rgb(payload));
        } catch (Exception e) {
            Log.error("Invalid Datatype RGB", e);
            return Optional.empty();
        }
    }

    public Optional<Integer> getInt() {
        try {
            return Optional.of(ByteBuffer.wrap(payload).getInt());
        } catch (Exception e) {
            Log.error("Invalid Datatype Int", e);
            return Optional.empty();
        }
    }

    public static Integer toInt(byte[] bytes) {
        return ByteUtil.uInt8(bytes);
    }



    public static RGB toRGB(byte[] bytes) {
        return ByteUtil.rgb(bytes);
    }

    public static Boolean toBool(byte[] bytes) {
        return ByteUtil.bool(bytes);
    }
}
