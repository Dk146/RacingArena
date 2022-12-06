import GUI.GUI;
import GUI.GUISetting;

import network.NetworkController;

import object.GameController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerMain {
    private static NetworkController networkController;

    private static GameController gameController;
    private static JFrame serverGUI;

    public static void main(String args[]) {
        initServerGameMaster();
        initServerGUI();
        initServerNetwork();
    }

    private static void initServerGameMaster() {
        gameController = new GameController();
    }

    private static void initServerNetwork() {
        networkController = new NetworkController();
    }


    private static void initServerGUI() {
        serverGUI = new GUI(GUISetting.GAME_NAME);
        serverGUI.pack();

        serverGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (networkController.isNetworkOpenning()) {
                    networkController.closeNetwork();
                }
                super.windowClosing(e);
            }
        });

        serverGUI.setLocationRelativeTo(null);
        serverGUI.setVisible(true);
    }
}
