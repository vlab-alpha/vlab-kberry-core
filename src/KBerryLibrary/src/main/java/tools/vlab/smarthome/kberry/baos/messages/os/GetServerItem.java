package tools.vlab.smarthome.kberry.baos.messages.os;

import lombok.Getter;
import tools.vlab.smarthome.kberry.baos.ByteUtil;
import tools.vlab.smarthome.kberry.baos.messages.FT12Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * BAOS ServerItem Request / Response
 * <p>
 * Protokoll:
 * - Client sendet Request, z.B. GetServerItem.Req
 * - Server antwortet mit Response, z.B. GetServerItem.Res
 * <p>
 * Felder:
 * - mainService: Hauptservicecode (z.B. 0xF0 für Datapoint Service)
 * - subService: Subservicecode (z.B. 0x01 für GetServerItem Request)
 * - startItem: ID des ersten Elements, das angefragt wird
 * - numberOfItems: Anzahl der Elemente, die zurückgegeben werden sollen
 */
public class GetServerItem {


    public static class Indicator extends GetServerItem.Response {

        public Indicator(int mainService, int subService, ServerItemId startItem, Error error, List<ServerItem> items) {
            super(mainService, subService, startItem, error, items);
        }

        public static Indicator frameData(FT12Frame.Data data) {
            var response = GetServerItem.Response.frameData(data);
            return new Indicator(response.getMainService(), response.getSubService(), response.getStartItem(), response.getError(), response.getItems());
        }

    }

    /**
     * Request: GetServerItem.Req
     */
    public static class Request implements DataFramePayload {
        public static final int MAIN_SERVICE = 0xF0;   // 1 Byte, 0..255
        public static final int SUBSERVICE = 0x01;    // 1 Byte, 0..255
        private int startItem = 0;        // 2 Byte, 0..65535
        @Getter
        private int numberOfItems = 1;    // 2 Byte, 0..65535

        /**
         * Builder method to create a new Request instance
         */
        public static Request create() {
            return new Request();
        }


        public Request serverItem(ServerItemId id) {
            this.startItem = id.getId();
            this.numberOfItems = id.numberOfItems();
            return this;
        }

        @Override
        public byte[] toByteArray() {
            ByteBuffer buf = ByteBuffer.allocate(6);
            buf.order(ByteOrder.BIG_ENDIAN); // BAOS uses Big Endian
            buf.put((byte) MAIN_SERVICE);
            buf.put((byte) SUBSERVICE);
            buf.putShort((short) startItem);
            buf.putShort((short) numberOfItems);
            return buf.array();
        }

        @Override
        public int getId() {
            return this.startItem;
        }

        @Override
        public Service getService() {
            return Service.from(SUBSERVICE);
        }

        @Override
        public String toHex() {
            return ByteUtil.toHex(toByteArray());
        }

    }

    @Getter
    public static class Response {
        private final int mainService;
        private final int subService;
        private final ServerItemId startItem;
        private final Error error;
        private final List<ServerItem> items;

        private Response(int mainService, int subService, ServerItemId startItem, Error error,
                        List<ServerItem> items) {
            this.mainService = mainService;
            this.subService = subService;
            this.startItem = startItem;
            this.error = error;
            this.items = items;
        }

        public boolean isSuccess() {
            return getError() == Error.OK;
        }

        public static Response frameData(FT12Frame.Data frameData) {
            byte[] payload = frameData.getPayload();
            int mainService = ByteUtil.uInt8(payload, 0);
            int subService = ByteUtil.uInt8(payload, 1);
            int startItem = ByteUtil.uInt16(payload, 2);
            int numberOfItems = ByteUtil.uInt16(payload, 4);

            // Error
            if (numberOfItems <= 0) {
                int errorCode = ByteUtil.uInt8(payload, 6);
                return new Response(
                        mainService, subService, ServerItemId.withCode(startItem), Error.withCode(errorCode), new ArrayList<>()
                );
            }

            // Success
            var items = new ArrayList<ServerItem>();
            for (int i = 0, itemIndex = 6; i < numberOfItems; i++) {
                int serverItemCode = ByteUtil.uInt16(payload, itemIndex);
                itemIndex += 2;
                int dataLength = ByteUtil.uInt8(payload, itemIndex);
                itemIndex += 1;
                byte[] data = ByteUtil.copy(payload, itemIndex, dataLength);
                itemIndex += dataLength;
                items.add(new ServerItem(ServerItemId.withCode(serverItemCode), data));
            }
            return new Response(mainService, subService, ServerItemId.withCode(startItem), Error.OK, items);
        }



        public record ServerItem(ServerItemId id, byte[] data) {

            public String toHex() {
                return ByteUtil.toHex(data);
            }

            public int intData() {
                return ByteUtil.uInt8(data, 0);
            }

            public boolean boolData() {
                return ByteUtil.bool(data, 0);
            }

            public String stringData() {
                return new String(data);
            }
        }
    }
}