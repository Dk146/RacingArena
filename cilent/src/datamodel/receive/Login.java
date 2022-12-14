package datamodel.receive;

import datamodel.DataModel;
import network.NetworkSetting;

import obj.GameSetting;
import obj.Player;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Login extends DataModel {
    private int eventFlag;
    private int racerVictory;
    private int numOfRacers;
    private int raceLength;
    private int currentNumOfRacers;
    private HashMap<String, Player> cOpponents;

    public Login() {
        this.eventFlag = -1;
        this.racerVictory = -1;
        this.cOpponents = null;
    }

    @Override
    public void unpack(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.rewind();

        this.eventFlag = byteBuffer.getInt();

        if (this.eventFlag == NetworkSetting.LOGIN_FLAG.SUCCESS) {
            this.racerVictory = byteBuffer.getInt();
            this.numOfRacers = byteBuffer.getInt();
            this.raceLength = byteBuffer.getInt();
            this.currentNumOfRacers = byteBuffer.getInt();

            System.out.println("RECEIVE no Of racers: " + this.currentNumOfRacers);

            cOpponents = new HashMap<>();
            for (int i = 0; i < this.currentNumOfRacers - 1; ++i) { // exclude this racer
                System.out.println("LOOP: " + i);

                int lUsername = byteBuffer.getInt();

                byte[] bUsername = new byte[lUsername];
                byteBuffer.get(bUsername);

                String rUsername = new String(bUsername);

                int rPosition = byteBuffer.getInt();

                int rStatus = byteBuffer.getInt();

                Player clientOpponent = new Player(rUsername, rPosition, 0, rStatus, GameSetting.STATUS_STRING[rStatus]);
                cOpponents.put(rUsername, clientOpponent);
            }
        }
    }

    public int getEventFlag() {
        return this.eventFlag;
    }
    public void setEventFlag(int eventFlag) {
        this.eventFlag = eventFlag;
    }

    public int getRacerVictory() {
        return this.racerVictory;
    }
    public void setRacerVictory(int racerVictory) {
        this.racerVictory = racerVictory;
    }

    public int getNumOfRacers() {
        return this.numOfRacers;
    }
    public void setNumOfRacers(int numOfRacers) {
        this.numOfRacers = numOfRacers;
    }

    public int getRaceLength() {
        return this.raceLength;
    }
    public void setRaceLength(int raceLength) {
        this.raceLength = raceLength;
    }

    public int getCurrentNumOfRacers() {
        return this.currentNumOfRacers;
    }
    public void setCurrentNumOfRacers(int currentNumOfRacers) {
        this.currentNumOfRacers = currentNumOfRacers;
    }

    public HashMap<String, Player> getcOpponents() {
        return this.cOpponents;
    }

    public void setcOpponents(HashMap<String, Player> cOpponents) {
        this.cOpponents = cOpponents;
    }
}
