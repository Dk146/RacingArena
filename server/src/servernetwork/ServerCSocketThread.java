package servernetwork;

import org.h2.tools.Server;
import serverdatabase.ServerDBConfig;
import serverdatabase.ServerDBHelper;

import serverdatamodel.ServerDataModel;
import serverdatamodel.request.SReqAccount;
import serverdatamodel.request.SReqAnswer;
import serverdatamodel.response.SResLoginError;
import serverdatamodel.response.SResLoginSuccess;
import serverdatamodel.response.SResOpponentInfo;

import serverobject.ServerGameConfig;
import serverobject.ServerGameMaster;
import serverobject.ServerQuestion;
import serverobject.ServerRacerObject;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerCSocketThread implements Runnable{
    private int cSocketID;
    private boolean isPermittedToRun;

    private Socket socketOfServer;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private ServerNetwork.ServerNetworkThread parentThread;
    static ArrayList<String> usrNames = new ArrayList<String>();

    private String sRacerName;

    public ServerCSocketThread(Socket _socketOfServer, int _cSocketID, ServerNetwork.ServerNetworkThread _parentThread) {
        this.cSocketID = _cSocketID;
        this.isPermittedToRun = true;

        this.socketOfServer = _socketOfServer;
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
        try {
            while (this.isPermittedToRun) {
                int cmd = inStream.readInt();

                int lData = inStream.available();
                byte[] bytes = new byte[lData];
                inStream.read(bytes);

                // switch on command id
                switch (cmd) {
                    case ServerNetworkConfig.CMD.CMD_LOGIN:
                        handleLogin(cmd, bytes, this.outStream, this.parentThread);
                        break;
                    case ServerNetworkConfig.CMD.CMD_ANSWER:
                        handleAnswer(bytes);
                        break;
                    case ServerNetworkConfig.CMD.DISCONNECT:
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
        }
    }

    public String getsRacerName() {
        return this.sRacerName;
    }

    public void finalizeOnClose() throws IOException {
        SResOpponentInfo sResOpponentInfo = new SResOpponentInfo(
                ServerNetworkConfig.CMD.CMD_INFO,
                ServerNetworkConfig.INFO_TYPE_FLAG.TYPE_NOTICE_UPDATE_OPPONENT,
                this.sRacerName,
                ServerGameMaster.getInstance());
        this.parentThread.signalAllClients(sResOpponentInfo, this.cSocketID, true);

        inStream.close(); // close input stream
        outStream.close(); // close output stream
        socketOfServer.close(); // close the given socket

        this.parentThread.unSubscribeClientSocket(this.cSocketID); // remove this client socket from the array of network's client sockets
        this.isPermittedToRun = false; // break loop in run()

        System.out.println(getClass().getSimpleName() + ": Client "+ this.getsRacerName() +" disconnected");

        ServerGameMaster.getInstance().removeRacer(this.sRacerName); // tell master to remove itself

    }

    public void reply(ServerDataModel data) {
        try {
            outStream.write(data.pack());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(int cmd, byte[] bytes, DataOutputStream outStream, ServerNetwork.ServerNetworkThread parentThread) throws SQLException, IOException {
        SReqAccount sReqAccount = new SReqAccount();
        sReqAccount.unpack(bytes);

        System.out.println(this.getClass().getSimpleName() + ": request login: " + sReqAccount.getUsername());
        for (String name: usrNames) {
            System.out.println(name);
        }

        if (ServerGameMaster.getInstance().getCurrentNumOfRacers() >= ServerGameMaster.getInstance().getNumOfRacers()) {
            SResLoginError sResLoginError = new SResLoginError(cmd, ServerNetworkConfig.LOGIN_FLAG.NO_MORE_SLOTS);
            outStream.write(sResLoginError.pack());
        }

        if (!usrNames.contains(sReqAccount.getUsername())) {
            usrNames.add(sReqAccount.getUsername());

            ServerRacerObject sRacer = new ServerRacerObject(sReqAccount.getUsername(), 0);
            ServerGameMaster.getInstance().addSRacer(sRacer);

            this.sRacerName = sReqAccount.getUsername();

            SResLoginSuccess sResLoginSuccess = new SResLoginSuccess(cmd, ServerNetworkConfig.LOGIN_FLAG.SUCCESS, sReqAccount.getUsername(), 0, ServerGameMaster.getInstance());
            outStream.write(sResLoginSuccess.pack());

            SResOpponentInfo sResOpponentInfo = new SResOpponentInfo(ServerNetworkConfig.CMD.CMD_INFO, ServerNetworkConfig.INFO_TYPE_FLAG.TYPE_NOTICE_NEW_OPPONENT, sReqAccount.getUsername(), ServerGameMaster.getInstance());
            this.parentThread.signalAllClients(sResOpponentInfo, this.cSocketID, true);

            for (String name: usrNames) {
                System.out.println(name);
            }

            System.out.println("OK");
        }
        else {
            SResLoginError sResLoginError = new SResLoginError(cmd, ServerNetworkConfig.LOGIN_FLAG.DUPLICATED_LOGIN);
            outStream.write(sResLoginError.pack());
            System.out.println("DUP");
        }

    }

    private void handleAnswer(byte[] bytes) {
        SReqAnswer sReqAnswer = new SReqAnswer();
        sReqAnswer.unpack(bytes);

        ServerRacerObject thisRacer = ServerGameMaster.getInstance().getRacerByUsername(this.sRacerName);

        ServerQuestion currentSQuestion = ServerGameMaster.getInstance().getQuestion(sReqAnswer.getCQuestionID());

        // check for time-out first
        long sDeltaAnsweringTime = sReqAnswer.getCAnsweringTime() - currentSQuestion.getStartingTimeOfQuestion();
        thisRacer.setCurrDeltaSAnsweringTime(sDeltaAnsweringTime);
        System.out.println(getClass().getSimpleName() + "this racer init pos: " + thisRacer.getPosition());
        System.out.println(getClass().getSimpleName() + ": " + sReqAnswer.getCAnswer() + " " + sReqAnswer.getCQuestionID() + " " + sDeltaAnsweringTime);

        // if not timeout, then check for correctness
        if (sDeltaAnsweringTime <= ServerGameConfig.MAX_TIMER_MILIS) {
            // signal master to show racer's answer on UI
            ServerGameMaster.getInstance().receiveAnswerFromARacer(thisRacer.getUsername(), sReqAnswer.getCAnswer());

            int sAnswer = currentSQuestion.getAnswer(); // get actual answer from server

            if (sAnswer == sReqAnswer.getCAnswer()) {
                System.out.println(thisRacer.getUsername() + ": NORMAL");
                // correct answer, get 1 point, status normal
                thisRacer.updatePositionBy(ServerGameConfig.GAME_BALANCE.GAIN_NORMAL);
                thisRacer.setStatus(ServerGameConfig.RACER_STATUS_FLAG.FLAG_NORMAL);
            }
            else {
                System.out.println(thisRacer.getUsername() + ": WRONG");
                // incorrect answer, get -1 point, status incorrect
                thisRacer.updatePositionBy(ServerGameConfig.GAME_BALANCE.GAIN_WRONG);
                thisRacer.setStatus(ServerGameConfig.RACER_STATUS_FLAG.FLAG_WRONG);
                thisRacer.updateNumOfWrongBy(1);
            }
        }
        else {
            System.out.println(thisRacer.getUsername() + ": TIME OUT");
            // timeout
            thisRacer.updatePositionBy(ServerGameConfig.GAME_BALANCE.GAIN_TIMEOUT);
            thisRacer.setStatus(ServerGameConfig.RACER_STATUS_FLAG.FLAG_TIMEOUT);
        }
    }
}
