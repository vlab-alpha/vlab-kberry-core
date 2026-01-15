package tools.vlab.kberry.core.baos.messages.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.vlab.kberry.core.baos.ByteUtil;
import tools.vlab.kberry.core.devices.HeaterMode;
import tools.vlab.kberry.core.devices.RGB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DataPoint(DataPointId id, byte[] payload, List<GetDatapointValue.State> states) {

    private static final Logger Log = LoggerFactory.getLogger(DataPoint.class);

    public static DataPoint hvac(DataPointId id, HeaterMode mode) {
        return new DataPoint(id, ByteUtil.mode(mode), new ArrayList<>());
    }

    public static DataPoint uInt8(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.uInt8(value), new ArrayList<>());
    }

    public static DataPoint int8(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.int8(value), new ArrayList<>());
    }

    public static DataPoint sInt8(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.sInt8(value), new ArrayList<>());
    }

    public static DataPoint sint16(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.sint16(value), new ArrayList<>());
    }

    public static DataPoint sint32(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.sint32(value), new ArrayList<>());
    }

    public static DataPoint uInt16(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.uint16(value), new ArrayList<>());
    }

    public static DataPoint uint32(DataPointId id, int value) {
        return new DataPoint(id, ByteUtil.uint32(value), new ArrayList<>());
    }

    public static DataPoint float9(DataPointId id, float value) {
        return new DataPoint(id, ByteUtil.float9(value), new ArrayList<>());
    }

    public static DataPoint float32(DataPointId id, float value) {
        return new DataPoint(id, ByteUtil.float32(value), new ArrayList<>());
    }

    public static DataPoint rgb(DataPointId id, RGB value) {
        return new DataPoint(id, ByteUtil.rgb(value), new ArrayList<>());
    }

    public static DataPoint bool(DataPointId id, boolean value) {
        return new DataPoint(id, ByteUtil.bool(value), new ArrayList<>());
    }

    public boolean isValueUpdated() {
        return states.contains(GetDatapointValue.State.VALUE_UPDATED);
    }

    public boolean isReadRequestRequired() {
        return states.contains(GetDatapointValue.State.READ_REQUEST_REQUIRED);
    }

    public boolean isWriteRequestRequired() {
        return !states.contains(GetDatapointValue.State.READ_REQUEST_REQUIRED);
    }

    /**
     * Determines whether the current data point is available in the object server.
     *
     * @return true if the data point is available, false otherwise
     */
    public boolean isAvailable() {
        return states.contains(GetDatapointValue.State.AVAILABLE);
    }

    public boolean isInProgress() {
        return states.contains(GetDatapointValue.State.IN_PROGRESS) || states.contains(GetDatapointValue.State.START_PROGRESS);
    }

    public boolean isOk() {
        return states.contains(GetDatapointValue.State.OK) && !isError();
    }

    public boolean isError() {
        return states.contains(GetDatapointValue.State.ERROR);
    }

    public Optional<Integer> getUInt16() {
        try {
            return Optional.of(ByteUtil.uInt16(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype uInt16 [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
            return Optional.empty();
        }
    }

    public Optional<Integer> getUInt8() {
        try {
            return Optional.of(ByteUtil.uInt8(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype uIn8 [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
            return Optional.empty();
        }
    }

    public Optional<Float> getFloat32() {
        try {
            return Optional.of(ByteUtil.toFloat32(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype float32 [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
            return Optional.empty();
        }
    }

    public Optional<Float> getFloat9() {
        try {
            return Optional.of(ByteUtil.toFloat9(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype float [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
            return Optional.empty();
        }
    }

    public Optional<RGB> getRGB() {
        try {
            return Optional.of(ByteUtil.rgb(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype RGB [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
        }
        return Optional.empty();
    }

    public Optional<Boolean> getBoolean() {
        try {
            return Optional.of(ByteUtil.bool(payload));
        } catch (Exception e) {
            Log.error("Invalid datatype Bool [Id:{}; Length:{}; Hex:{}]", id().id(), payload.length, ByteUtil.toHex(payload));
        }
        return Optional.empty();
    }

    public String toHex() {
        return  ByteUtil.toHex(payload);
    }

    public String toString() {
        return String.format("%s = %s", id().id(), toHex());
    }
}
