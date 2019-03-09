package frc.robot.auto.actions;

import edu.wpi.first.wpilibj.Timer;
import frc.robot.components.drivetrain;

/**
 * Drives the motors at a given speed for a given number of seconds
 *
 * @see Action
 */
public class DriveForwardAction implements Action {
    private double timeToDrive; //Amount of seconds to drive the motors for
    private double startTime; //FPGA timestamp at the beginning of the drive
    private double speed; //Speed to drive the motors for

    /**
     * Constructor for a new DriveForwardAction
     *
     * @param speed Amount of PWM signal to give to the motor controllers. Valid ranges are -1.0 - 1.0
     * @param seconds Number of seconds to drive the motor for
     */
    public DriveForwardAction(double speed, double seconds) {
        timeToDrive = seconds;
        this.speed = speed;
    }

    /**
     * Finished() returns whether or not the program is done. In this case, it simply returns whether or not the amount
     * of seconds since the time we started the DriveForwardAction is greater than or equal to the amount of time we told
     * it to drive for, or in layman's terms: if it's driven for the amount of time we want it to.
     *
     * @return Whether the amount of time to drive the motors for has passed
     */
    @Override
    public boolean finished() {
        return (Timer.getFPGATimestamp() - startTime >= timeToDrive);
        //The >= operator makes the above a boolean statement, so this is actually totally legal to do in java
    }

    /**
     * Makes sure the motor controllers are kept at the right speed
     */
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
        startTime = Timer.getFPGATimestamp(); //Sets startTime to the current FPGA timestamp
    }
}
