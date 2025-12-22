package tools.vlab.kberry.core.baos.messages.os;

import tools.vlab.kberry.core.baos.ByteUtil;
import tools.vlab.kberry.core.baos.messages.FT12Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetDatapointValue {

    public static class Request implements DataFramePayload {
        public static final int MAIN_SERVICE = 0xF0;   // 1 Byte, 0..255
        public static final int SUBSERVICE = 0x06;    // 1 Byte, 0..255
        private final int startDatapointId;        // 2 Byte, 0..65535
        private final int numberOfDataPoints;
        private final DataPointCommand command;

        public Request(int startDatapointId, int numberOfDataPoints, DataPointCommand command) {
            this.startDatapointId = startDatapointId;
            this.numberOfDataPoints = numberOfDataPoints;
            this.command = command;
        }

        public static Request updateCache(DataPointId dpIds) {
            var commands = new DataPointCommand(dpIds, Command.READ_VALUE_ON_BUS, new byte[0]);
            var startPointId = dpIds.id();
            var numberOfDataPoints = 1;
            return new Request(startPointId, numberOfDataPoints, commands);
        }

        public static Request setCacheAndBus(DataPoint dataPoint) {
            var commands = new DataPointCommand(
                    dataPoint.id(),
                    Command.SET_CACHE_AND_SEND_BUS,
                    dataPoint.payload()
            );
            var startPointId = dataPoint.id().id();
            var numberOfDataPoints = 1;
            return new Request(startPointId, numberOfDataPoints, commands);
        }

        public static Request clearDPStatus(DataPointId dpIds) {
            var commands = new DataPointCommand(dpIds, Command.CLEAR_DP_STATUS, new byte[0]);
            var startPointId = dpIds.id();
            var numberOfDataPoints = 1;
            return new Request(startPointId, numberOfDataPoints, commands);
        }

        @Override
        public byte[] toByteArray() {
            ByteBuffer buf = ByteBuffer.allocate(10 + command.payload.length);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.put((byte) MAIN_SERVICE);
            buf.put((byte) SUBSERVICE);
            buf.putShort((short) startDatapointId);
            buf.putShort((short) numberOfDataPoints);
            // Command
            buf.putShort((short) command.id.id());
            buf.put(command.cmd.toByte());
            buf.put((byte) command.payload.length);
            buf.put(command.payload);
            return buf.array();
        }

        @Override
        public int getId() {
            return this.startDatapointId;
        }

        @Override
        public String toHex() {
            return ByteUtil.toHex(toByteArray());
        }

        @Override
        public Service getService() {
            return Service.from(SUBSERVICE);
        }
    }

    public record Response(int mainService, int subService, DataPointId startDatapoint, Error error) {

        public static Response frameData(FT12Frame.Data frameData) {
            byte[] payload = frameData.getPayload();
            ByteBuffer buf = ByteBuffer.wrap(payload);
            buf.order(ByteOrder.BIG_ENDIAN);
            int mainService = ByteUtil.uInt8(payload, 0);
            int subService = ByteUtil.uInt8(payload, 1);
            int startDataPoint = ByteUtil.uInt16(payload, 2);
            // Ignore numberOfItems
            int errorCode = ByteUtil.uInt8(payload, 6);
            return new SetDatapointValue.Response(mainService, subService, DataPointId.id(startDataPoint), Error.withCode(errorCode));
        }

        public boolean isFailed() {
            return error != Error.OK;
        }

    }

    public enum Command {
        SET_CACHE_VALUE(0x00),
        SEND_TO_BUS(0x02),
        SET_CACHE_AND_SEND_BUS(0x03),
        READ_VALUE_ON_BUS(0x04),
        CLEAR_DP_STATUS(0x05);

        private final int code;

        Command(int code) {
            this.code = code;
        }

        public byte toByte() {
            return (byte) code;
        }
    }

    public record DataPointCommand(DataPointId id, Command cmd, byte[] payload) {
    }
}