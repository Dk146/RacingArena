package obj;

public class Racer extends Player {
    private int numOfIncorrect;

    public Racer(String _nickname, int _position, int _gain, int _statusFlag, String _statusStr) {
        super(_nickname, _position, _gain, _statusFlag, _statusStr);
        this.numOfIncorrect = 0;
    }

    public int getNumOfIncorrect() { return this.numOfIncorrect; }
    public void setNumOfIncorrect(int numOfIncorrect) { this.numOfIncorrect = numOfIncorrect; }
    public void updateNumOfIncorrectBy(int incorrectPenalty) { this.numOfIncorrect += incorrectPenalty; }
}
