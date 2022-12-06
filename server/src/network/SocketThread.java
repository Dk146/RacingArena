package network;

import datamodel.DataModel;
import datamodel.request.Account;
import datamodel.request.Answer;
import datamodel.response.LoginError;
import datamodel.response.LoginSuccess;
import datamodel.response.EnemyInfo;

import object.GameSetting;
import object.GameController;
import object.GameQuestion;
import object.Racer;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class SocketThread implements Runnable{
    private int cSocketID;
    private boolean isPermittedToRun;

    private Socket socketOfServer;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private NetworkController.ServerNetworkThread parentThread;
    static ArrayList<String> usrNames = new ArrayList<String>();

    private String sRacerName;

    public SocketThread(Socket _socketOfServer, int _cSocketID, NetworkController.ServerNetworkThread _parentThread) {
        this.cSocketID = _cSocketID;
        this.isPermittedToRun = true;

        this.socketOfServer = _socketOfServer;
        System.out.println(_socketOfServer.getInetAddress());
        System.out.println(_socketOfServer.getPort());

        // server socket I/O
        try {
            inStream = new DataInputStream(socketOfServer.getInputStream());
            outStream = new DataOutputStream(socketOfServer.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.parentThread = _parentThread;
        this.sRacerName = null;
    }

    @Override
    public void run() {
        System.out.println("SERVERCSCHOKET RUN");
        try {
            while (this.isPermittedToRun) {
                Thread.sleep(5000);
                int cmd = inStream.readInt();

                int lData = inStream.available();
                byte[] bytes = new byte[lData];
                inStream.read(bytes);


                // switch on command id
                switch (cmd) {
                    case NetworkSetting.CMD.CMD_LOGIN:
                        handleLogin(cmd, bytes, this.outStream, this.parentThread);
                        break;
                    case NetworkSetting.CMD.CMD_ANSWER:
                        handleAnswer(bytes);
                        break;
                    case NetworkSetting.CMD.DISCONNECT:
                        finalizeOnClose();
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getsRacerName() {
        return this.sRacerName;
    }

    public void finalizeOnClose() throws IOException {
        EnemyInfo enemyInfo = new EnemyInfo(
                NetworkSetting.CMD.CMD_INFO,
                NetworkSetting.INFO_TYPE_FLAG.TYPE_NOTICE_UPDATE_OPPONENT,
                this.sRacerName,
                GameController.getInstance());
        this.parentThread.signalAllClients(enemyInfo, this.cSocketID, true);

        inStream.close(); // close input stream
        outStream.close(); // close output stream
        socketOfServer.close(); // close the given socket

        this.parentThread.unSubscribeClientSocket(this.cSocketID); // remove this client socket from the array of network's client sockets
        this.isPermittedToRun = false; // break loop in run()

        GameController.getInstance().removeRacer(this.sRacerName); // tell master to remove itself

    }

    public void reply(DataModel data) {
        try {
            outStream.write(data.pack());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(int cmd, byte[] bytes, DataOutputStream outStream, NetworkController.ServerNetworkThread parentThread) throws SQLException, IOException {
        Account account = new Account();
        account.unpack(bytes);

        System.out.println(this.getClass().getSimpleName() + ": request login: " + account.getUsername());
        for (String name: usrNames) {
            System.out.println(name);
        }

        if (GameController.getInstance().getCurrentNumOfRacers() >= GameController.getInstance().getNumOfRacers()) {
            LoginError loginError = new LoginError(cmd, NetworkSetting.LOGIN_FLAG.NO_MORE_SLOTS);
            outStream.write(loginError.pack());
        }

        if (!usrNames.contains(account.getUsername())) {
            usrNames.add(account.getUsername());

            Racer sRacer = new Racer(account.getUsername(), 0);
            GameController.getInstance().addSRacer(sRacer);

            this.sRacerName = account.getUsername();

            LoginSuccess loginSuccess = new LoginSuccess(cmd, NetworkSetting.LOGIN_FLAG.SUCCESS, account.getUsername(), 0, GameController.getInstance());
            outStream.write(loginSuccess.pack());

            EnemyInfo enemyInfo = new EnemyInfo(NetworkSetting.CMD.CMD_INFO, NetworkSetting.INFO_TYPE_FLAG.TYPE_NOTICE_NEW_OPPONENT, account.getUsername(), GameController.getInstance());
            this.parentThread.signalAllClients(enemyInfo, this.cSocketID, true);

            for (String name: usrNames) {
                System.out.println(name);
            }

            System.out.println("OK");
        }
        else {
            LoginError loginError = new LoginError(cmd, NetworkSetting.LOGIN_FLAG.DUPLICATED_LOGIN);
            outStream.write(loginError.pack());
            System.out.println("DUP");
        }

    }

    private void handleAnswer(byte[] bytes) {
        Answer answer = new Answer();
        answer.unpack(bytes);

        Racer thisRacer = GameController.getInstance().getRacerByUsername(this.sRacerName);

        GameQuestion currentSGameQuestion = GameController.getInstance().getQuestion(answer.getCQuestionID());

        // check for time-out first
        long sDeltaAnsweringTime = answer.getCAnsweringTime() - currentSGameQuestion.getStartingTimeOfQuestion();
        thisRacer.setCurrDeltaSAnsweringTime(sDeltaAnsweringTime);
        System.out.println(getClass().getSimpleName() + "this racer init pos: " + thisRacer.getPosition());
        System.out.println(getClass().getSimpleName() + ": " + answer.getCAnswer() + " " + answer.getCQuestionID() + " " + sDeltaAnsweringTime);

        // if not timeout, then check for correctness
        if (sDeltaAnsweringTime <= GameSetting.MAX_TIMER_MILIS) {
            // signal master to show racer's answer on UI
            GameController.getInstance().receiveAnswerFromARacer(thisRacer.getUsername(), answer.getCAnswer());

            int sAnswer = currentSGameQuestion.getAnswer(); // get actual answer from server

            if (sAnswer == answer.getCAnswer()) {
                System.out.println(thisRacer.getUsername() + ": NORMAL");
                // correct answer, get 1 point, status normal
                thisRacer.updatePositionBy(GameSetting.GAME_BALANCE.GAIN_NORMAL);
                thisRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_NORMAL);
            }
            else {
                System.out.println(thisRacer.getUsername() + ": WRONG");
                // incorrect answer, get -1 point, status incorrect
                thisRacer.updatePositionBy(GameSetting.GAME_BALANCE.GAIN_WRONG);
                thisRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_WRONG);
                thisRacer.updateNumOfWrongBy(1);
            }
        }
        else {
            System.out.println(thisRacer.getUsername() + ": TIME OUT");
            // timeout
            thisRacer.updatePositionBy(GameSetting.GAME_BALANCE.GAIN_TIMEOUT);
            thisRacer.setStatus(GameSetting.RACER_STATUS_FLAG.FLAG_TIMEOUT);
        }
    }
}
