package tools.vlab.smarthome.kberry.baos.messages;

import lombok.Getter;
import tools.vlab.smarthome.kberry.baos.ByteUtil;

public abstract class Frame {

    @Getter
    protected final byte[] payload;

    protected Frame(byte[] payload) {
        this.payload = payload;
    }

    abstract byte[] toByteArray();

    public String toHex() {
        return ByteUtil.toHex(this.payload);
    }

}
