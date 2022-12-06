package datamodel.request;

import datamodel.DataModel;

import java.nio.ByteBuffer;

public class Account extends DataModel {
    private String username;

    public Account() {
        this.username = null;
    }

    @Override
    public void unpack(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        int lUsername = byteBuffer.getInt();

        byte[] bUsername = new byte[lUsername];

        byteBuffer.get(bUsername);

        this.username = new String(bUsername);
    }

    public String getUsername() {
        return this.username;
    }
}
