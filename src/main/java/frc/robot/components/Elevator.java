package frc.robot.components;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.Robot;
import frc.robot.common.robotMap;
import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    private static int goal = 0; //Determines the desired height of the elevator

    private static double setpoint, error, integral, drive, derivative, previousError = 0;

    private static final double P = 0.2; //Proportional Constant, optimal 4/10 : 0.2
    private static final double I = 0.1; //Integrator Constant, optimal 4/10 : 0.1
    private static final double D = 1; //Derivative Constant, optimal 4/10 : 1
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    public static boolean dRightPressed = false;
    public static boolean dLeftPressed = false;
    private static int retractionTimer = 0;
    private static int extensionTimer = 0;
    public static boolean hatchOrCargo = false; //Boolean for changing the elevator setPoint based on scoring hatch or ball

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
        if ((!dLeftPressed && retractionTimer == 0) && (!dRightPressed && extensionTimer == 0)) {
            setSetpoint(); //Ensures that the setpoint is where we want it when dLeft AND dRight has not been pressed and its' methods is completed
        }
        else if(dRightPressed && extensionTimer < 40){ //If right on the d-pad is pressed, within the first 2 seconds...
            setSetpoint(); //Allows setSetopoint to work, which brings the elevator up to level one
        }
        PI(); // Runs control loop

        robotMap.elevator.set(ControlMode.PercentOutput, -drive); // Engages the elevator motor (Because of its positioning, negative makes the elevator go up)

//        Print methods
        System.out.println("\n\n*******************************");
        System.out.println("\nElevator drive:  " + drive);
        System.out.println("\nElevator is at:  " + robotMap.elevatorEncoder.get());
        System.out.println("\nElevator Setpoint:  " + setpoint);
        System.out.println("\nElevator Goal:  " + goal);
        if(hatchOrCargo){
            System.out.println("BALL        BALL");
        }
        else{
            System.out.println("HATCH       HATCH");
        }
    }

    /**
     * Based on the current goal level, with the available options being levels 0, 1, 2, and 3, gets a particular setpoint to be at.
     */
    //CURRENT ELEVATOR DESIRED RAW ENCODER VALUE VS ENCODER VALUE WITH PID
    // IS MINUS 100 FROM DESIRED VALUE
    //To fix this, added 100 to ALL setpoints
    private static void setSetpoint() {
        if (!hatchOrCargo) { //Set points for hatch height
            if (goal == 0) { //Sets desired level to 0
                setpoint = 0; //Ticks at level 0
            } else if (goal == 1) { //Sets desired level to 0
                setpoint = 1110; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 1
                setpoint = 4700; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 2
            }
        }
        else{ //Setpoints for ball height
            if (goal == 0) { //Sets desired level to 0
                setpoint = 0; //Ticks at level 0
            } else if (goal == 1) { //Sets desired level to 0
                setpoint = 700; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 1
                setpoint = 4300; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 2
                setpoint = 7400; //Ticks at level 3
            }
        }
    }

    /**
     * Sets the setPoint lower by 129 ticks for hatch placement. Since the hatch mechanism gets caught on screws on
     * the mechanism, we need to lower the elevator by a little bit in order to be able to retract the hatch mechanism
     */
    public static void subSetpoint() {
        setpoint -= 300; //Makes the elevator go down before retraction by 300 encoder ticks
    }

    public static void superSetpoint(){
        setpoint += 300; //Makes the elevator go up after extension by 300 encoder ticks
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

        //PI = P * error + I * D * derivative
        //Where P, I, and D are constants and error is the difference between the setpoint and the current position
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

        //CHANGE THESE FOR MAX MOTOR SPEED
        if (drive > 0.6) { //If we want to go up too fast...
            //ON THE UP
            drive = 0.6; //...limit it to 60% power
        } else if (drive < -.3) { //If we want to go down too fast...
            //ON THE DOWN
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
        //When dLeft is Pressed, a Timer is created with a maximum of 90 ticks and the following checks will be activated
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
            dLeftPressed = false; //Sets dLeftPressed to false ( turns off the method)
            retractionTimer = 0; // resets retractionTimer for next iteration

        }
    }
    public static void dRightIsPressed(){
        if(dRightPressed && extensionTimer < 150){ //Checks if dRightPressed is true and time is leses than 7.5 seconds
            extensionTimer++;
            if (extensionTimer == 5) { //@ 5 ticks
                Elevator.setGoal(1); //Boosts the elevator to level 1
            }
            if (extensionTimer == 15) { //@ 15 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); // Extends Hatch Base
            }
            if (extensionTimer == 100) { //@ 100 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); // Extends wings
            }
            if (extensionTimer == 120) { //@ 120 ticks
                Elevator.superSetpoint(); // Adds the setpoint (currently @ +300)
            }
            
        } else {
            dRightPressed = false; //Sets dRightPressed to false ( turns off the method)
            extensionTimer = 0; // resets extensionTimer for next iteration
            
        }
    }
}