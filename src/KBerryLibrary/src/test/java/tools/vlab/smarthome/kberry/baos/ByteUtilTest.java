package tools.vlab.smarthome.kberry.baos;

import org.junit.jupiter.api.Test;
import tools.vlab.smarthome.kberry.devices.RGB;

import static org.junit.jupiter.api.Assertions.*;

class ByteUtilTest {

    @Test
    void testUInt8() {
        byte[] b = ByteUtil.uInt8(255);
        assertEquals(1, b.length);
        assertEquals((byte) 0xFF, b[0]);
        assertEquals(255, ByteUtil.uInt8(b));
    }

    @Test
    void testInt8() {
        byte[] b = ByteUtil.int8(-128);
        assertEquals((byte) -128, b[0]);
    }

    @Test
    void testSInt8() {
        byte[] b = ByteUtil.sInt8(-1);
        assertEquals((byte) -1, b[0]);
    }

    @Test
    void testUInt16() {
        int value = 0xABCD;
        byte[] b = ByteUtil.uint16(value);
        assertEquals(value, ByteUtil.uInt16(b));
    }

    @Test
    void testUInt32() {
        int value = 0xDEADBEEF;
        byte[] b = ByteUtil.uint32(value);
        int result = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
        assertEquals(value, result);
    }

    @Test
    void testFloat32() {
        float value = 123.456f;
        byte[] b = ByteUtil.float32(value);
        assertEquals(value, ByteUtil.toFloat32(b), 0.001f);
    }

    @Test
    void testFloat9() {
        float value = 12.34f;
        byte[] b = ByteUtil.float9(value);
        float decoded = ByteUtil.toFloat9(b);
        assertEquals(value, decoded, 0.1f);
    }

    @Test
    void testRGB() {
        RGB rgb = new RGB(10, 20, 30);
        byte[] b = ByteUtil.rgb(rgb);
        assertArrayEquals(new byte[]{10, 20, 30}, b);

        RGB decoded = ByteUtil.rgb(b);
        assertEquals(10, decoded.red());
        assertEquals(20, decoded.green());
        assertEquals(30, decoded.blue());
    }

    @Test
    void testBool() {
        assertArrayEquals(new byte[]{1}, ByteUtil.bool(true));
        assertArrayEquals(new byte[]{0}, ByteUtil.bool(false));

        assertTrue(ByteUtil.bool(new byte[]{1}));
        assertFalse(ByteUtil.bool(new byte[]{0}));
    }

    @Test
    void testBit() {
        byte b = 0b00001010;
        assertTrue(ByteUtil.bit(b, 3));
        assertTrue(ByteUtil.bit(b, 1));
        assertFalse(ByteUtil.bit(b, 0));
        assertFalse(ByteUtil.bit(b, 2));
    }

    @Test
    void testConcatByteArrays() {
        byte[] a = {0x01, 0x02};
        byte[] b = {0x03};
        byte[] c = {0x04, 0x05};
        byte[] result = ByteUtil.concatByteArrays(a, b, c);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, result);
    }

    @Test
    void testCopy() {
        byte[] arr = {0, 1, 2, 3, 4};
        byte[] copy = ByteUtil.copy(arr, 1, 3);
        assertArrayEquals(new byte[]{1, 2, 3}, copy);
    }

    @Test
    void testToHex() {
        byte[] arr = {(byte) 0xAB, 0x0F};
        assertEquals("AB 0F", ByteUtil.toHex(arr));
        assertEquals("0xAB", ByteUtil.toHex((byte) 0xAB));
    }

    @Test
    void testUInt8Edge() {
        byte[] min = ByteUtil.uInt8(0);
        byte[] max = ByteUtil.uInt8(255);
        assertEquals(0, ByteUtil.uInt8(min));
        assertEquals(255, ByteUtil.uInt8(max));
    }

    @Test
    void testInt8Edge() {
        byte[] min = ByteUtil.int8(-128);
        byte[] max = ByteUtil.int8(127);
        assertEquals(-128, min[0]);
        assertEquals(127, max[0]);
    }

    @Test
    void testSInt8Edge() {
        byte[] min = ByteUtil.sInt8(-128);
        byte[] max = ByteUtil.sInt8(127);
        assertEquals(-128, min[0]);
        assertEquals(127, max[0]);
    }

    @Test
    void testUInt16Edge() {
        byte[] min = ByteUtil.uint16(0);
        byte[] max = ByteUtil.uint16(65535);
        assertEquals(0, ByteUtil.uInt16(min));
        assertEquals(65535, ByteUtil.uInt16(max));
    }

    @Test
    void testUInt32Edge() {
        byte[] min = ByteUtil.uint32(0);
        byte[] max = ByteUtil.uint32(0xFFFFFFFF);
        int minVal = ((min[0] & 0xFF) << 24) | ((min[1] & 0xFF) << 16) | ((min[2] & 0xFF) << 8) | (min[3] & 0xFF);
        int maxVal = ((max[0] & 0xFF) << 24) | ((max[1] & 0xFF) << 16) | ((max[2] & 0xFF) << 8) | (max[3] & 0xFF);
        assertEquals(0, minVal);
        assertEquals(0xFFFFFFFF, maxVal);
    }

    @Test
    void testFloat32Edge() {
        float[] testValues = {Float.MIN_VALUE, Float.MAX_VALUE, 0f, -0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN};
        for (float f : testValues) {
            byte[] b = ByteUtil.float32(f);
            float decoded = ByteUtil.toFloat32(b);
            if (Float.isNaN(f)) {
                assertTrue(Float.isNaN(decoded));
            } else {
                assertEquals(f, decoded);
            }
        }
    }

    @Test
    void testFloat9Edge() {
        float[] testValues = {-2047f, 0f, 2047f};
        for (float f : testValues) {
            byte[] b = ByteUtil.float9(f);
            float decoded = ByteUtil.toFloat9(b);
            assertEquals(Math.signum(f) * Math.abs(decoded), Math.signum(f) * Math.abs(f), 1f); // grobe Toleranz
        }
    }

    @Test
    void testRGBEdge() {
        RGB rgbMin = new RGB(0, 0, 0);
        RGB rgbMax = new RGB(255, 255, 255);
        byte[] bMin = ByteUtil.rgb(rgbMin);
        byte[] bMax = ByteUtil.rgb(rgbMax);
        RGB decodedMin = ByteUtil.rgb(bMin);
        RGB decodedMax = ByteUtil.rgb(bMax);
        assertEquals(0, decodedMin.red());
        assertEquals(255, decodedMax.red());
    }

    @Test
    void testBoolEdge() {
        assertTrue(ByteUtil.bool(new byte[]{1}));
        assertFalse(ByteUtil.bool(new byte[]{0}));
        byte[] bTrue = ByteUtil.bool(true);
        byte[] bFalse = ByteUtil.bool(false);
        assertEquals(1, bTrue[0]);
        assertEquals(0, bFalse[0]);
    }

    @Test
    void testBitEdge() {
        byte b = (byte) 0b11111111;
        for (int i = 0; i < 8; i++) {
            assertTrue(ByteUtil.bit(b, i));
        }
        b = 0;
        for (int i = 0; i < 8; i++) {
            assertFalse(ByteUtil.bit(b, i));
        }
    }

    @Test
    void testConcatCopyEdge() {
        byte[] a = {};
        byte[] b = {1};
        byte[] c = {2, 3};
        byte[] result = ByteUtil.concatByteArrays(a, b, c);
        assertArrayEquals(new byte[]{1, 2, 3}, result);

        byte[] copy = ByteUtil.copy(result, 1, 2);
        assertArrayEquals(new byte[]{2, 3}, copy);
    }

    @Test
    void testToHexEdge() {
        byte[] arr = {};
        assertEquals("", ByteUtil.toHex(arr));

        byte[] arr2 = {(byte) 0x00, (byte) 0xFF};
        assertEquals("00 FF", ByteUtil.toHex(arr2));
    }
}