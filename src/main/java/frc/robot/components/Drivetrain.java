package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * These provide some convenience methods for the Drivetrain. It gives methods for setting motor speeds,
 * getting motor speeds, and more.
 */
@SuppressWarnings("unused")
public class Drivetrain {
    /**
     * Sets the speed for the left Drivetrain motors
     *
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setLeftMotorSpeed(double speed) {
        robotMap.leftFrontDrive.set(speed);
        robotMap.leftRearDrive.set(speed);
    }

    /**
     * Sets the speed for the right Drivetrain motors.
     *
     * @param speed Amount of PWM Signal to give the motors. Valid ranges are -1.0 - 1.0
     */
    public static void setRightMotorSpeed(double speed) {
        robotMap.rightFrontDrive.set(-speed);
        robotMap.rightRearDrive.set(-speed);
    }

    /**
     * Returns the average speed for left Drivetrain motors
     *
     * @return The current PWM signal of both motor controllers on the left side, averaged out
     */
    public static double getLeftMotorSpeed() {
        return ((robotMap.leftFrontDrive.getSpeed() + robotMap.leftRearDrive.getSpeed()) / 2);
    }

    /**
     * Returns the current speed for right Drivetrain motors
     *
     * @return The current PWM signal of both motor controllers on the right side, averaged out
     */
    public static double getRightMotorSpeed() {
        return ((robotMap.rightFrontDrive.getSpeed() + robotMap.rightRearDrive.getSpeed()) / 2);
    }

    /**
     * Aligns the drivetrain to a given center pixel of a target. Meant to be run iteratively
     *
     * @param targetCenterPix The center pixel of the target
     * @param turnInPlace     If true the robot will turn in place. Will move forward towards target if false
     */
    public static void align(int targetCenterPix, boolean turnInPlace) {
        int cameraCenterPix = robotMap.cameraResolution / 2;
        if (!turnInPlace) {
            if (targetCenterPix + 5 > cameraCenterPix) {
                setRightMotorSpeed(.15);
                setLeftMotorSpeed(.35);
            } else if (targetCenterPix - 5 < cameraCenterPix) {
                setRightMotorSpeed(.35);
                setLeftMotorSpeed(.15);

            }
        } else {
            //error = center - setpoint
            //P*error
            //trying to make error 0
            if (targetCenterPix > cameraCenterPix) {
                setRightMotorSpeed(-.35);
                setLeftMotorSpeed(.35);
            } else if (targetCenterPix < cameraCenterPix) {
                setRightMotorSpeed(.35);
                setLeftMotorSpeed(-.35);
            }
        }
    }
}
