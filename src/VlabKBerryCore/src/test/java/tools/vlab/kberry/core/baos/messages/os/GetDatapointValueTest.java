package tools.vlab.kberry.core.baos.messages.os;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetDatapointValueTest {

    @Test
    void testRequestToByteArray() {
        GetDatapointValue.Request req = new GetDatapointValue.Request(GetDatapointValue.Filter.ONLY_VALID_DP, 0x1234, 2);
        byte[] bytes = req.toByteArray();
        assertEquals(7, bytes.length);
        assertEquals((byte) 0xF0, bytes[0]);
        assertEquals((byte) 0x05, bytes[1]);
        assertEquals(0x12, bytes[2] & 0xFF);
        assertEquals(0x34, bytes[3] & 0xFF);
        assertEquals(0x00, bytes[4]); // Number of datapoints high byte
        assertEquals(0x02, bytes[5]); // Number of datapoints low byte
        assertEquals(0x01, bytes[6]); // Filter = ONLY_VALID_DP
    }

//    @Test
//    void testResponseParsing() {
//        byte[] payload = new byte[]{
//                (byte) 0xF0, (byte) 0x85, // main/sub service
//                0x00, 0x01, // start datapoint 1
//                0x00, 0x01, // number of items = 1
//                0x00, 0x01, // datapoint ID = 1
//                0x10,       // state byte = AVAILABLE
//                0x01,       // data length = 1
//                0x55        // payload data
//        };
//        FT12Frame.Data frame = FT12Frame.Data.of(payload);
//        GetDatapointValue.Response resp = GetDatapointValue.Response.frameData(frame);
//
//        assertEquals(0xF0, resp.getMainService());
//        assertEquals(0x85, resp.getSubService());
//        assertTrue(resp.isValid());
//        assertTrue(resp.isSuccess());
//        assertEquals(1, resp.getDataPoints().size());
//
//        DataPoint dp = resp.getDataPoints().get(0);
//        assertEquals(1, dp.id().id());
//        assertEquals(1, dp.payload().length);
//        assertEquals(0x55, dp.payload()[0]);
//        assertTrue(dp.states().contains(GetDatapointValue.State.AVAILABLE));
//    }

//    @Test
//    void testIndicatorParsing() {
//        byte[] payload = new byte[]{
//                (byte) 0xF0, (byte) 0xC1, // main/sub service for indicator
//                0x00, 0x01, // start datapoint
//                0x00, 0x01, // number of items
//                0x00, 0x01, // datapoint ID
//                0x18,       // state byte: VALUE_UPDATED + READ_REQUEST_REQUIRED
//                0x01,       // data length
//                0x33        // payload
//        };
//        FT12Frame.Data frame = FT12Frame.Data.request(payload);
//        GetDatapointValue.Indicator indicator = GetDatapointValue.Indicator.frameData(frame);
//
//        assertEquals(0xF0, indicator.getMainService());
//        assertEquals(0xC1, indicator.getSubService());
//        assertTrue(indicator.isValid());
//        assertEquals(1, indicator.getDataPoints().size());
//
//        DataPoint dp = indicator.getDataPoints().get(0);
//        assertEquals(1, dp.id().id());
//        assertEquals(0x33, dp.payload()[0]);
//        List<GetDatapointValue.State> states = dp.states();
//        assertTrue(states.contains(GetDatapointValue.State.VALUE_UPDATED));
//        assertTrue(states.contains(GetDatapointValue.State.READ_REQUEST_REQUIRED));
//    }

    @Test
    void testStateWithByteAllFlags() {
        byte stateByte = 0b00011111; // All flags set: bits 0-4
        List<GetDatapointValue.State> states = GetDatapointValue.State.withByte(stateByte);
        assertTrue(states.contains(GetDatapointValue.State.OK) ||
                states.contains(GetDatapointValue.State.ERROR) ||
                states.contains(GetDatapointValue.State.IN_PROGRESS) ||
                states.contains(GetDatapointValue.State.START_PROGRESS));
        assertTrue(states.contains(GetDatapointValue.State.READ_REQUEST_REQUIRED));
        assertTrue(states.contains(GetDatapointValue.State.VALUE_UPDATED));
        assertTrue(states.contains(GetDatapointValue.State.AVAILABLE));
    }

//    @Test
//    void testGetFirstDataPointOptional() {
//        byte[] payload = new byte[]{
//                (byte) 0xF0, (byte) 0x85, // main/sub service
//                0x00, 0x01, // start datapoint
//                0x00, 0x01, // number of items
//                0x00, 0x01, // datapoint ID
//                0x10,       // state byte = AVAILABLE
//                0x01,       // data length
//                0x55        // payload data
//        };
//        FT12Frame.Data frame = FT12Frame.Data.of(payload);
//        GetDatapointValue.Response resp = GetDatapointValue.Response.frameData(frame);
//
//        Optional<DataPoint> dp = resp.getFirstDataPoint();
//        assertTrue(dp.isPresent());
//        assertEquals(0x55, dp.get().payload()[0]);
//    }
}