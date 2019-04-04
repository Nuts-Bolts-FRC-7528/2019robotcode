package frc.robot.components;

import frc.robot.common.OI;
import frc.robot.common.robotMap;

import static frc.robot.Robot.toggleOn;

/**
 * Creates a PID loop for the pivoting cargo arm.
 */
public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean terminate = true;
    private static double integral, error, derivative = 0;
    private static double previousError = 0; //Derivative is based on finding the slope of our function.
    //Equation is (x2 - x1)/(y2 - y1) to find slope. In this case, teleopPeriodic is iterative so we only worry about the
    //x part of the equation

    //Be careful setting the proportional or integral constants > 1
    //This can cause the manipulator to violently flop and potentially
    //damage itself
    private static final double P = 0.15; //Proportional Constant
    private static final double I = 0.1; //Integrator Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup
    private static final double D = 0.25; //Derivative Constant

    /**
     * Is called by teleopPeriodic. Handles iterative logic for the arm.
     */
    public static void iterate() {
        PI(); //Calculate control loop values
        if (setpoint < 10) {
            setpoint = 10; //Make sure the manipulator doesn't go *all* the way back, preventing the ball from being pushed out
        }
        robotMap.cargoPivotOne.set(upOrDown(drive)); //Drive pivot one based on the PI values
        robotMap.cargoPivotTwo.set(upOrDown(drive)); //Drive pivot two based on the PI values
        System.out.println("************************");
        System.out.println("THE WINNING NUMBER IS:\n" + drive);
        System.out.println("Encoder1: " + robotMap.encoderPivotOne.get());
        System.out.println("Encoder2: " + robotMap.encoderPivotTwo.get());
        System.out.println("Setpoint is " + getSetpoint());
    }

    /**
     * Mutator for drive
     *
     * @param drive
     * @return mutated drive depending on up or down
     * We want it to go slower on the down
     * and faster on the up
     */


    public static double upOrDown(double drive) {
        if (drive > 0)
            drive *= .6; //0.3; //Sets drive lower if the maniuplator is going down (compensates for gravity)
        else if (drive < 0)
            drive *= 1.0; //Sets drive higher if the manipulator is going up
        if (drive < 0.25 && drive > 0) // roughly the minimum amount for motor movement
            drive = 0.25;
        return drive;
    }

    /**
     * Accessor for the setpoint.
     *
     * @return The current setpoint
     */
    public static double getSetpoint() {
        return setpoint;
    }

    /**
     * Resets the current setpoint and the encoder for the arm
     */
    public static void reset() {
        setpoint = 0;
        robotMap.encoderPivotTwo.reset();
    }

    /**
     * Mutator method for the setpoint
     *
     * @param down Whether to set the setpoint to go down (if true, it will attempt to go down)
     */
    public static void setSetpoint(boolean down) {
         int set = 480;
        if (down && setpoint < 520) { //If we want to go down AND we are not all the way down
            setpoint += set; //Go down by 60 encoder ticks
            if ( setpoint > 520)
                setpoint = 520;
        } else if (!down && setpoint > 0) { //If we want to go up AND we are not all the way up
            setpoint -= set; //Go up by 60 encoder ticks
            if (setpoint < 0)
                setpoint = 0;
        }
    }

    /**
     * Runs the calculations for the PI loop based on the
     * current setpoint and the current encoder value
     */
    private static void PI() {

        //PI = P * error + I * (previous error)
        //Where P and I are constants and error is the difference between the setpoint and the current position
//        previousValue = currentValue; //sets previousValue equal to the old currentValue
//        currentValue = robotMap.encoderPivotTwo.get(); //Sets a new currentValue equal to encoder input
//        derivative = currentValue - previousValue; //Sets derivative equal to change in last tick and current tick

        previousError = error;
        error = setpoint - robotMap.encoderPivotTwo.get(); //Set error to the difference of the setpoint and the current position
        derivative = error - previousError;

        integral += error * .02; //Calculate integral sum
        if (integral > integrator_limit) { //If the integral is too high...
            integral = integrator_limit; //Set it to the intergrator limit
        } else if (integral < -integrator_limit) { //Else if the integral is too low...
            integral = -integrator_limit; //Set it to -integrator
        }
        drive = (P * error + I * integral + D * derivative) / 100.0; //Calculate the PI loop based on the above equation
        if (drive > 0.8) { //If we want to go forward too fast...
            drive = .2; //...limit it to 20% power
        } else if (drive < -.8) { //Else we want to go backwards too fast...
            drive = -.8; //...limit it to -80% power
        }
    }

    public static void updateToggle()
    {
        if(OI.manipulatorController.getAButtonPressed()) {
            if (!frc.robot.Robot.togglePressed) {
                toggleOn = !toggleOn;
                frc.robot.Robot.togglePressed = true;
            }
        }else{
            frc.robot.Robot.togglePressed = false;
        }

    }
}
