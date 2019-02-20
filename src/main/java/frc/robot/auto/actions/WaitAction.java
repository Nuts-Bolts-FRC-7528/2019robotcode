package frc.robot.auto.actions;

import edu.wpi.first.wpilibj.Timer;

public class WaitAction implements Action {

    private double timeToWait;
    private double startTime;

    public WaitAction(double seconds) {
        timeToWait = seconds;

    }
    @Override
    public boolean finished() {
        return (Timer.getFPGATimestamp() - startTime >= timeToWait);
    }

    @Override
    public void update() {
        //Nothing, as no iterative logic is needed here
    }

    @Override
    public void done() {

    }

    @Override
    public void start() {
        startTime = Timer.getFPGATimestamp();
    }
}