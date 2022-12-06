package object;

import java.util.concurrent.ThreadLocalRandom;

public class GameQuestion {
    private int firstNum, secondNum, operator;
    private long startingTimeOfQuestion;

    public GameQuestion() {
        firstNum = ThreadLocalRandom.current().nextInt(GameSetting.MIN_NUMBER, GameSetting.MAX_NUMBER + 1);
        secondNum = ThreadLocalRandom.current().nextInt(GameSetting.MIN_NUMBER, GameSetting.MAX_NUMBER + 1);
        operator = ThreadLocalRandom.current().nextInt(0, GameSetting.OPERATORS.length);

        while (secondNum == 0 &&
                (operator == GameSetting.OPERATOR_FLAG.DIVIDE_OP || operator == GameSetting.OPERATOR_FLAG.MODULA_OP)) {
            secondNum = ThreadLocalRandom.current().nextInt(GameSetting.MIN_NUMBER, GameSetting.MAX_NUMBER + 1);
            operator = ThreadLocalRandom.current().nextInt(0, GameSetting.OPERATORS.length);
        }
        this.startingTimeOfQuestion = 0;
    }

    public int getFirstNum() { return this.firstNum; }
    public int getSecondNum() { return this.secondNum; }
    public int getOperator() { return this.operator; }

    public long getStartingTimeOfQuestion() { return this.startingTimeOfQuestion; }
    public void setStartingTimeOfQuestion(long startTimeOfQuestion) { this.startingTimeOfQuestion = startTimeOfQuestion; }

    public int getAnswer() {
        switch (operator) {
            case GameSetting.OPERATOR_FLAG.ADD_OP:
                return firstNum + secondNum;
            case GameSetting.OPERATOR_FLAG.MINUS_OP:
                return firstNum - secondNum;
            case GameSetting.OPERATOR_FLAG.MULTIPLY_OP:
                return firstNum * secondNum;
            case GameSetting.OPERATOR_FLAG.DIVIDE_OP:
                return firstNum / secondNum;
            case GameSetting.OPERATOR_FLAG.MODULA_OP:
                return firstNum % secondNum;
            default:
                return Integer.MAX_VALUE;
        }
    }
}
