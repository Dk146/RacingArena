package serverdatamodel.request;

import serverdatamodel.ServerDataModel;

import java.nio.ByteBuffer;

public class SReqAccount extends ServerDataModel {
    private String username;

    public SReqAccount() {
        this.username = null;
    }

    @Override
    public void unpack(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        // actual data
        int lUsername = byteBuffer.getInt();

        byte[] bUsername = new byte[lUsername];

        byteBuffer.get(bUsername);

        this.username = new String(bUsername);
    }

    public String getUsername() {
        return this.username;
    }
}
