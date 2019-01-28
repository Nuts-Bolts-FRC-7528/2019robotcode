package frc.robot.components;

import frc.robot.common.robotMap;

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
}