package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    private static int level = 1;
    private static int goal = 1;

    private static double setpoint, error, integral, drive, derivative, previousError = 0;

    private static final double P = 0.1; //Proportional Constant
    private static final double I = 0; //Integrator Constant
    private static final double D = 0; //Derivative Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    public static void reset() {
        level = 1;
        goal = 1;
        robotMap.elevatorEncoder.reset();
    }

    /**
     * Runs iteratively in teleopPeriodic() and in update() of actions
     * Note that the Elevator runs forward when going down and backwards
     * when going up - this is because the Elevator motor is flipped
     * backwards.
     */
    public static void iterate() {
        if(goal > 3) { //Checks if goal is higher than it should be
            goal = 3; //If it is, reset to highest possible level
        } else if (goal < 1) { //Checks if goal is lower than it should be
            goal = 1; //If it is, reset to lowest possible level
        }
        setSetpoint();
        PI();

//        robotMap.elevator.set(drive);
        System.out.println("elevator drive" + drive);
    }

    /**
     * Based on the current goal level, gets a particular setpoint to be at.
     */
    private static void setSetpoint() {
        if(goal == 1) {
            setpoint = 1010;
        } else if (goal == 2) {
            setpoint = 4745;
        } else if (goal == 3) {
            setpoint = 7810;
        }
    }

    /**
     * Mutator for the goal level.
     * @param height The desired goal level. Valid ranges are 1-3
     */
    public static void setGoal(int height){
        if(height < 4 && height > 0){ //Checks if goal is between 1 and 3 inclusive
            goal = height; //Sets goal equal to the input level
        }
    }

    /**
     * Public accessor method for the current level of the Elevator
     *
     * @return The current level of the Elevator
     */
    public static int getLevel() { return level; }

    /**
     * Runs the calculations for the PI loop based on the
     * current setpoint and the current encoder value
     */
    private static void PI() {

        //PI = P * error + I * (previous error)
        //Where P and I are constants and error is the difference between the setpoint and the current position
        previousError = error;
        error = setpoint - robotMap.elevatorEncoder.get(); //Set error to the difference of the setpoint and the current position
        derivative = error - previousError;

        integral += error*.02; //Calculate the integral sum
        if(integral > integrator_limit) { //If the integral is too high...
            integral = integrator_limit; //Set it to the integrator limit
        } else if(integral < -integrator_limit) { //Else if the integral is too low...
            integral = -integrator_limit; //...Set it to -integrator limit
        }
        drive = (P * error + I * integral + D * derivative) / 100.0; //Calculate the PI loop based on the above equation
        if(drive > 0.6) { //If we want to go up too fast...
            drive = .6; //...limit it to 60% power
        } else if (drive < -.3) { //If we want to go down too fast...
            drive = -.3; //...limit it to -30% power
        }
    }
}