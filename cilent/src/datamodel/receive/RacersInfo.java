package datamodel.receive;

import datamodel.DataModel;
import obj.GameSetting;
import obj.Player;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class RacersInfo extends DataModel {
    private int currentNumOfRacers;
    private int correctAnswer;
    private HashMap<String, Player> cPlayers;

    public RacersInfo() {
        this.currentNumOfRacers = -1;
        this.correctAnswer = Integer.MAX_VALUE;
        this.cPlayers = null;
    }

    @Override
    public void unpack(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        this.correctAnswer = byteBuffer.getInt();
        this.currentNumOfRacers = byteBuffer.getInt();

        cPlayers = new HashMap<>();
        for (int i = 0; i < this.currentNumOfRacers; ++i) {
            int lUsername = byteBuffer.getInt();
            byte[] bUsername = new byte[lUsername];
            byteBuffer.get(bUsername);

            String rUsername = new String(bUsername);

            int rPosition = byteBuffer.getInt();

            int rStatus = byteBuffer.getInt();

            Player clientOpponent = new Player(rUsername, rPosition, 0, rStatus, GameSetting.STATUS_STRING[rStatus]);
            cPlayers.put(rUsername, clientOpponent);
        }
    }

    public int getCorrectAnswer() { return this.correctAnswer; }

    public int getCurrentNumOfRacers() {
        return this.currentNumOfRacers;
    }

    public HashMap<String, Player> getAllRacers() {
        return this.cPlayers;
    }

    public Player getThisRacer(String cRacerUsername) {
        return this.cPlayers.get(cRacerUsername);
    }
}
