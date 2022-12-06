package obj;

import GUI.GUI;
import datamodel.send.CSenAnswer;
import network.Network;
import network.NetworkSetting;

import java.util.HashMap;
import java.util.Map;

public class GameController {
    private int numOfRacers, numOfEliminatedRacers;
    private Racer cRacer;
    private HashMap<String, Player> cOpponents;
    private GameQuestion currentQuestion;

    private static GameController gameController = null;
    public static GameController getInstance() {
        if (gameController == null) {
            gameController = new GameController();
        }
        return gameController;
    }

    public GameController() {
        this.cRacer = new Racer(
                "",
                GameSetting.INIT_RACER_POSITION,
                0,
                GameSetting.RACER_STATUS_FLAG.FLAG_READY,
                GameSetting.STATUS_STRING[GameSetting.RACER_STATUS_FLAG.FLAG_READY]);

        this.numOfRacers = 0;
        this.cOpponents = null;
        this.currentQuestion = null;
        gameController = this;
    }

    public Racer getCRacer() { return this.cRacer; }

    public int getNumOfRacers() { return this.numOfRacers; }
    public void setNumOfRacers(int numOfRacers) { this.numOfRacers = numOfRacers; }

    public int getCurrentNumOfRacers() { return this.cOpponents.size() + 1; }

    public void setInitCOpponents(HashMap<String, Player> cOpponents) {
        this.cOpponents = cOpponents;

        GUI.getInstance().initOpponentProgressWhenReceiveNumOfPplJoinning();

        for (Map.Entry<String, Player> opps : this.cOpponents.entrySet()) {
            System.out.println("PREV OPPOs: " + opps.getKey());
            GUI.getInstance().updateOpponentNameWhenJoin(opps.getValue());
        }
    }

    public void addNewOpponent(Player cNewOpponent) {
        System.out.println("NEW OPPOs: " + cNewOpponent.getNickname());
        this.cOpponents.put(cNewOpponent.getNickname(), cNewOpponent);
        GUI.getInstance().updateOpponentNameWhenJoin(cNewOpponent);
        GUI.getInstance().updateOpponentNameWhenJoin(cNewOpponent);
    }

    public void updateAnOpponent(Player cOpponent) {
        // replace old info
        this.cOpponents.put(cOpponent.getNickname(), cOpponent);

        switch (cOpponent.getStatusFlag()) {
            case GameSetting.RACER_STATUS_FLAG.FLAG_READY:
            case GameSetting.RACER_STATUS_FLAG.FLAG_NORMAL:
            case GameSetting.RACER_STATUS_FLAG.FLAG_FASTEST:
            case GameSetting.RACER_STATUS_FLAG.FLAG_WRONG:
            case GameSetting.RACER_STATUS_FLAG.FLAG_TIMEOUT:
                GUI.getInstance().updateOpponentProgress(cOpponent);
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED:
                checkForEndgameOnElimination();
                GUI.getInstance().strikeThroughEliminatedRacer(cOpponent);
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_QUIT:
                GUI.getInstance().updateOpponentProgressWhenARacerQuit(cOpponent);
                this.cOpponents.remove(cOpponent.getNickname());
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_VICTORY:
                GUI.getInstance().announceWinner(cOpponent.getNickname());
                GUI.getInstance().updateOpponentProgress(cOpponent);
                break;
            default:
                break;
        }
    }

    public void confirmRacerPostLogin(int numOfVictory) {
        this.cRacer.setNumOfVictory(numOfVictory);

        GUI.getInstance().updateYouNickname(this.cRacer.getNickname()); // update racer name in status panel on UI
        GUI.getInstance().updateYouNumOfVictory(numOfVictory); // update reacer's victory count on UI
    }

    public void setCurrentQuestion(GameQuestion currentQuestion) {
        this.currentQuestion = currentQuestion;

        GUI.getInstance().setFirstNum(currentQuestion.getFirstNum());
        GUI.getInstance().setOperator(currentQuestion.getOperator());
        GUI.getInstance().setSecondNum(currentQuestion.getSecondNum());

        try {
            GUI.getInstance().startAnswering();
            GUI.getInstance().stopAnswering();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void replay() {
        _prepareRacer();
        _prepareOpponents();

        currentQuestion = null;
        numOfEliminatedRacers = 0;
        GUI.getInstance().resetUIForReplay();
    }

    private void _prepareRacer() {
        // update UI
        cRacer.setNumOfIncorrect(0);
        cRacer.setGain(0);
        GUI.getInstance().renewRacerNickname();
        GUI.getInstance().resetYouProgressBar();
    }

    private void _prepareOpponents() {
        for (Map.Entry<String, Player> opps : cOpponents.entrySet()) {
            GUI.getInstance().updateOpponentProgress(opps.getValue());
            GUI.getInstance().updateOpponentNameWhenJoin(opps.getValue());
            GUI.getInstance().renewRacerNickname();
        }
    }

    public void giveAnswer(int cAnswer) {
        CSenAnswer cSenAnswer = new CSenAnswer(
                NetworkSetting.CMD.CMD_ANSWER,
                this.currentQuestion.getQuestionId(),
                cAnswer,
                System.currentTimeMillis() - this.currentQuestion.getTimeOffset()
        );

        Network.getInstance().send(cSenAnswer);
    }

    public void updateThisRacer() {
        // new position, new status on UI
        GUI.getInstance().updateYouPoint(cRacer.getPosition());
        GUI.getInstance().setUpdateStatus(GameSetting.STATUS_STRING[cRacer.getStatusFlag()]);

        String gainStr = cRacer.getGain() >= 0 ? ("+"+ cRacer.getGain()) : String.valueOf(cRacer.getGain());

        switch (cRacer.getStatusFlag()) {
            case GameSetting.RACER_STATUS_FLAG.FLAG_READY:
                GUI.getInstance().resetYouProgressBar();
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_NORMAL:
            case GameSetting.RACER_STATUS_FLAG.FLAG_FASTEST:
            case GameSetting.RACER_STATUS_FLAG.FLAG_TIMEOUT:
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_WRONG:
                cRacer.updateNumOfIncorrectBy(1);
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED:
                checkForEndgameOnElimination();
                GUI.getInstance().strikeThroughYouNickname();
                break;
            case GameSetting.RACER_STATUS_FLAG.FLAG_VICTORY:
                cRacer.updateNumOfVictoryBy(1);
                GUI.getInstance().updateYouNumOfVictory(cRacer.getNumOfVictory());
                break;
            default:
                break;
        }
    }

    private void checkForEndgameOnElimination() {
        numOfEliminatedRacers += 1;

        // if all racers are eliminated
        if (numOfEliminatedRacers >= getCurrentNumOfRacers()) {
            GUI.getInstance().announceNoWinner();
        }
    }

    public void updateAllOpponents(HashMap<String, Player> allRacers) {
        for (Map.Entry<String, Player> racer : allRacers.entrySet()) {
            Player player = racer.getValue();
            if (player.getNickname() != cRacer.getNickname()) {
                updateAnOpponent(player);
            }
        }
    }

    public void updateCorrectAnswer(int answer) {
        GUI.getInstance().updateCorrectAnswer(answer);
    }
}
