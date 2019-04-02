package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * Creates a PID loop for the pivoting cargo arm.
 */
public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean terminate = true;
    private static double integral, error = 0;


    //Be careful setting the proportional or integral constants > 1
    //This can cause the manipulator to violently flop and potentially
    //damage itself
    private static final double P = 0.8; //Proportional Constant
    private static final double I = 0.08; //Integrator Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    /**
     * Is called by teleopPeriodic. Handles iterative logic for the arm.
     */
    public static void iterate() {
        PI(); //Calculate control loop values
        if(setpoint < 10) {
            setpoint = 10; //Make sure the manipulator doesn't go *all* the way back, preventing the ball from being pushed out
        }
        robotMap.cargoPivotOne.set(drive); //Drive pivot one based on the PI values
        robotMap.cargoPivotTwo.set(drive); //Drive pivot two based on the PI values
    }

    /**
     * Accessor for the setpoint.
     * @return The current setpoint
     */
    public static double getSetpoint() { return setpoint; }

    /**
     * Resets the current setpoint and the encoder for the arm
     */
    public static void reset() {
        setpoint = 0;
        robotMap.encoderPivotTwo.reset();
    }

    /**
     * Mutator method for the setpoint
     * @param down Whether to set the setpoint to go down (if true, it will attempt to go down)
     */
    public static void setSetpoint(boolean down) {
        if(down && setpoint < 250) { //If we want to go down AND we are not all the way down
            setpoint += 60; //Go down by 60 encoder ticks
        } else if (!down && setpoint > 0) { //If we want to go up AND we are not all the way up
            setpoint -= 60; //Go up by 60 encoder ticks
        }
    }

    /**
     * Runs the calculations for the PI loop based on the
     * current setpoint and the current encoder value
     */
    private static void PI() {

        //PI = P * error + I * (previous error)
        //Where P and I are constants and error is the difference between the setpoint and the current position

        error = setpoint - robotMap.encoderPivotTwo.get(); //Set error to the difference of the setpoint and the current position
        integral += error*.02; //Calculate integral sum
        if(integral > integrator_limit) { //If the integral is too high...
            integral = integrator_limit; //Set it to the intergrator limit
        } else if(integral < -integrator_limit) { //Else if the integral is too low...
            integral = -integrator_limit; //Set it to -integrator
        }
        drive = (P * error + I * integral) / 100.0; //Calculate the PI loop based on the above equation
        if(drive > 0.2) { //If we want to go forward too fast...
            drive = .2; //...limit it to 20% power
        } else if (drive < -.8) { //Else we want to go backwards too fast...
            drive = -.6; //...limit it to -60% power
        }
    }
}
