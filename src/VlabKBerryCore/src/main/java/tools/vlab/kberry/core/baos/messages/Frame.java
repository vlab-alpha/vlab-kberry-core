package tools.vlab.kberry.core.baos.messages;

import lombok.Getter;
import tools.vlab.kberry.core.baos.ByteUtil;

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
