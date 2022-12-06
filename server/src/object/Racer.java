package object;

public class Racer {
    private String username;
    private int winningGames; // private to individual
    private int position;
    private int status;

    private int prevPosition;
    private int numOfWrong;
    private long currDeltaSAnsweringTime;

    public Racer(String _username, int _winningGame) {
        this.username = _username;
        this.winningGames = _winningGame;
        this.position = GameSetting.INIT_RACER_POSITION;
        this.status = GameSetting.RACER_STATUS_FLAG.FLAG_READY;

        this.prevPosition = GameSetting.INIT_RACER_POSITION;
        this.numOfWrong = 0;
        this.currDeltaSAnsweringTime = GameSetting.INIT_RACER_DELTA_ANSWERING_TIME;
    }

    public Racer() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getWinningGames() { return winningGames; }
    public void setWinningGames(int winningGames) { this.winningGames = winningGames; }
    public void updateNumOfVictoryBy(int delta) { this.winningGames += delta; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public void updatePositionBy(int delta) {
        this.position += delta;
        System.out.println("At Position: " + this.position);
        if (this.position < GameSetting.INIT_RACER_POSITION) {
            this.position = GameSetting.INIT_RACER_POSITION;
        }
    }

    public int getGain() { return this.position - prevPosition; }

    public int getNumOfWrong() { return this.numOfWrong; }
    public void setNumOfWrong(int numOfWrong) {
        this.numOfWrong = numOfWrong;
    }

    public void updateNumOfWrongBy(int delta) {
        this.numOfWrong += delta;
    }

    public long getCurrDeltaSAnsweringTime() {
        return currDeltaSAnsweringTime;
    }

    public void setCurrDeltaSAnsweringTime(long currDeltaSAnsweringTime) {
        this.currDeltaSAnsweringTime = currDeltaSAnsweringTime;
    }

    public void resetRacerForNewQuestion() {
        this.status = GameSetting.RACER_STATUS_FLAG.FLAG_READY;
        this.prevPosition = this.position;
        this.currDeltaSAnsweringTime = GameSetting.INIT_RACER_DELTA_ANSWERING_TIME;
    }

    public void resetRacerForNewMatch() {
        this.status = GameSetting.RACER_STATUS_FLAG.FLAG_READY;
        this.prevPosition = GameSetting.INIT_RACER_POSITION;
        this.position = GameSetting.INIT_RACER_POSITION;
        this.numOfWrong = 0;
        this.currDeltaSAnsweringTime = GameSetting.INIT_RACER_DELTA_ANSWERING_TIME;
    }
}
