package clientGUI;

import clientdatamodel.send.CSenLogin;
import clientobject.ClientGameConfig;
import clientobject.ClientGameMaster;

import clientnetwork.ClientNetwork;
import clientnetwork.ClientNetworkConfig;
import clientobject.ClientPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static clientGUI.ClientGUIConfig.ColorButtonConfig.*;

public class ClientGUI extends JFrame {
    private static String userNickname;

    private JPanel ClientPanel;

    private JLabel usernameLabel;
    private JTextField enterUsername;

    private JLabel victoryLabel;
    private JLabel numOfVictory;

    private JButton joinServerButton;
    private JLabel joinServerNoti;
    private JButton submitAnswerButton;

    private JLabel questionLabel;
    private JLabel firstNum, operator, secondNum, equalSign, correctAnswer;
    private JTextField enterAnswer;

    private JLabel updateStatus;

    private JLabel timerLabel;
    private JProgressBar timerBar;
    private Timer timer;

    private JSeparator separator1, separator2, separator3, separator4;
    
    private JLabel racerStatusLabel;
    private JPanel racerStatusPanel;
    private List<Component> racerStatusList;
    private JLabel winnerLabel;
    private JLabel winner;

    private JButton c1, c4, c5, c6, c7;

    // error pane components
    JOptionPane noOpenConnectionPane;
    JDialog noOpenConnectionDialog;
    JButton retryButton, cancelButton;
    JLabel errorMessage;

    // Singleton
    private static ClientGUI clientGUI = null;
    public static ClientGUI getInstance() {
        if (clientGUI == null) {
            clientGUI = new ClientGUI(ClientGUIConfig.GAME_NAME);
        }
        return clientGUI;
    }

    public ClientGUI(String _gameName) {
        super(_gameName);
        clientGUI = this;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.setContentPane(ClientPanel);
        this.setChangeClientGUI();
        this.setPermanentClientGUI();
        this.setButtonAction();
        this.setErrorPaneUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.getRootPane(submitAnswerButton).setDefaultButton(submitAnswerButton);
    }

    private void createUIComponents() {
        racerStatusPanel = new JPanel();
    }

    private void setChangeClientGUI() {
        usernameLabel.setForeground(Color.BLACK);
        timerLabel.setForeground(Color.BLACK);
        questionLabel.setForeground(Color.BLACK);

        racerStatusLabel.setForeground(ClientGUIConfig.LIGHT_ORANGE);
        racerStatusLabel.setFont(new Font("Britannic Bold", Font.BOLD, 25));

        correctAnswer.setFont(new Font("Arial", Font.ITALIC, 11));
        correctAnswer.setText("correct answer");

        // set buttons
        joinServerButton.setBackground(ClientGUIConfig.LIGHT_ORANGE);
        joinServerButton.setForeground(ClientGUIConfig.LIGHT_ORANGE);
        joinServerButton.setBorder(new LineBorder(ClientGUIConfig.LIGHT_ORANGE));
        joinServerButton.setFont(new Font("Arial", Font.BOLD,  16));
        joinServerButton.setEnabled(false);

        submitAnswerButton.setBackground(ClientGUIConfig.LIGHT_ORANGE);
        submitAnswerButton.setForeground(ClientGUIConfig.LIGHT_ORANGE);
        submitAnswerButton.setBorder(new LineBorder(ClientGUIConfig.LIGHT_ORANGE));
        submitAnswerButton.setFont(new Font("Arial", Font.BOLD,  16));
        submitAnswerButton.setEnabled(false);
    }

    private void setPermanentClientGUI() {
        this.getContentPane().setBackground(ClientGUIConfig.BACKGROUND_COLOR);

        // color palette
        setColorButtonUI();

        // set label
        victoryLabel.setFont(new Font("Britannic Bold", Font.BOLD, 15));
        numOfVictory.setFont(new Font("Britannic Bold", Font.BOLD, 15));
        joinServerNoti.setFont(new Font("Arial", Font.ITALIC, 13));

        winnerLabel.setFont(new Font("Britannic Bold", Font.BOLD, 15));
        winner.setFont(new Font("Arial", Font.BOLD, 15));
        winner.setText("Playing...");

        setSeparatorUI();

        setTextBoxUI();
        setEventWithTextBox();

        updateStatus.setFont(new Font("Arial", Font.BOLD, 9));

        setRacerStatusPanelUI();

        createCountDownTimer();
    }

    private void setSeparatorUI() {
        List<JSeparator> sep = Arrays.asList(separator1, separator2, separator3, separator4);

        for (int i = 0; i < sep.size(); ++i) {
            sep.get(i).setBackground(ClientGUIConfig.BORDER_COLOR);
            sep.get(i).setForeground(ClientGUIConfig.BORDER_COLOR);
            sep.get(i).setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, ClientGUIConfig.BORDER_COLOR));
        }
    }

    private boolean checkUsernameValidity(String nickname) {
        return nickname.matches("^[a-zA-Z0-9_]+$");
    }

    private boolean isUsernameValid() {
        if (checkUsernameValidity(enterUsername.getText()) &&
                !enterUsername.equals("Enter your username") && !(enterUsername.getText().length() == 0)) {
            return true;
        }
        return false;
    }

    private void setTextBoxUI() {
        UIManager.put("ToolTip.background", Color.YELLOW);
        UIManager.put("ToolTip.foreground", Color.BLACK);
        UIManager.put("ToolTip.font", new Font("Calibri", Font.PLAIN, 10));

        enterUsername.setBorder(ClientGUIConfig.BORDER);
        enterUsername.setToolTipText("CASE-SENSITIVE, LENGTH <= 10, and ONLY CONTAINS [a-zA-Z0-9_]   ");
        enterUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { // if nickname exceeds MAX_NICKNAME_LENGTH then prevent racer to type more
                if (enterUsername.getText().length() >= ClientGUIConfig.MAX_NICKNAME_LENGTH) {
                    e.consume();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) { 
                if (checkUsernameValidity(enterUsername.getText())) {
                    enterUsername.setForeground((Color.BLACK));
                }
                else {
                    enterUsername.setForeground((Color.RED));
                    joinServerButton.setEnabled(false);
                }

                if (isUsernameValid()) {
                    joinServerButton.setEnabled(true);
                }
                else {
                    joinServerButton.setEnabled(false);
                }
            }
        });

        enterAnswer.setBorder(ClientGUIConfig.BORDER);
        enterAnswer.setToolTipText("Only accept INTEGER");
    }

    private void setEventWithTextBox() {
        enterUsername.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { // if cursor is in the box
                if (enterUsername.getText().equals("Enter your username")) {
                    enterUsername.setText(null);
                }
            }
            @Override
            public void focusLost(FocusEvent e) { // if cursor is not in the box
                if (enterUsername.getText().equals("")) {
                    enterUsername.setForeground((Color.BLACK));
                    enterUsername.setText("Enter your nickname");
                }
            }
        });

        enterAnswer.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { // if cursor is in the box
                if (enterAnswer.getText().equals("Enter your answer")) {
                    enterAnswer.setText(null);
                }
            }
            @Override
            public void focusLost(FocusEvent e) { // if cursor is not in the box
                if (enterAnswer.getText().equals("")) {
                    enterAnswer.setText("Enter your answer");}
            }
        });
    }

    private void createCountDownTimer() {
        timerBar.setStringPainted(true);

        timerBar.setBorder(new LineBorder(ClientGUIConfig.BORDER_COLOR, 2));
        timerBar.setForeground(Color.BLACK);
        timerBar.setBackground(ClientGUIConfig.BACKGROUND_COLOR);
        timerBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.BLACK; }
            protected Color getSelectionForeground() { return ClientGUIConfig.BACKGROUND_COLOR; }
        });

        timerBar.setMaximum(ClientGUIConfig.TIMER_MAX);
        timerBar.setValue(ClientGameConfig.MAX_TIMER_SEC);

        timerBar.setString(Integer.toString(ClientGameConfig.MAX_TIMER_SEC));
    }

    private void setColorButtonUI() {
        List<JButton> colorButtons = Arrays.asList(c1,c4, c5, c6, c7);

        for (int i = 0; i < colorButtons.size(); ++i) {
            colorButtons.get(i).setMaximumSize(new Dimension(COLOR_BUTTON_SIZE, COLOR_BUTTON_SIZE));
            colorButtons.get(i).setPreferredSize(new Dimension(COLOR_BUTTON_SIZE, COLOR_BUTTON_SIZE));
            colorButtons.get(i).setHorizontalAlignment(SwingConstants.CENTER);

            colorButtons.get(i).setBackground(ClientGUIConfig.BACKGROUND_COLOR);
            colorButtons.get(i).setForeground(ClientGUIConfig.LIGHT_ORANGE);

            int index = i;
            colorButtons.get(i).addActionListener(e -> {
                setChangeClientGUI();
                changeRacerStatusBarTheme();
            });
        }
    }

    private void setButtonAction() {
        // click join server button
        joinServerButton.addActionListener(e -> {
            userNickname = enterUsername.getText();

            ClientGameMaster.getInstance().getCRacer().setNickname(userNickname);

            CSenLogin cdLogin = new CSenLogin(ClientNetworkConfig.CMD.CMD_LOGIN, userNickname);
            ClientNetwork.getInstance().send(cdLogin);

            submitAnswerButton.setEnabled(false); // not allow racers to resubmit their answer
        });

        // click to submit answer
        submitAnswerButton.addActionListener(e -> {
            submitAnswerButton.setEnabled(false);
            submitAnswer();
        });
        // press [Enter] to submit answer
        submitAnswerButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER && submitAnswerButton.isEnabled()) {
                    submitAnswerButton.setEnabled(false);
                    submitAnswer();
                }
            }
        });
    }

    private CompoundBorder createProgressBarBorder(int rightThickness) {
        Border line = BorderFactory.createMatteBorder(0, 0, 0, rightThickness, ClientGUIConfig.BORDER_COLOR);
        Border empty = new EmptyBorder(2, 2, 2, 2);
        CompoundBorder border = new CompoundBorder(line, empty);

        return border;
    }

    // create progress bar for every race with the same template
    private JProgressBar createRacerStatusBar(int rightThickness) {
        JProgressBar tmpBar = new JProgressBar();

        tmpBar.setStringPainted(false);
        tmpBar.setVisible(true);

        tmpBar.setMinimumSize(new Dimension(180, 20));
        tmpBar.setBorder(createProgressBarBorder(rightThickness));
        tmpBar.setForeground(ClientGUIConfig.BACKGROUND_COLOR);
        tmpBar.setBackground(ClientGUIConfig.BACKGROUND_COLOR);
        tmpBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() {
                return Color.BLACK;
            }
            protected Color getSelectionForeground() {
                return ClientGUIConfig.BACKGROUND_COLOR;
            }
        });

        tmpBar.setMaximum(ClientGUIConfig.MAX_RACE_LENGTH);
        tmpBar.setValue(ClientGUIConfig.INIT_POSITION);
        tmpBar.setString(Integer.toString(ClientGUIConfig.INIT_POSITION));

        return tmpBar;
    }

    // add component to racer status panel
    private void addComponent(Component component, Container racerStatusPanel,
                              GridBagLayout gblayout, GridBagConstraints gbconstraints,
                              int gridx, int gridy) {

        gbconstraints.gridx = gridx;
        gbconstraints.gridy = gridy;

        gblayout.setConstraints(component, gbconstraints);
        racerStatusPanel.add(component);
    }

    // create current racer progress bar first
    private void createYouProgressBar(GridBagLayout gblayout, GridBagConstraints gbconstraints) {
        gbconstraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel tmpLabel = new JLabel();
        tmpLabel.setMinimumSize(new Dimension(ClientGUIConfig.RACER_STAT_PANEL_LABEL_WIDTH, 20));
        tmpLabel.setText("<HTML>&#x2666; Me &#x2666;</HTML>");
        tmpLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        tmpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tmpLabel.setVerticalAlignment(SwingConstants.CENTER);
        addComponent(tmpLabel, racerStatusPanel, gblayout, gbconstraints, 0, 0); // label on the left

        JProgressBar tmpBar = createRacerStatusBar(3);
        tmpBar.setStringPainted(true);
        tmpBar.setForeground(ClientGUIConfig.LIGHT_ORANGE);
        addComponent(tmpBar, racerStatusPanel, gblayout, gbconstraints, 1, 0); // progress bar on the right
    }

    // create a line to separate between current racer and other racers
    private void createSeparatorBetweenYouAndOtherRacers(GridBagLayout gblayout, GridBagConstraints gbconstraints) {
        JSeparator separator4 = new JSeparator();
        separator4.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ClientGUIConfig.BACKGROUND_COLOR));
        separator4.setAlignmentY(Component.TOP_ALIGNMENT);

        gbconstraints.gridwidth = 2; // separator will go across 2 cells
        gbconstraints.ipadx = ClientGUIConfig.RACER_STAT_PANEL_WIDTH; // padding width
        gbconstraints.ipady = 0; // padding height

        addComponent(separator4, racerStatusPanel, gblayout, gbconstraints, 0, 1);
    }

    // create a grid bag layout to dynamically add racer progress bar
    private void setRacerStatusPanelUI() {
        GridBagLayout gblayout = new GridBagLayout();
        GridBagConstraints gbconstraints = new GridBagConstraints();
        gbconstraints.fill = GridBagConstraints.HORIZONTAL;
        gbconstraints.insets = new Insets(0, 2, 0, 2);
        gbconstraints.weightx = 1;
        gbconstraints.weighty = 1;

        racerStatusPanel.setBackground(ClientGUIConfig.BACKGROUND_COLOR);
        racerStatusPanel.setPreferredSize(new Dimension(250, -1));
        racerStatusPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        racerStatusPanel.setLayout(gblayout);

        createYouProgressBar(gblayout, gbconstraints);
        createSeparatorBetweenYouAndOtherRacers(gblayout, gbconstraints);

        // reset parameter to correctly add labels and progress bars
        gbconstraints.gridwidth = 1;
        gbconstraints.ipadx = 0;
        gbconstraints.ipady = 2;

        for (int i = 0; i < ClientGameConfig.MAX_NUM_OF_RACERS - 1; ++i) {
            JLabel tmpLabel = new JLabel();
            tmpLabel.setMinimumSize(new Dimension(ClientGUIConfig.RACER_STAT_PANEL_LABEL_WIDTH, 25));
            tmpLabel.setText("ImpostorNo" + Integer.toString(i+1));
            tmpLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            tmpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            tmpLabel.setVerticalAlignment(SwingConstants.CENTER);
            tmpLabel.setVisible(false);
            addComponent(tmpLabel, racerStatusPanel, gblayout, gbconstraints, 0, i+2); // label on the left

            JProgressBar tmpBar = createRacerStatusBar(0);
            addComponent(tmpBar, racerStatusPanel, gblayout, gbconstraints, 1, i+2); // progress bar on the right
        }

        racerStatusList = Arrays.asList(racerStatusPanel.getComponents());
    }

    // change racers' progress bar theme
    private void changeRacerStatusBarTheme() {
        // change current racer's progress bar
        racerStatusList.get(1).setForeground(ClientGUIConfig.LIGHT_ORANGE);

    }

    private void setErrorPaneUI() {
        retryButton = new JButton();

        retryButton.setText("RETRY");
        retryButton.setPreferredSize(new Dimension(80, 25));
        retryButton.setBackground(ClientGUIConfig.LIGHT_ORANGE);
        retryButton.setForeground(ClientGUIConfig.BACKGROUND_COLOR);
        retryButton.setBorder(new LineBorder(Color.BLACK));

        retryButton.addActionListener(e -> { ClientNetwork.getInstance().connect(); });

        cancelButton = new JButton();

        cancelButton.setText("CANCEL");
        cancelButton.setPreferredSize(new Dimension(80, 25));
        cancelButton.setBackground(ClientGUIConfig.LIGHT_ORANGE);
        cancelButton.setForeground(ClientGUIConfig.BACKGROUND_COLOR);
        cancelButton.setBorder(new LineBorder(Color.RED));

        cancelButton.addActionListener(e -> { System.exit(-1); });

        errorMessage = new JLabel("<HTML><center>NO OPEN CONNECTION FOR CLIENT</center><HTML>");
        errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        errorMessage.setFont(new Font("Arial", Font.BOLD, 13));

        noOpenConnectionDialog = new JDialog((Frame)null, Dialog.ModalityType.TOOLKIT_MODAL);
        noOpenConnectionDialog.setTitle("CONNECTION ERROR");

        noOpenConnectionPane = new JOptionPane(errorMessage, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);
        noOpenConnectionPane.setOptions(new Object[]{retryButton, cancelButton});

        noOpenConnectionDialog.setContentPane(noOpenConnectionPane);
        noOpenConnectionDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        noOpenConnectionDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(-1);
            }
        });

        noOpenConnectionDialog.pack();
        noOpenConnectionDialog.setLocationRelativeTo(null);
        noOpenConnectionDialog.setVisible(false);
    }

    public void setJoinServerNoti(String str, int color) {
        joinServerNoti.setForeground(new Color(0x025c1a));
        joinServerNoti.setText(str);
    }

    public void disableComponentAfterJoinServer() {
        enterUsername.setEnabled(false);
        joinServerButton.setEnabled(false);
    }

    public void turnOnNoOpenConnectionPane() {
        noOpenConnectionDialog.setVisible(true);
    }

    public void turnOffNoOpenConnectionPane() {
        if (this.noOpenConnectionDialog == null) return;
        this.noOpenConnectionDialog.setVisible(false);
    }

    public void updateYouNumOfVictory(int numOfVictory) {
        this.numOfVictory.setText(Integer.toString(numOfVictory));
    }

    public void updateRacersProgressBarSize(int raceLength) {
        ((JProgressBar)racerStatusList.get(1)).setMaximum(raceLength);

        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            ((JProgressBar)racerStatusList.get(i*2)).setMaximum(raceLength);
        }
    }

    public void updateYouNickname(String nickname) {
        ((JLabel)racerStatusList.get(0)).setText(nickname);
    }

    public void startAnswering() throws InterruptedException {
        if (ClientGameMaster.getInstance().getCRacer().getStatusFlag() != ClientGameConfig.RACER_STATUS_FLAG.FLAG_ELIMINATED) {
            enterAnswer.setEnabled(true);
            submitAnswerButton.setEnabled(true);
        }

        correctAnswer.setFont(new Font("Arial", Font.ITALIC, 11));
        correctAnswer.setText("correct answer ");

        CountDownLatch lock = new CountDownLatch(ClientGameConfig.MAX_TIMER_SEC);

        timer = new Timer(1000, new ActionListener() {
            int counter = ClientGameConfig.MAX_TIMER_SEC;

            public void actionPerformed(ActionEvent ae) {
                --counter;
                timerBar.setValue(counter);
                timerBar.setString(Integer.toString(counter));
                lock.countDown();
                if (counter < 1) {
                    timer.stop();
                }
            }
        });

        timer.start();

        try {
            lock.await();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void stopAnswering() {
        System.out.println("STOP ANSWERING");

        timerBar.setValue(ClientGameConfig.MAX_TIMER_SEC);
        timerBar.setString(Integer.toString(ClientGameConfig.MAX_TIMER_SEC));

        enterAnswer.setEnabled(false);
        submitAnswerButton.setEnabled(false);
    }

    public void setFirstNum(int firstNum) { this.firstNum.setText(Integer.toString(firstNum)); }
    public void setOperator(int operator) { this.operator.setText(ClientGameConfig.OPERATORS[operator]); }
    public void setSecondNum(int secondNum) { this.secondNum.setText(Integer.toString(secondNum)); }

    private void submitAnswer() {
        int answer;
        try {
            answer = Integer.parseInt(this.enterAnswer.getText());
        } catch (NumberFormatException e) {
            answer = Integer.MAX_VALUE;
        }
        ClientGameMaster.getInstance().giveAnswer(answer);
    }

    private String strikeThroughText(String str) {
        return "<HTML><STRIKE>" + str + "</STRIKE></HTML>";
    }

    private String removeStrikeThrough(String str) {
        str = str.replace("<HTML><STRIKE>", "");
        str = str.replace("</STRIKE></HTML>", "");
        return str;
    }

    private Boolean checkIfEqualToNickname(String str, String nickname) {
        if (str.equals(nickname) || str.equals("<HTML><STRIKE>" + nickname + "</STRIKE></HTML>")) {
            return true;
        }
        return false;
    }

    public void updateYouPoint(int point) {
        ((JProgressBar)racerStatusList.get(1)).setValue(point);
        ((JProgressBar)racerStatusList.get(1)).setString(String.valueOf(point));
    }

    public void resetYouProgressBar() {
        ((JProgressBar)racerStatusList.get(1)).setValue(ClientGameConfig.INIT_RACER_POSITION); // reset progress
        ((JProgressBar)racerStatusList.get(1)).setString(Integer.toString(ClientGameConfig.INIT_RACER_POSITION)); // reset progress number
    }

    public void strikeThroughYouNickname() {
        String str = ((JLabel)racerStatusList.get(0)).getText();
        ((JLabel)racerStatusList.get(0)).setText(strikeThroughText(str));
    }

    public void setUpdateStatus(String status) { updateStatus.setText(status); }

    public void initOpponentProgressWhenReceiveNumOfPplJoinning() {
        ((JSeparator)racerStatusList.get(2)).setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, ClientGUIConfig.BORDER_COLOR));

        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            racerStatusList.get(i*2-1).setVisible(true); // show opponent name

            JProgressBar tmpBar = (JProgressBar)(racerStatusList.get(i*2));
            tmpBar.setForeground(ClientGUIConfig.LIGHT_ORANGE); // show opponent bar
            tmpBar.setStringPainted(true); // show opponent bar value
            tmpBar.setBorder(createProgressBarBorder(3)); // show finnish line
        }
    }

    // update the progress bar to show how far each racer has come
    // first label to contain "ImpostorNo" will be the empty slot
    public void updateOpponentNameWhenJoin(ClientPlayer opponent) {
        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            if (((JLabel)racerStatusList.get(i*2-1)).getText().equals("ImpostorNo"+(i-1))) {
                ((JLabel)racerStatusList.get(i*2-1)).setText(opponent.getNickname()); // update opponent name

                break;
            }
        }
    }

    public void updateOpponentProgress(ClientPlayer opponent) {
        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            String str = ((JLabel)racerStatusList.get(i*2-1)).getText();

            if (checkIfEqualToNickname(str, opponent.getNickname())) {
                ((JProgressBar)racerStatusList.get(i*2)).setValue(opponent.getPosition()); // update opponent progress
                ((JProgressBar)racerStatusList.get(i*2)).setString(Integer.toString(opponent.getPosition())); // update progress number

                break;
            }
        }
    }

    // update other racers' IU when a racer quit
    public void updateOpponentProgressWhenARacerQuit(ClientPlayer opponent) {
        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            String str = ((JLabel)racerStatusList.get(i*2-1)).getText();

            if (checkIfEqualToNickname(str, opponent.getNickname())) {
                ((JLabel)racerStatusList.get(i*2-1)).setText("ImpostorNo"+(i-1)); // reset opponent name

                ((JProgressBar)racerStatusList.get(i*2)).setValue(ClientGameConfig.INIT_RACER_POSITION); // reset progress
                ((JProgressBar)racerStatusList.get(i*2)).setString(Integer.toString(ClientGameConfig.INIT_RACER_POSITION)); // reset progress number

                break;
            }
        }
    }

    public void updateCorrectAnswer(int answer) {
        correctAnswer.setFont(new Font("Arial", Font.PLAIN, 12));
        correctAnswer.setText(Integer.toString(answer));
    }

    public void strikeThroughEliminatedRacer(ClientPlayer opponent) {
        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            String str = ((JLabel)racerStatusList.get(i*2-1)).getText();

            if (checkIfEqualToNickname(str, opponent.getNickname())) {
                String nickName = ((JLabel)racerStatusList.get(i*2-1)).getText();
                ((JLabel)racerStatusList.get(i*2-1)).setText(strikeThroughText(nickName)); // strike through opponent name

                break;
            }
        }
    }

    public void resetUIForReplay() {
        // reset winner announcement
        winner.setFont(new Font("Arial", Font.ITALIC, 9));
        winner.setText("Unknown.");

        // reset question info
        firstNum.setText("1st no.");
        operator.setText("op");
        secondNum.setText("2nd no.");

        correctAnswer.setFont(new Font("Arial", Font.ITALIC, 11));
        correctAnswer.setText("correct answer ");

        // reset answer status
        updateStatus.setText("Answer status");
        updateStatus.setText("Extra status");
    }

    public void updateNumOfVictory(int victory) {
        numOfVictory.setText(Integer.toString(victory));
    }

    public void announceWinner(String winnerName) {
        winner.setFont(new Font("Britannic Bold", Font.PLAIN, 18));
        winner.setText(winnerName);
    }

    public void announceNoWinner() {
        winner.setFont(new Font("Arial", Font.ITALIC, 9));
        winner.setText("No winner.");
    }

    public void renewRacerNickname() {
        JLabel tmpLabel = (JLabel)racerStatusList.get(0);
        tmpLabel.setText(removeStrikeThrough(tmpLabel.getText()));

        for (int i = 2; i < ClientGameMaster.getInstance().getNumOfRacers() + 1; ++i) {
            tmpLabel = (JLabel)racerStatusList.get(i*2-1);
            tmpLabel.setText(removeStrikeThrough(tmpLabel.getText()));
        }
    }
}