package frc.robot.components;

import frc.robot.common.robotMap;
import edu.wpi.first.wpilibj.Timer;

/**
 * These are a bunch of convenience methods for the Drivetrain. It gives methods for setting motor speeds,
 * getting motor speeds, and more.
 */
public class Drivetrain {
    /**
     * Sets the speed for the left Drivetrain motors
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setLeftMotorSpeed(double speed) {
        robotMap.leftFrontDrive.set(speed);
        robotMap.leftRearDrive.set(speed);
    }

    /**
     * Sets the speed for the right Drivetrain motors.
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setRightMotorSpeed(double speed) {
        robotMap.rightFrontDrive.set(-speed);
        robotMap.rightRearDrive.set(-speed);
    }

    /**
     * Returns the average speed for left Drivetrain motors
     * @return The PWM signal of both motor controllers on the left side, averaged out (they should both be the same)
     */
    public static double getLeftMotorSpeed() {
        return((robotMap.leftFrontDrive.getSpeed()+robotMap.leftRearDrive.getSpeed())/2);
    }

    /**
     * Returns the average speed for right Drivetrain motors
     * @return The PWM signal of both motor controllers on the right side, averaged out (they should both be the same)
     */
    public static double getRightMotorSpeed() {
        return((robotMap.rightFrontDrive.getSpeed()+robotMap.rightRearDrive.getSpeed())/2);
    }

    public static void align(int centerPix) {
        if(centerPix > 80) {
            setRightMotorSpeed(.3);
            setLeftMotorSpeed(.4);
            System.out.println("Turning right");
        } else if(centerPix<80) {
            setRightMotorSpeed(.4);
            setLeftMotorSpeed(.3);
            System.out.println("Turning left");
        }
    }
}
