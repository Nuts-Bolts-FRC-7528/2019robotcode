package frc.robot.auto.actions;

import edu.wpi.first.wpilibj.Timer;
import frc.robot.components.drivetrain;

public class DriveForwardAction implements Action {
    private double timeToDrive;
    private double startTime;
    private double speed;

    public DriveForwardAction(double speed, double seconds) {
        timeToDrive = seconds;
        this.speed = speed;
        startTime = Timer.getFPGATimestamp();
    }

    @Override
    public boolean finished() {
        return (Timer.getFPGATimestamp() - startTime >= timeToDrive);
    }

    @Override
    public void update() {
        drivetrain.setLeftMotorSpeed(speed);
        drivetrain.setRightMotorSpeed(speed);
    }

    @Override
    public void done() {

    }

    @Override
    public void start() {

    }
}
