package frc.robot.components;

import frc.robot.common.robotMap;
import edu.wpi.first.wpilibj.Timer;

/**
 * These are a bunch of convenience methods for the drivetrain. It gives methods for setting motor speeds,
 * getting motor speeds, and more.
 */
public class drivetrain {
    /**
     * Sets the speed for the left drivetrain motors
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setLeftMotorSpeed(double speed) {
        robotMap.leftFrontDrive.set(speed);
        robotMap.leftRearDrive.set(speed);
    }

    /**
     * Sets the speed for the right drivetrain motors.
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setRightMotorSpeed(double speed) {
        robotMap.rightFrontDrive.set(-speed);
        robotMap.rightRearDrive.set(-speed);
    }

    /**
     * Returns the average speed for left drivetrain motors
     * @return The PWM signal of both motor controllers on the left side, averaged out (they should both be the same)
     */
    public static double getLeftMotorSpeed() {
        return((robotMap.leftFrontDrive.getSpeed()+robotMap.leftRearDrive.getSpeed())/2);
    }

    /**
     * Returns the average speed for right drivetrain motors
     * @return The PWM signal of both motor controllers on the right side, averaged out (they should both be the same)
     */
    public static double getRightMotorSpeed() {
        return((robotMap.rightFrontDrive.getSpeed()+robotMap.rightRearDrive.getSpeed())/2);
    }

    /**
     * Test method to test manipulator controller. Marked as deprecated as its really just for testing only and serves
     * no functional purpose
     */
    @Deprecated
    public static void turnLeftForSecond() {
        setLeftMotorSpeed(1);
        Timer.delay(1);
        setLeftMotorSpeed(0);
    } 
}
