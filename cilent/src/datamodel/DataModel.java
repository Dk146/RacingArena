package datamodel;

import java.nio.ByteBuffer;

public class DataModel {
    private ByteBuffer byteBuffer;

    public DataModel() { this.byteBuffer = null; }

    public byte[] pack() { return this.byteBuffer.array(); };
    public void unpack(byte[] bytes) {};
}
