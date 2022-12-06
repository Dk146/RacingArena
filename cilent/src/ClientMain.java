import GUI.GUI;
import GUI.GUISetting;
import network.Network;
import obj.GameController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientMain {
    private static Network network;

    private static GameController gameController;
    private static JFrame clientGUI;

    public static void main(String[] args) {
        initClientGUI();
        connectToServer();
        initClientGameMaster();
    }

    private static void initClientGameMaster() {
        gameController = new GameController();
    }

    private static void connectToServer() {
        network = new Network();
        network.connect();
    }

    private static void initClientGUI() {
        clientGUI = new GUI(GUISetting.GAME_NAME);
        clientGUI.pack();

        clientGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (network.isConnected()) {
                    network.disconnect();
                    System.out.println(ClientMain.class.getSimpleName() + ": disconnect from server");
                }
                super.windowClosed(e);
                System.exit(-1);
            }
        });

        clientGUI.setLocationRelativeTo(null);
    }
}
