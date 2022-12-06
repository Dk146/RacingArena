package datamodel.response;

import datamodel.DataModel;
import object.GameController;
import object.Racer;

import java.nio.ByteBuffer;

public class EnemyInfo extends DataModel {
    private int cmd;
    private int eventFlag;

    private String newRacerUsername;
    private GameController sGameMaster;

    public EnemyInfo(int _cmd, int _eventFlag, String _newRacerUsername, GameController _sGameMaster) {
        this.cmd = _cmd;
        this.eventFlag = _eventFlag;
        this.newRacerUsername = _newRacerUsername;
        this.sGameMaster = _sGameMaster;
    }

    @Override
    public byte[] pack() {
        int capacity = Integer.BYTES // cmd
                + Integer.BYTES // storing eventFlag
                + Integer.BYTES // storing numOfRacers

                + this.sGameMaster.getSizeInBytesOfRacer(this.newRacerUsername);
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);


        byteBuffer.putInt(this.cmd);
        // Then put data sequentially
        byteBuffer.putInt(this.eventFlag);
        byteBuffer.putInt(this.sGameMaster.getCurrentNumOfRacers());

        Racer aRacer = this.sGameMaster.getRacerInfo(this.newRacerUsername);

        // string username
        byteBuffer.putInt(aRacer.getUsername().length());
        byteBuffer.put(aRacer.getUsername().getBytes());
        // int position;
        byteBuffer.putInt(aRacer.getPosition());
        // int rStatus;
        byteBuffer.putInt(aRacer.getStatus());

        // Return a byte[] array
        return byteBuffer.array();
    }

    @Override
    public void unpack(byte[] bytes) {}
}
