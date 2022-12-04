package clientdatamodel.send;

import clientdatamodel.ClientDataModel;

import java.nio.ByteBuffer;

public class CSenLogin extends ClientDataModel {
    private int cmd;
    private String username;
    public CSenLogin(int _cmd, String _username) {
        this.cmd = _cmd;
        this.username = _username;
    }
    
    @Override
    public byte[] pack() {
        // Allocate a bytebuffer
        int capacity = Integer.BYTES // cmd String
                + Integer.BYTES // storing size of username String
                + Integer.BYTES // storing size of password String
                + this.username.length();
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        // Pour in data
        // Always put cmd first
        byteBuffer.putInt(this.cmd);
        // Then put size of each object in sequence
        byteBuffer.putInt(this.username.length());
        // Put the objects following the sequence
        byteBuffer.put(this.username.getBytes());

        // Return a byte[] array
        return byteBuffer.array();
    }
}
