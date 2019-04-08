package frc.robot.components;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.common.robotMap;

import static frc.robot.Robot.pistonExtended;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    private static int level = 1;
    private static int goal = 1;

    private static double setpoint, error, integral, drive, derivative, previousError = 0;

    private static final double P = 0.1; //Proportional Constant
    private static final double I = 0; //Integrator Constant
    private static final double D = 0.1; //Derivative Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    public static boolean yPressed = false;
    private static int retractionTimer = 0;

    /**
     *     Resets the level and goal in teleopInit
     *    so that everything works correctly when the robot is started
     **/

    public static void reset() {
        level = 0;
        goal = 0;
        robotMap.elevatorEncoder.reset();
    }

    public static double getElevatorDrive() {
        return drive;
    }

    /**
     * Runs iteratively in teleopPeriodic() and in update() of actions
     * Note that the Elevator runs forward when going down and backwards
     * when going up - this is because the Elevator motor is flipped
     * backwards.
     */
    public static void iterate() {
        if (goal > 3) { //Checks if goal is higher than it should be
            goal = 3; //If it is, reset to highest possible level
        } else if (goal < 0) { //Checks if goal is lower than it should be
            goal = 0; //If it is, reset to lowest possible level
        }
        if (!yPressed && retractionTimer == 0) {
            setSetpoint(); //Ensures that the setpoint is where we want it when Y has not been pressed and its method is completed
        }
        PI(); // Runs control loop

        robotMap.elevator.set(-drive); // Engages the elevator motor (Because of its positioning, negative makes the elevator go up)

        //Print methods
        System.out.println("\n\n*******************************");
        System.out.println("\nElevator drive:  " + drive);
        System.out.println("\nElevator is at:  " + robotMap.elevatorEncoder.get());
        System.out.println("\nElevator Setpoint:  " + setpoint);
        System.out.println("\nElevator Goal:  " + goal);
    }

    /**
     * Based on the current goal level, gets a particular setpoint to be at.
     */
    private static void setSetpoint() {
        if (goal == 0) {
            setpoint = 0;
        } else if (goal == 1) {
            setpoint = 1010;
        } else if (goal == 2) {
            setpoint = 4600;
        } else if (goal == 3) {
            setpoint = 7810;
        }
    }

    /**
     * Sets the setPoint lower by 129 ticks for hatch placement. Since the hatch mechanism gets caught on screws on
     * the mechanism, we need to lower the elevator by a little bit in order to be able to retract the hatch mechanism
     */
    public static void subSetpoint() {
        setpoint -= 300; //Makes the elevator go down before retraction
    }

    /**
     * Mutator for the goal level.
     *
     * @param height The desired goal level. Valid ranges are 1-4
     */
    public static void setGoal(int height) {
        if (height < 4 && height > -1) { //Checks if goal is between 0 and 3 inclusive
            goal = height; //Sets goal equal to the input level
        }
    }

    /**
     * Public accessor method for the current level of the Elevator
     *
     * @return The current level of the Elevator
     */
    public static int getLevel() {
        return level;
    }

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

        integral += error * .02; //Calculate the integral sum
        if (integral > integrator_limit) { //If the integral is too high...
            integral = integrator_limit; //Set it to the integrator limit
        } else if (integral < -integrator_limit) { //Else if the integral is too low...
            integral = -integrator_limit; //...Set it to -integrator limit
        }
        drive = (P * error + I * integral + D * derivative) / 100.0; //Calculate the PI loop based on the above equation
        if (drive > 0.4) { //If we want to go up too fast...
            drive = .4; //...limit it to 60% power
        } else if (drive < -.3) { //If we want to go down too fast...
            drive = -.3; //...limit it to -30% power
        }
    }

    /*
            [RETRACTION]
     */

    public static void yIsPressed() {
        //Print Statements for testing
        System.out.println("yPressed:  " + yPressed);
        System.out.println("\nrectractionTimer:  " + retractionTimer);
        //When Y is Pressed, a Timer is created with a maximum of 140 ticks and the following checks will be activated
        if (yPressed && retractionTimer < 140) {
            retractionTimer++; // Increases Timer (In teleopPeriodic)
            if (retractionTimer == 5) { //@ 5 ticks
                Elevator.subSetpoint(); // Subtracts the setpoint (currently @ -300)
            }
            if (retractionTimer == 40) { //@ 40 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); // Withdraws Wings
            }
            if (retractionTimer == 80) { //@ 80 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); // Pulls in Main Base
            }
            if (retractionTimer == 120) { //@ 120 ticks
                setGoal(0); // Sets Goal to 0, telling the elevator to go to the bottom
            }
//            if (retractionTimer == 140) { //@140 ticks
//                pistonExtended = false; //Unlocks cargo manipulator after task is complete
//            }
        } else {
            yPressed = false; //Sets yPressed to false ( turns off the method)
            retractionTimer = 0; // resets Timer for next iteration

        }
    }
}