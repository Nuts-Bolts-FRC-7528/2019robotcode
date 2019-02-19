package frc.robot.auto;

public class AutoModeDoneException extends Exception {
    private static final long serialVersionUID = 1L;

    public AutoModeDoneException() {
        super("Auto mode already completed");
    }
}