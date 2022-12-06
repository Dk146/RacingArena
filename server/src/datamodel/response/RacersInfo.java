package datamodel.response;

import datamodel.DataModel;
import object.GameController;
import object.Racer;

import java.nio.ByteBuffer;
import java.util.Map;

public class RacersInfo extends DataModel {
    private int cmd;
    private int sAnswer;
    private GameController sGameMaster;

    public RacersInfo(int _cmd, int _sAnswer, GameController _sGameMaster) {
        this.cmd = _cmd;
        this.sAnswer = _sAnswer;
        this.sGameMaster = _sGameMaster;
    }

    @Override
    public byte[] pack() {
        int capacity = Integer.BYTES // cmd
                + Integer.BYTES // storing answer
                + Integer.BYTES // storing numOfRacers

                + this.sGameMaster.getSizeInBytes(false, null);
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        byteBuffer.putInt(this.cmd);
        // Then put data sequentially
        byteBuffer.putInt(this.sAnswer);
        byteBuffer.putInt(this.sGameMaster.getCurrentNumOfRacers());

        for (Map.Entry<String, Racer> entry : this.sGameMaster.getsRacers().entrySet()) {
            Racer racerObject = entry.getValue();
            byteBuffer.putInt(racerObject.getUsername().length());
            byteBuffer.put(racerObject.getUsername().getBytes());
            byteBuffer.putInt(racerObject.getPosition());
            byteBuffer.putInt(racerObject.getStatus());
        }

        return byteBuffer.array();
    }

    @Override
    public void unpack(byte[] bytes) {}
}
