package object;

import GUI.GUI;
import datamodel.response.RacersInfo;
import network.NetworkController;
import network.NetworkSetting;

import java.util.*;

public class GameController {
    private int numOfRacers;
    private int raceLength;
    private HashMap<String, Racer> sRacers;
    private HashMap<Integer, GameQuestion> sQuestions;
    private Timer questionTimer;
    private boolean isEndgame;
    private int numOfRemainRacers;

    // Singleton
    private static GameController gameController = null;
    public static GameController getInstance() {
        if (gameController == null) {
            gameController = new GameController();
        }
        return gameController;
    }

    public GameController() {
        this.sRacers = new HashMap<>();
        this.sQuestions = new HashMap<>();
        this.numOfRacers = GameSetting.INIT_NUM_OF_RACERS;
        this.numOfRemainRacers = this.numOfRacers;
        this.raceLength = GameSetting.INIT_RACE_LENGTH;
        this.isEndgame = false;
        gameController = this;
    }

    public int getNumOfRacers() { return this.numOfRacers; }
    public void setNumOfRacers(int numOfRacers) {
        this.numOfRacers = numOfRacers;
        this.numOfRemainRacers = numOfRacers;
    }

    public int getRaceLength() { return this.raceLength; }
    public void setRaceLength(int raceLength) { this.raceLength = raceLength; }

    public void addSRacer (Racer sRacer) {
        sRacers.put(sRacer.getUsername(), sRacer);

        System.out.println(Arrays.asList(sRacers));

        // Show this new racer on UI (increase number of joining racers and add to statistics table)
        GUI.getInstance().updatejoiningValue(this.getCurrentNumOfRacers());
        GUI.getInstance().addSRacerToUI(sRacer.getUsername(), sRacer.getGain(), sRacer.getStatus(), sRacer.getPosition());
    }

    public void removeRacer(String racerName) {
        if (racerName == null) {
            return;
        }
        sRacers.remove(racerName);
        GUI.getInstance().updatejoiningValue(this.getCurrentNumOfRacers());
        GUI.getInstance().removeSRacerFromUI(racerName);
    }

    public HashMap<String, Racer> getsRacers() {
        return sRacers;
    }

    public int getCurrentNumOfRacers() {
        return sRacers.size();
    }

    public int getSizeInBytes(boolean ignore, String cUsername) {
        int capacity = 0;

        if (ignore) {
            for (Map.Entry<String, Racer> entry : this.sRacers.entrySet()) {
                if (!entry.getKey().equals(cUsername)) {
                    Racer racerObject = entry.getValue();

                    // string rUsername
                    String rUsername = racerObject.getUsername();
                    capacity += Integer.BYTES; // hold rUsername length
                    capacity += rUsername.length();

                    // int position
                    capacity += Integer.BYTES;

                    // int rStatus
                    capacity += Integer.BYTES;
                }
            }
        }
        else {
            for (Map.Entry<String, Racer> entry : this.sRacers.entrySet()) {
                Racer racerObject = entry.getValue();

                // string rUsername
                String rUsername = racerObject.getUsername();
                capacity += Integer.BYTES; // hold rUsername length
                capacity += rUsername.length();

                // int position
                capacity += Integer.BYTES;

                // int rStatus;
                capacity += Integer.BYTES;
            }
        }

        return capacity;
    }
    public int getSizeInBytesOfRacer (String cUsername) {
        int capacity = 0;

        for (Map.Entry<String, Racer> entry : this.sRacers.entrySet()) {
            if (entry.getKey().equals(cUsername)) {
                Racer racerObject = entry.getValue();

                // string rUsername
                String rUsername = racerObject.getUsername();
                capacity += Integer.BYTES; // hold rUsername length
                capacity += rUsername.length();

                // int position
                capacity += Integer.BYTES;

                // int rStatus;
                capacity += Integer.BYTES;

                break;
            }
        }

        return capacity;
    }

    public Racer getRacerByUsername(String rUsername) {
        return this.sRacers.get(rUsername);
    }

    public Racer getRacerInfo(String rUsername) {
        return this.sRacers.get(rUsername);
    }

    public int getNumberOfPrevQuestions () {
        return this.sQuestions.size();
    }

    public void giveQuestion() {
        GameQuestion serverGameQuestion = new GameQuestion();
        serverGameQuestion.setStartingTimeOfQuestion(System.currentTimeMillis());

        // keep a record of this question in game master
        int sCurrentQuestionID = getNumberOfPrevQuestions() + 1;
        this.sQuestions.put(sCurrentQuestionID, serverGameQuestion);

        GUI.getInstance().setGiveQuestionButton(true);

        // update question on UI
        GUI.getInstance().setFirstNum(serverGameQuestion.getFirstNum());
        GUI.getInstance().setSecondNum(serverGameQuestion.getSecondNum());
        GUI.getInstance().setOperator(serverGameQuestion.getOperator());
        GUI.getInstance().setAnswer(serverGameQuestion.getAnswer());

        // send packet to all clients
        datamodel.response.Question question = new datamodel.response.Question(
                NetworkSetting.CMD.CMD_QUESTION,
                sCurrentQuestionID,
                serverGameQuestion.getFirstNum(),
                serverGameQuestion.getOperator(),
                serverGameQuestion.getSecondNum(),
                serverGameQuestion.getStartingTimeOfQuestion()
        );
        NetworkController.getInstance().sendToAllClient(question, -1, false);

        // start timer
        this._startTimer();
    }

    private void _startTimer() {
        GUI.getInstance().setGiveQuestionButton(false);

        this.questionTimer = new Timer();
        questionTimer.scheduleAtFixedRate(new TimerTask() {
            int time = GameSetting.MAX_TIMER_SEC;

            @Override
            public void run() {
                if (time > 0) {
                    time -= 1;
                    GUI.getInstance().setUpdateTimer(time);
                }
                else {
                    finalEvaluateAfterAnAnswer();
                    if (getCurrentNumOfRacers() == numOfRacers) {
                        GUI.getInstance().setGiveQuestionButton(true);
                    }
                    else {
                        GUI.getInstance().setGiveQuestionButton(false);
                    }
                    questionTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    public GameQuestion getQuestion(int questionID) {
        return this.sQuestions.get(questionID);
    }

    public void finalEvaluateAfterAnAnswer() {
        long _minDeltaSAnsweringTime = Long.MAX_VALUE - 1;
        int lostPointsOfFuckedUpRacers = GameSetting.GAME_BALANCE.GAIN_FASTEST;

        for (Map.Entry<String, Racer> racerEntry : this.sRacers.entrySet()) {
            Racer currRacer = racerEntry.getValue();

            if (currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED &&
                    currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_QUIT) {

                if (currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_WRONG &&
                        currRacer.getCurrDeltaSAnsweringTime() < _minDeltaSAnsweringTime) {
                    _minDeltaSAnsweringTime = currRacer.getCurrDeltaSAnsweringTime();
                }

                // timeout
                if (currRacer.getCurrDeltaSAnsweringTime() == GameSetting.INIT_RACER_DELTA_ANSWERING_TIME) {
                    currRacer.updatePositionBy(GameSetting.GAME_BALANCE.GAIN_TIMEOUT);
                    currRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_TIMEOUT);
                }

                //prepare total lose points of fucked up racers
                if (currRacer.getStatus() == GameSetting.RACER_STATUS_FLAG.FLAG_TIMEOUT) {
                    lostPointsOfFuckedUpRacers += (-1) * GameSetting.GAME_BALANCE.GAIN_TIMEOUT;
                }
                if (currRacer.getStatus() == GameSetting.RACER_STATUS_FLAG.FLAG_WRONG) {
                    lostPointsOfFuckedUpRacers += (-1) * GameSetting.GAME_BALANCE.GAIN_WRONG;
                }

                // eliminate
                if (currRacer.getNumOfWrong() == GameSetting.GAME_BALANCE.MAX_NUM_OF_WRONG) {
                    currRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED);
                    numOfRemainRacers -= 1; // decrease number of remaining racers

                    GUI.getInstance().strikeThroughEliminatedRacer(currRacer.getUsername()); // update table UI

                }
            }
        }

        // reward the fastest
        for (Map.Entry<String, Racer> racerEntry : this.sRacers.entrySet()) {
            Racer currRacer = racerEntry.getValue();

            // ignore eliminated or disconnected racer
            if (currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED &&
                    currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_QUIT) {
                // prepare shortest answering time
                if (currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_WRONG &&
                        currRacer.getCurrDeltaSAnsweringTime() <= _minDeltaSAnsweringTime) {
                    // the fastest
                    currRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_FASTEST);
                    currRacer.updatePositionBy(lostPointsOfFuckedUpRacers);

                    // the racer may also be the victor
                    if (currRacer.getPosition() >= raceLength) {
                        currRacer.updateNumOfVictoryBy(1);
                        currRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_VICTORY);

                        GUI.getInstance().announceWinner(currRacer.getUsername()); // announce winner on UI

                        isEndgame = true;
                        GUI.getInstance().updateControllButtonToReplayButton();
                        GUI.getInstance().changeStateOfControllButton();
                    }
                }
            }
        }

        // if no racers wins the race
        if (numOfRemainRacers <= 0) {
            isEndgame = true;

            GUI.getInstance().updateControllButtonToReplayButton();
            GUI.getInstance().changeStateOfControllButton();
            GUI.getInstance().announceNoWinner();
        }

        int sCorrectAnswer = getQuestion(sQuestions.size()).getAnswer();

        // send to clients
        RacersInfo racersInfo = new RacersInfo(
                NetworkSetting.CMD.CMD_RESULT,
                sCorrectAnswer,
                GameController.getInstance());
        NetworkController.getInstance().sendToAllClient(racersInfo, -1, false);

        // update values on UI and reset flags
        for (Map.Entry<String, Racer> racerEntry : this.sRacers.entrySet()) {
            Racer currRacer = racerEntry.getValue();

            // update values on UI
            GUI.getInstance().updateSRacerToUI(
                    currRacer.getUsername(),
                    currRacer.getGain(),
                    currRacer.getStatus(),
                    currRacer.getPosition());

            // reset flags, ignore eliminated or disconnected racer
            if (currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_ELIMINATED &&
                    currRacer.getStatus() != GameSetting.RACER_STATUS_FLAG.FLAG_QUIT) {
                currRacer.resetRacerForNewQuestion();
            }
        }
    }

    public void replay() {
        isEndgame = false;

        numOfRemainRacers = numOfRacers;

        sQuestions.clear();
        sQuestions = new HashMap<>();

        resetAllRacersForNewMatch(); // reset table
        GUI.getInstance().resetUIForReplay(); // reset UI

        RacersInfo racersInfo = new RacersInfo(NetworkSetting.CMD.CMD_REPLAY, Integer.MAX_VALUE, this);
        NetworkController.getInstance().sendToAllClient(racersInfo, -1, false);
    }

    private void resetAllRacersForNewMatch() {
        for (Map.Entry<String, Racer> racerEntry : this.sRacers.entrySet()) {
            Racer currRacer = racerEntry.getValue();
            currRacer.resetRacerForNewMatch();

            // update values on UI
            System.out.println(currRacer.getUsername());
            GUI.getInstance().renewRacerNickname(currRacer);
            GUI.getInstance().updateSRacerToUI(
                    currRacer.getUsername(),
                    currRacer.getGain(),
                    currRacer.getStatus(),
                    currRacer.getPosition());
            GUI.getInstance().updateSRacerAnswerToUI(currRacer.getUsername(), Integer.MAX_VALUE);
        }
    }

    public void receiveAnswerFromARacer(String racerName, int racerAnswer) {
        GUI.getInstance().updateSRacerAnswerToUI(racerName, racerAnswer);
    }
}
