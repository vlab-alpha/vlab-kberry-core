package tools.vlab.smarthome.kberry.baos.messages.os;

import lombok.Getter;
import tools.vlab.smarthome.kberry.baos.ByteUtil;
import tools.vlab.smarthome.kberry.baos.messages.FT12Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetDatapointValue {

    public static class Request implements DataFramePayload {
        public static final int MAIN_SERVICE = 0xF0;   // 1 Byte, 0..255
        public static final int SUBSERVICE = 0x05;    // 1 Byte, 0..255
        private final Filter filter;
        private final int startDatapoint;        // 2 Byte, 0..65535
        private final int numberOfDataPoints;

        public Request(Filter filter, int startDatapoint, int numberOfDataPoints) {
            this.filter = filter;
            this.startDatapoint = startDatapoint;
            this.numberOfDataPoints = numberOfDataPoints;
        }

        public static Request all(int max) {
            return new Request(Filter.NONE, 0, max);
        }

        public static Request getAllValidDP(int max) {
            return new Request(Filter.ONLY_VALID_DP, 0, max);
        }

        public static Request getAllUpdatedDP(int max) {
            return new Request(Filter.ONLY_UPDATED_DP, 0, max);
        }

        public static Request getDP(DataPointId id) {
            return new Request(Filter.NONE, id.id(), 1);
        }

        @Override
        public byte[] toByteArray() {
            ByteBuffer buf = ByteBuffer.allocate(7);
            buf.order(ByteOrder.BIG_ENDIAN); // BAOS uses Big Endian
            buf.put((byte) MAIN_SERVICE);
            buf.put((byte) SUBSERVICE);
            buf.putShort((short) startDatapoint);
            buf.putShort((short) numberOfDataPoints);
            buf.put(filter.getByte());
            return buf.array();
        }

        @Override
        public int getId() {
            return this.startDatapoint;
        }

        @Override
        public String toHex() {
            return ByteUtil.toHex(this.toByteArray());
        }

        @Override
        public Service getService() {
            return Service.from(SUBSERVICE);
        }
    }


    @Getter
    public static class Response {

        private final int mainService;
        private final int subService;
        private final DataPointId startDatapoint;
        private final Error error;
        private final List<DataPoint> dataPoints;

        private Response(int mainService, int subService, DataPointId startDatapoint, Error error, List<DataPoint> dataPoints) {
            this.mainService = mainService;
            this.subService = subService;
            this.startDatapoint = startDatapoint;
            this.error = error;
            this.dataPoints = dataPoints;
        }

        public static Response frameData(FT12Frame.Data frameData) {
            byte[] payload = frameData.getPayload();
            int mainService = ByteUtil.uInt8(payload, 0);
            int subService = ByteUtil.uInt8(payload, 1);
            int startDataPoint = ByteUtil.uInt16(payload, 2);
            int numberOfItems = ByteUtil.uInt16(payload, 4);

            // Error
            if (numberOfItems <= 0) {
                int errorCode = ByteUtil.uInt8(payload, 6);
                return new GetDatapointValue.Response(
                        mainService, subService, DataPointId.id(startDataPoint), Error.withCode(errorCode), new ArrayList<>()
                );
            }

            // Success
            var dataPointList = new ArrayList<DataPoint>();
            for (int i = 0, itemIndex = 6; i < numberOfItems; i++) {
                int dpCode = ByteUtil.uInt16(payload, itemIndex);
                itemIndex += 2;
                byte stateByte = payload[itemIndex];
                itemIndex += 1;
                int dataLength = ByteUtil.uInt8(payload, itemIndex);
                itemIndex += 1;
                byte[] data = ByteUtil.copy(payload, itemIndex, dataLength);
                itemIndex += dataLength;
                dataPointList.add(new DataPoint(DataPointId.id(dpCode), data, State.withByte(stateByte)));
            }
            return new Response(mainService, subService, DataPointId.id(startDataPoint), Error.OK, dataPointList);
        }

        public boolean isValid() {
            return mainService == 0xF0 && subService == 0x85;
        }

        public boolean isSuccess() {
            return error == Error.OK;
        }

        /**
         * Check if all datapoints could be found on the object server
         *
         * @return true
         */
        public boolean foundInOSCache() {
            return dataPoints.stream().allMatch(DataPoint::isAvailable);
        }

        public boolean anyProgress() {
            return dataPoints.stream().anyMatch(DataPoint::isInProgress);
        }

        public Optional<DataPoint> getFirstDataPoint() {
            return dataPoints.stream().filter(DataPoint::isAvailable).findFirst();
        }

    }

    public static class Indicator extends GetDatapointValue.Response {

        private Indicator(int mainService, int subService, DataPointId startDatapoint, Error error, List<DataPoint> dataPoints) {
            super(mainService, subService, startDatapoint, error, dataPoints);
        }

        public static Indicator frameData(FT12Frame.Data frameData) {
            var response = GetDatapointValue.Response.frameData(frameData);
            return new Indicator(response.mainService, response.subService, response.startDatapoint, response.error, response.dataPoints);
        }

        @Override
        public boolean isValid() {
            return getMainService() == 0xF0 && getSubService() == 0xC1;
        }

    }

    public enum Filter {
        NONE(0x00),
        ONLY_VALID_DP(0x01),
        ONLY_UPDATED_DP(0x02);

        private final int code;

        Filter(int code) {
            this.code = code;
        }

        public byte getByte() {
            return (byte) code;
        }
    }

    public enum State {
        AVAILABLE,
        VALUE_UPDATED,
        READ_REQUEST_REQUIRED,
        OK,
        ERROR,
        IN_PROGRESS,
        START_PROGRESS;

        public static List<State> withByte(byte stateByte) {
            List<State> states = new ArrayList<>();
            int t = stateByte & 0x03; // Bits 0-1
            switch (t) {
                case 0 -> states.add(OK);
                case 1 -> states.add(ERROR);
                case 2 -> states.add(IN_PROGRESS);
                case 3 -> states.add(START_PROGRESS);
            }
            // Bit 2: Read/Write Request
            if ((stateByte & 0x04) != 0) states.add(READ_REQUEST_REQUIRED);
            // Bit 3: Update Flag
            if ((stateByte & 0x08) != 0) states.add(VALUE_UPDATED);
            // Bit 4: Valid Flag
            if ((stateByte & 0x10) != 0) states.add(AVAILABLE);
            return states;
        }
    }

}