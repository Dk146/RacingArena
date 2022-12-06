package network;

import datamodel.DataModel;
import object.GameSetting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkController {
    private ServerNetworkThread serverNetworkThread;
    private ExecutorService networkPool;

    private static NetworkController networkController = null;
    public static NetworkController getInstance() {
        if (networkController == null) {
            networkController = new NetworkController();
        }
        return networkController;
    }

    public NetworkController() {
        this.serverNetworkThread = null;
        this.networkPool = Executors.newFixedThreadPool(1);
        networkController = this;
    }

    public boolean isNetworkOpenning() {
        return this.serverNetworkThread != null && this.serverNetworkThread.isOpenning();
    }

    public void closeNetwork() {
        serverNetworkThread.close();
        networkPool.shutdown();
    }

    public void openServerSocket() {
        serverNetworkThread = new ServerNetworkThread();
        networkPool.execute(serverNetworkThread);
    }

    public void sendToAllClient (DataModel data, int callerID, boolean ignoreCaller) {
        serverNetworkThread.signalAllClients(data, callerID, ignoreCaller);
    }

    public static class ServerNetworkThread implements Runnable {
        private ServerSocket serverSocket;
        private ExecutorService clientPool;
        private int cSocketID;
        private HashMap<Integer, SSocketThread> cSocketThreads;

        public ServerNetworkThread() {
            this.serverSocket = null;
            this.cSocketThreads = new HashMap<>();
            this.cSocketID = 0;
            this.clientPool = Executors.newFixedThreadPool(GameSetting.MAX_NUM_OF_RACERS);
        }

        @Override
        public void run() {
            System.out.println("Server is waiting to accept user...");

            try {
                this.serverSocket = new ServerSocket(NetworkSetting.SERVER_PORT);

            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }

            try {
                while (true) {
                    Socket cSocket = null;
                    try {
                        System.out.println("CREATE SOCKET");
                        cSocket = this.serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    this.cSocketID += 1;
                    SSocketThread clientThread = new SSocketThread(cSocket, this.cSocketID, this);

                    this.subscribeClientSocket(this.cSocketID, clientThread);
                    this.clientPool.execute(clientThread);
                }
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.clientPool.shutdown();
            }
        }

        public void subscribeClientSocket(int cSocketThreadID, SSocketThread cSockThread) {
            this.cSocketThreads.put(cSocketThreadID, cSockThread);
        }

        public void unSubscribeClientSocket(int cSocketThreadID) {
            this.cSocketThreads.remove(cSocketThreadID);
        }

        public boolean isOpenning() {
            return !this.serverSocket.isClosed();
        }

        public void close() {
            try {
                this.serverSocket.close();
                this.clientPool.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void signalAllClients(DataModel data, int callerID, boolean ignoreCaller) {
            if (ignoreCaller) {
                for (Map.Entry<Integer, SSocketThread> entry : this.cSocketThreads.entrySet()) {
                    if (entry.getKey() != callerID && entry.getValue().getsRacerName() != null) {
                        entry.getValue().reply(data);
                    }
                }
            }
            else {
                for (Map.Entry<Integer, SSocketThread> entry : this.cSocketThreads.entrySet()) {
                    if (entry.getValue().getsRacerName() != null) {
                        entry.getValue().reply(data);
                    }
                }
            }
        }

        public int getNumberOfClient() {
            return this.cSocketThreads.size();
        }
    }
}