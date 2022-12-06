package network;

import GUI.GUI;

import datamodel.receive.RacersInfo;
import datamodel.receive.Login;
import datamodel.receive.EnemyId;

import datamodel.receive.Question;
import obj.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class RecvThread implements Runnable {
    private boolean isPermittedToRun;
    private Socket clientSocket;
    private DataInputStream inStream;

    public RecvThread(Socket _clientSocket, DataInputStream _inStream) {
        this.isPermittedToRun = true;
        this.clientSocket = _clientSocket;
        this.inStream = _inStream;
    }

    @Override
    public void run() {
        while (this.isPermittedToRun) {
            if (this.clientSocket.isClosed()) {
                return;
            }

            try {
                int cmd = this.inStream.readInt();

                int lData = this.inStream.available();
                byte[] bytes = new byte[lData];
                inStream.read(bytes);

                System.out.println("CHECK CMD" + cmd);
                // Switch on command id
                switch (cmd) {
                    case NetworkSetting.CMD.CMD_LOGIN:
                        receiveLogin(bytes);
                        break;
                    case NetworkSetting.CMD.CMD_INFO:
                        receiveOpponentInfo(bytes);
                        break;
                    case NetworkSetting.CMD.CMD_QUESTION:
                        receiveQuestion(bytes);
                        break;
                    case NetworkSetting.CMD.CMD_RESULT:
                        receiveResult(bytes);
                        break;
                    default:
                        break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void stopReceiverThread() throws IOException {
        isPermittedToRun = false;
        inStream.close();
        clientSocket.close();
    }

    private void receiveQuestion(byte[] bytes) {
        Question question = new Question();
        question.unpack(bytes);

        // update UI
        GameQuestion currentQuestion = new GameQuestion(
                question.getCQuestionID(),
                question.getCNum1(),
                question.getCOp(),
                question.getCNum2(),
                question.getTimeOffset()
        );
        GameController.getInstance().setCurrentQuestion(currentQuestion);
    }

    private void receiveLogin(byte[] bytes) throws InterruptedException {
        Login login = new Login();
        login.unpack(bytes);

        switch (login.getEventFlag()) {
            case NetworkSetting.LOGIN_FLAG.NO_MORE_SLOTS:
                GUI.getInstance().setJoinServerNoti("Full", 0);
                break;
            case NetworkSetting.LOGIN_FLAG.DUPLICATED_LOGIN:
                GUI.getInstance().setJoinServerNoti("Duplicated login ", 0);
                break;
            case NetworkSetting.LOGIN_FLAG.ERROR:
                break;
            case NetworkSetting.LOGIN_FLAG.SUCCESS:
                // confirm this racer, i.e., local input username and password are accepted
                GameController.getInstance().confirmRacerPostLogin(login.getRacerVictory());
                GUI.getInstance().setJoinServerNoti("Success", 9);

                // record his opponents' array
                GameController.getInstance().setNumOfRacers(login.getNumOfRacers());
                GameController.getInstance().setInitCOpponents(login.getcOpponents());

                // lock connection button and text area for nickname and password
                GUI.getInstance().disableComponentAfterJoinServer();

                // resize racers' progress bar
                GUI.getInstance().updateRacersProgressBarSize(login.getRaceLength());
                break;
            
            default:
                break;
        }
    }

    private void receiveOpponentInfo(byte[] bytes) {
        EnemyId enemyId = new EnemyId();
        enemyId.unpack(bytes);

        switch (enemyId.getEventFlag()) {
            case NetworkSetting.INFO_TYPE_FLAG.TYPE_NOTICE_NEW_OPPONENT:
                _ROI_newOpponentInfo(enemyId);
                break;
            case NetworkSetting.INFO_TYPE_FLAG.TYPE_NOTICE_UPDATE_OPPONENT:
                _ROI_updateOpponentInfo(enemyId);
                break;
            default:
                break;
        }
    }

    private void _ROI_newOpponentInfo(EnemyId info) {
        // added new racer
        Player clientOpponent = new Player(
                info.getOpponentUsername(),
                info.getOpponentPosition(),
                0,
                info.getOpponentStatus(),
                "");
        GameController.getInstance().addNewOpponent(clientOpponent);
    }

    private void _ROI_updateOpponentInfo(EnemyId info) {
        // updated a racer
        Player clientOpponent = new Player(
                info.getOpponentUsername(),
                info.getOpponentPosition(),
                0,
                info.getOpponentStatus(),
                "");
        GameController.getInstance().updateAnOpponent(clientOpponent);
    }

    private void receiveResult(byte[] bytes) {
        RacersInfo racersInfo = new RacersInfo();
        racersInfo.unpack(bytes);

        _RR_updateThisRacer(racersInfo, false);
        _RR_updateOpponentsInfo(racersInfo);
        _RR_updateCorrectAnswer(racersInfo);
    }

    private void _RR_updateThisRacer(RacersInfo racersInfo, boolean isNewMatch) {
        // update this racer info
        Racer thisCRacer = GameController.getInstance().getCRacer();
        Player thisPlayer = racersInfo.getThisRacer(thisCRacer.getNickname());

        // update status flag received from server
        thisCRacer.setStatusFlag(thisPlayer.getStatusFlag());

        // player's position on server
        if (isNewMatch) {
            thisCRacer.setGain(0);
        }
        else {
            int newPositionOfThisRacer = thisPlayer.getPosition();
            thisCRacer.setGain(newPositionOfThisRacer - thisCRacer.getPosition());
            thisCRacer.setPosition(newPositionOfThisRacer);
        }

        // signal the master to update its racer with these info
        GameController.getInstance().updateThisRacer();
    }

    private void _RR_updateOpponentsInfo(RacersInfo racersInfo) {
        // signal the master to update the racer opponents with these info
        GameController.getInstance().updateAllOpponents(racersInfo.getAllRacers());
    }

    private void _RR_updateCorrectAnswer(RacersInfo racersInfo) {
        GameController.getInstance().updateCorrectAnswer(racersInfo.getCorrectAnswer());
    }
}
