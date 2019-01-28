package frc.robot.components;

import frc.robot.common.robotMap;
import edu.wpi.first.wpilibj.Timer;


/*
* These are a bunch of public methods I wrote for the drivetrain.
* They can be called from any other class, just import this class and
* use, for example, drivetrain.setLeftMotorSpeed(speed) to set the speed
* of the left motors.
*/

public class drivetrain {
    /**
     * Sets the speed for the left drivetrain motors
     * @param speed
     */
    public static void setLeftMotorSpeed(double speed) {
        robotMap.leftFrontDrive.set(speed);
        robotMap.leftRearDrive.set(speed);
    }

    /**
     * Sets the speed for the right drivetrain motors
     * @param speed
     */
    public static void setRightMotorSpeed(double speed) {
        robotMap.rightFrontDrive.set(speed);
        robotMap.rightRearDrive.set(speed);
    }

    /**
     * Returns the average speed for left drivetrain motors
     * @return
     */
    public static double getLeftMotorSpeed() {
        return((robotMap.leftFrontDrive.getSpeed()+robotMap.leftRearDrive.getSpeed())/2);
    }

    /**
     * Returns the average speed for right drivetrain motors
     * @return
     */
    public static double getRightMotorSpeed() {
        return((robotMap.rightFrontDrive.getSpeed()+robotMap.rightRearDrive.getSpeed())/2);
    }

    /*
    * This is just a test method to check controller input and whatnot
    * Will probably not be in final product unless we need it
    */
    public static void turnLeftForSecond() {
        setLeftMotorSpeed(1);
        Timer.delay(1);
        setLeftMotorSpeed(0);
    }
}