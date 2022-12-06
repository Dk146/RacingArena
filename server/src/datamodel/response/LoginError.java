package datamodel.response;

import datamodel.DataModel;
import java.nio.ByteBuffer;

public class LoginError extends DataModel {
    private int cmd;
    private int eventFlag;

    public LoginError(int _cmd, int _eventFlag) {
        this.cmd = _cmd;
        this.eventFlag = _eventFlag;
    }

    @Override
    public byte[] pack() {
        int capacity = Integer.BYTES // cmd
                + Integer.BYTES; // storing eventFlag

        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        byteBuffer.putInt(this.cmd);
        byteBuffer.putInt(this.eventFlag);

        // Return a byte[] array
        return byteBuffer.array();
    }

    @Override
    public void unpack(byte[] bytes) {}
}
