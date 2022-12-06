import serverGUI.ServerGUI;
import serverGUI.ServerGUIConfig;

import servernetwork.ServerNetwork;

import serverobject.ServerGameMaster;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerMain {
    private static ServerNetwork serverNetwork;

    private static ServerGameMaster serverGameMaster;
    private static JFrame serverGUI;

    public static void main(String args[]) {
        initServerGameMaster();
        initServerGUI();
        initServerNetwork();
    }

    private static void initServerGameMaster() {
        serverGameMaster = new ServerGameMaster();
    }

    private static void initServerNetwork() {
        serverNetwork = new ServerNetwork();
    }


    private static void initServerGUI() {
        serverGUI = new ServerGUI(ServerGUIConfig.GAME_NAME);
        serverGUI.pack();

        serverGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (serverNetwork.isNetworkOpenning()) {
                    serverNetwork.closeNetwork();
                }
                super.windowClosing(e);
            }
        });

        serverGUI.setLocationRelativeTo(null);
        serverGUI.setVisible(true);
    }
}
