package frc.robot.components;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.Robot;
import frc.robot.common.robotMap;

import static frc.robot.Robot.pistonExtended;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    private static int goal = 0; //Determines the desired height of the elevator

    private static double setpoint, error, integral, drive, derivative, previousError = 0;

    private static final double P = 0.2; //Proportional Constant
    private static final double I = 0.1; //Integrator Constant
    private static final double D = 1; //Derivative Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    public static boolean dRightPressed = false;
    public static boolean dLeftPressed = false;
    private static int retractionTimer = 0;
    private static int extensionTimer = 0;
    public static boolean hatchOrCargo = false;

    /**
     *     Resets the level and goal in teleopInit
     *    so that everything works correctly when the robot is started
     **/

    public static void reset() {
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
        if ((!dLeftPressed && retractionTimer == 0) || (!dRightPressed && extensionTimer == 0)) {
            setSetpoint(); //Ensures that the setpoint is where we want it when Y has not been pressed and its method is completed
        }
        PI(); // Runs control loop

        robotMap.elevator.set(ControlMode.PercentOutput, -drive); // Engages the elevator motor (Because of its positioning, negative makes the elevator go up)

        //Print methods
//        System.out.println("\n\n*******************************");
//        System.out.println("\nElevator drive:  " + drive);
//        System.out.println("\nElevator is at:  " + robotMap.elevatorEncoder.get());
//        System.out.println("\nElevator Setpoint:  " + setpoint);
//        System.out.println("\nElevator Goal:  " + goal);
    }

    /**
     * Based on the current goal level, with the available options being levels 0, 1, 2, and 3, gets a particular setpoint to be at.
     */
    private static void setSetpoint() {
        if (!hatchOrCargo) { //Set points for hatch height
            if (goal == 0) { //Sets desired level to 0
                setpoint = 0; //Ticks at level 0
            } else if (goal == 1) { //Sets desired level to 0
                setpoint = 1010; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 1
                setpoint = 4600; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 2
                setpoint = 7810; //Ticks at level 3
            }
        }
        else{ //Setpoints for ball height
            if (goal == 0) { //Sets desired level to 0
                setpoint = 0; //Ticks at level 0
            } else if (goal == 1) { //Sets desired level to 0
                setpoint = 850; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 1
                setpoint = 4450; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 2
                setpoint = 7650; //Ticks at level 3
            }
        }
    }

    /**
     * Sets the setPoint lower by 129 ticks for hatch placement. Since the hatch mechanism gets caught on screws on
     * the mechanism, we need to lower the elevator by a little bit in order to be able to retract the hatch mechanism
     */
    public static void subSetpoint() {
        setpoint -= 300; //Makes the elevator go down before retraction
    }

    public static void superSetpoint(){
        setpoint += 1000;
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
            [RETRACTION & EXTENSION]
     */

    public static void dLeftIsPressed() {
        //Print Statements for testing
//        System.out.println("dLeftPressed:  " + dLeftPressed);
//        System.out.println("\nrectractionTimer:  " + retractionTimer);
        //When Y is Pressed, a Timer is created with a maximum of 140 ticks and the following checks will be activated
        if (dLeftPressed && retractionTimer < 90) {
            retractionTimer++; // Increases Timer (In teleopPeriodic)
            if (retractionTimer == 5) { //@ 5 ticks
                Elevator.subSetpoint(); // Subtracts the setpoint (currently @ -300)
            }
            if (retractionTimer == 20) { //@ 20 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); // Withdraws Wings
            }
            if (retractionTimer == 40) { //@ 40 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); // Pulls in Main Base
            }
            if (retractionTimer == 60) { //@ 60 ticks
                setGoal(0); // Sets Goal to 0, telling the elevator to go to the bottom
            }
            if (retractionTimer == 80) { //@80 ticks
                Robot.pistonExtended = false; //Unlocks cargo manipulator after task is complete
            }
        } else {
            dLeftPressed = false; //Sets yPressed to false ( turns off the method)
            retractionTimer = 0; // resets Timer for next iteration

        }
    }
    public static void dRightIsPressed(){
        if(dRightPressed && extensionTimer < 200){
            extensionTimer++;
            if (extensionTimer == 5) { //@ 5 ticks
                Elevator.setGoal(1); //Boosts the elevator to level 1
            }
            if (extensionTimer == 60) { //@ 60 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); // Extends Hatch Base
            }
            if (extensionTimer == 80) { //@ 80 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); // Extends wings
            }
            if (extensionTimer == 110) { //@ 110 ticks
                Elevator.superSetpoint(); // Adds the setpoint (currently @ +450)
            }
            
        } else {
            dRightPressed = false; //Sets yPressed to false ( turns off the method)
            extensionTimer = 0; // resets Timer for next iteration
            
        }
    }
}