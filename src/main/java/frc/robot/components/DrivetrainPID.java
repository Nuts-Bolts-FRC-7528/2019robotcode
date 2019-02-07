package frc.robot.components;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import frc.robot.common.robotMap;

public class DrivetrainPID extends PIDSubsystem {

    /**
     * Instantiates a new DrivetrainPID object that extends PIDSubsystem
     */
    public DrivetrainPID() {
        super("DrivetrainPID",1.0,0.0,0.0,12/6000.0);
        setAbsoluteTolerance(0.05);
    }

    @Override
    public void initDefaultCommand() {}

    @Override
    public double returnPIDInput() {
        return robotMap.enc.get();
    }

    @Override
    public void usePIDOutput(double output) {
        pidSetMotorSpeeds(output, output);
    }

    /**
     * PID Writes to all drivetrain motors
     * @param leftSpeed PID signal being sent to left drivetrain motors
     * @param rightSpeed PID signal being sent to right drivetrain motors
     */
    private static void pidSetMotorSpeeds(double leftSpeed, double rightSpeed) {
        robotMap.rightFrontDrive.pidWrite(leftSpeed);
        robotMap.leftFrontDrive.pidWrite(rightSpeed);
    }

    /**
     * Sets the speed for the left drivetrain motors
     * @param speed PWM signal, from either -1.0 (full reverse) to 1.0 (full forward)
     */
    public static void setLeftMotorSpeed(double speed) {
        robotMap.leftFrontDrive.set(speed);
        robotMap.leftRearDrive.set(speed);
    }

    /**
     * Sets the speed for the right drivetrain motors
     * @param speed PWM signal, from either -1.0 (full reverse) to 1.0 (full forward)
     */
    public static void setRightMotorSpeed(double speed) {
        robotMap.rightFrontDrive.set(speed);
        robotMap.rightRearDrive.set(speed);
    }

    /**
     * Returns the average speed for left drivetrain motors
     * @return Average of all PWM signals being currently sent to all left drives
     */
    public static double getLeftMotorSpeed() {
        return((robotMap.leftFrontDrive.getSpeed()+robotMap.leftRearDrive.getSpeed())/2);
    }

    /**
     * Returns the average speed for right drivetrain motors
     * @return Average of all PWM signals currently being sent to all right drives
     */
    public static double getRightMotorSpeed() {
        return((robotMap.rightFrontDrive.getSpeed()+robotMap.rightRearDrive.getSpeed())/2);
    }

    /**
     * Turns left at half speed until color sensors are aligned with line
     * Particularly useful for aligning to the lines on the field
     * @param color Which color to turn to, either white, blue, red, or green
     */
    public static void turnLeftToLine(String color) {
        boolean abort = false; //If we have to abort, this variable gets set to true
        double c = Timer.getFPGATimestamp(); //Sets counter variable to FPGA time as of starting the command
        if(color.equalsIgnoreCase("blue")) {
            while(!(robotMap.colorA.blue < 340 && robotMap.colorA.blue > 300)) { //While front color sensor is NOT over line
                setLeftMotorSpeed(.5);
                setRightMotorSpeed(.25);
                System.out.println (c);
                if((Timer.getFPGATimestamp()-c) > 4.00) { //If after 4 seconds, the color sensor has not found the line
                    System.out.println (Timer.getFPGATimestamp()-c);
                    abort = true; //Set abort to true (make the rest of the command not happen)
                    break; //Stop the loop
                }
            }
            c = Timer.getFPGATimestamp(); //Reset the counter variable
            if(!abort) { //If we didn't have to abort (the front color sensor found the line)
                while(!(robotMap.colorB.blue < 340 && robotMap.colorB.blue > 300)) { //While back color sensor is NOT over line
                    setLeftMotorSpeed(.5);
                    setRightMotorSpeed(.25); //Keep going from earlier
                    if(Timer.getMatchTime() - c > 4) { //If the back color sensor isn't over the line after 4 seconds
                        abort = true; //Set abort to true (make the rest of the command not happen)
                        break; //Stop the loop
                    }
                }
            }

            c = Timer.getMatchTime(); //Reset the counter variable again
            if(!abort) { //If we didn't have to abort (the back color sensor found the line)
                while(!(robotMap.colorA.blue < 340 && robotMap.colorA.blue > 300) && !(robotMap.colorB.blue < 340 && robotMap.colorB.blue > 300)) {
                    setRightMotorSpeed(.5);
                    setLeftMotorSpeed(-.5);

                    if(Timer.getMatchTime() - c > 4) { //If both sensors don't find the line
                        break; //Stop the loop (this is the last thing so we don't have to change abort)
                    }
                }
            } 
        }
    }
}
