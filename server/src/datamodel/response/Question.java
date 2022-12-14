package datamodel.response;

import datamodel.DataModel;

import java.nio.ByteBuffer;

public class Question extends DataModel {
    private int cmd;
    private int sQuestionID;

    private int sNum1;
    private int sOp;
    private int sNum2;

    private long sQuestionStartTime;

    public Question(int _cmd, int _sQuestionID, int _sNum1, int _sOp, int _sNum2, long _sQuestionStartTime) {
        this.cmd = _cmd;
        this.sQuestionID = _sQuestionID;
        this.sNum1 = _sNum1;
        this.sOp = _sOp;
        this.sNum2 = _sNum2;
        this.sQuestionStartTime = _sQuestionStartTime;
    }

    @Override
    public byte[] pack() {
        int capacity = Integer.BYTES // cmd
                + Integer.BYTES // storing sQuestionID
                + Integer.BYTES // storing sNum1
                + Integer.BYTES // storing sOp
                + Integer.BYTES // storing sNum2
                + Long.BYTES; // storing sQuestionStartTime
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);


        byteBuffer.putInt(this.cmd);
        byteBuffer.putInt(this.sQuestionID);
        byteBuffer.putInt(this.sNum1);
        byteBuffer.putInt(this.sOp);
        byteBuffer.putInt(this.sNum2);
        byteBuffer.putLong(this.sQuestionStartTime);

        // Return a byte[] array
        return byteBuffer.array();
    }

    @Override
    public void unpack(byte[] bytes) {}
}
