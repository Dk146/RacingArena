package network;

import GUI.GUI;
import datamodel.DataModel;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network {
    private Socket clientSocket;
    private DataOutputStream outStream;
    private DataInputStream inStream;
    private Thread executor;
    private RecvThread receiverThread;

    // Singleton
    private static Network network = null;
    public static Network getInstance() {
        if (network == null) {
            network = new Network();
            network.connect();
        }
        return network;
    }

    public Network() {
        this.clientSocket = null;
        this.outStream = null;
        this.inStream = null;
        this.receiverThread = null;
        this.network = this;
    }

    public Socket getClientSocket() { return this.clientSocket; }

    public void connect() {
        try {
            // send connection request to Server
            this.clientSocket = new Socket(NetworkSetting.SERVER_HOST, NetworkSetting.SERVER_PORT);

            // output stream at Client (send data to server)
            this.outStream = new DataOutputStream(clientSocket.getOutputStream());

            // input stream at Client (receive data from server)
            this.inStream = new DataInputStream(clientSocket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("Unknown host named " + NetworkSetting.SERVER_HOST);
            return;
        } catch (IOException e) { // if no connection
            System.err.println("I/O Exception in connection to " + NetworkSetting.SERVER_HOST);

            GUI.getInstance().setVisible(false);
            GUI.getInstance().turnOnNoOpenConnectionPane();

            return;
        }

        // notify of successful connection
        GUI.getInstance().setVisible(true); // open client UI
        GUI.getInstance().turnOffNoOpenConnectionPane(); // turn off error message pane

        // Start receiver thread
        receiverThread = new RecvThread(this.clientSocket, this.inStream);
        executor = new Thread(receiverThread);
        executor.start();
    }

    public boolean isConnected() {
        return this.clientSocket.isConnected();
    }

    public void send(DataModel dataModel) {
        try {
            outStream.write(dataModel.pack());
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e);
        }
    }

    public void disconnect() {
        try {
            // send packet to server saying close connection
            outStream.writeInt(NetworkSetting.CMD.DISCONNECT);
            receiverThread.stopReceiverThread();
            executor.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void _wait(int sec) {
        try {
            Thread.sleep(sec);
        } catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}