package datamodel.response;

import datamodel.DataModel;
import object.GameController;
import object.Racer;

import java.nio.ByteBuffer;
import java.util.Map;

public class LoginSuccess extends DataModel {
    private int cmd;
    private int eventFlag;

    private String cUsername;
    private int racerVictory;

    private GameController sGameMaster;

    public LoginSuccess(int _cmd, int _eventFlag, String _cUsername, int _racerVictory, GameController _sGameMaster) {
        this.cmd = _cmd;
        this.eventFlag = _eventFlag;
        this.cUsername = _cUsername;
        this.racerVictory = _racerVictory;
        this.sGameMaster = _sGameMaster;
    }

    @Override
    public byte[] pack() {
        int capacity = Integer.BYTES // cmd
                + Integer.BYTES // storing eventFlag
                + Integer.BYTES // storing racerVictory
                + Integer.BYTES // storing numOfRacers
                + Integer.BYTES // storing racerLength
                + Integer.BYTES // storing currentNumOfRacers
                + this.sGameMaster.getSizeInBytes(true, this.cUsername); // storing racers array

        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        byteBuffer.putInt(this.cmd);
        byteBuffer.putInt(this.eventFlag);
        byteBuffer.putInt(this.racerVictory);
        byteBuffer.putInt(this.sGameMaster.getNumOfRacers());
        byteBuffer.putInt(this.sGameMaster.getRaceLength());
        byteBuffer.putInt(this.sGameMaster.getCurrentNumOfRacers());

        for (Map.Entry<String, Racer> entry : this.sGameMaster.getsRacers().entrySet()) {
            if (!entry.getKey().equals(this.cUsername)) {
                Racer racerObject = entry.getValue();

                byteBuffer.putInt(racerObject.getUsername().length());
                byteBuffer.put(racerObject.getUsername().getBytes());
                byteBuffer.putInt(racerObject.getPosition());
                byteBuffer.putInt(racerObject.getStatus());
            }
        }

        return byteBuffer.array();
    }

    @Override
    public void unpack(byte[] bytes) {}
}
