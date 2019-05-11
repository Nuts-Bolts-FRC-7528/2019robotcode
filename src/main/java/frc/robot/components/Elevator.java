package frc.robot.components;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.Robot;
import frc.robot.common.robotMap;
import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    public static int goal = 0; //Determines the desired height of the elevator

    private static double error, integral, drive, derivative, previousError = 0;
    public static double setpoint = 0;

    private static final double P = 0.2; //Proportional Constant, optimal 4/10 : 0.2
    private static final double I = 0.1; //Integrator Constant, optimal 4/10 : 0.1
    private static final double D = 1; //Derivative Constant, optimal 4/10 : 1
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    public static boolean dRightPressed = false;
    public static boolean dLeftPressed = false;
    private static int retractionTimer = 0;
    private static int extensionTimer = 0;
    public static boolean hatchOrCargo = false; //Boolean for changing the elevator setPoint based on scoring hatch or ball
    public static boolean startPressed = false;
    private static int startTimer = 0;

    /**
     * Resets the level and goal in teleopInit
     * so that everything works correctly when the robot is started
     **/

    public static void reset() {
        goal = -1;
        robotMap.elevatorEncoder.reset();
        robotMap.elevator.set(ControlMode.PercentOutput, 0);
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
//        System.out.println("Integrator is: " + integral);
        if (goal > 3 && goal != 4) { //Checks if goal is higher than it should be
            goal = 3; //If it is, reset to highest possible level unless we want a height exception
        } else if (goal < 0 && goal != -1) { //Checks if goal is lower than it should be
            goal = 0; //If it is, reset to lowest possible level
        }
        if ((!dLeftPressed && retractionTimer == 0) && (!dRightPressed && extensionTimer == 0)) {
            setSetpoint(); //Ensures that the setpoint is where we want it when dLeft AND dRight has not been pressed and its' methods is completed
        }

        PI(); // Runs control loop
        if (goal <= 0 && robotMap.elevatorEncoder.get() < 200) {
            setGoal(-1);
            robotMap.elevator.set(ControlMode.PercentOutput, 0);
        } else {
            robotMap.elevator.set(ControlMode.PercentOutput, -drive); // Engages the elevator motor (Because of its positioning, negative makes the elevator go up)
        }

//        Print methods
        System.out.println("\n\n*******************************");
        System.out.println("\nElevator drive:  " + drive);
        System.out.println("\nElevator is at:  " + robotMap.elevatorEncoder.get());
        System.out.println("\nElevator Setpoint:  " + setpoint);
        System.out.println("\nElevator Goal:  " + goal);
        System.out.println("\nElevator " + robotMap.elevator.getMotorOutputPercent());
        if (hatchOrCargo) {
            System.out.println("BALL        BALL");
        } else {
            System.out.println("HATCH       HATCH");
        }
//        END of print methods
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
            } else if (goal == 1) { //Sets desired level to 1
                setpoint = 1110; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 2
                setpoint = 4700; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 2
                setpoint = 7900;
            }
        } else { //Setpoints for ball height
            if (goal == 0) { //Sets desired level to 0
                setpoint = 0; //Ticks at level 0
            } else if (goal == 1) { //Sets desired level to 1
                setpoint = 700; //Ticks at level 1
            } else if (goal == 2) { //Sets desired level to 2
                setpoint = 4300; //Ticks at level 2
            } else if (goal == 3) { //Sets desired level to 3
                setpoint = 7700; //Ticks at level 3
            }
        }
        //NOT TESTED
        if (goal == 5) {
            setpoint = 2500; //rough average for cargo ship NOT TESTED
        }
    }

    /**
     * Sets the setPoint lower by 129 ticks for hatch placement. Since the hatch mechanism gets caught on screws on
     * the mechanism, we need to lower the elevator by a little bit in order to be able to retract the hatch mechanism
     */
    public static void subSetpoint() {
        setpoint -= 300; //Makes the elevator go down before retraction by 300 encoder ticks
    }

    public static void superSetpoint() {
        setpoint += 300; //Makes the elevator go up after extension by 300 encoder ticks
    }

    /**
     * Mutator for the goal level.
     *
     * @param height The desired goal level. Valid ranges are 1-4
     */
    public static void setGoal(int height) {
        if (height < 6 && height > -1) { //Checks if goal is between 0 and 3 inclusive
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

    public static void dLeftIsPressed() { //Method for retraction of hatch manipulator
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

        } else {
            dLeftPressed = false; //Sets dLeftPressed to false ( turns off the method)
            retractionTimer = 0; // resets retractionTimer for next iteration

        }
    }

    public static void dRightIsPressed() { //Method for grabbing hatches from the feeder station
        if (dRightPressed && extensionTimer < 180) { //Checks if dRightPressed is true and time is leses than 7.5 seconds
            extensionTimer++;
            if (extensionTimer == 10) { //@ 10 ticks
                setpoint = 920;
            }
            if (extensionTimer == 40) { //@40 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); // Extends Hatch Base
            }

            if (extensionTimer == 60) { //@ 60 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); // Extends wings
            }
            if (extensionTimer == 120) { //@ 120 ticks
                setpoint = 1500;
            }
        } else {
            dRightPressed = false; //Sets dRightPressed to false ( turns off the method)
            extensionTimer = 0; // resets extensionTimer for next iteration
        }
    }

    public static void startIsPressed() { //Method for start of the match hatch pickup
        if (startPressed && startTimer < 75) { //Checks if startPressed is true and startTimer is under 100 ticks
            startTimer++; //Increments startTimer each iteration of code
            if (startTimer == 10) { //@ 10 ticks
                Elevator.setGoal(1); //Set the elevator to level one
            }
            if (startTimer == 30) { //@ 30 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //Pushes wings out
            }
            if (startTimer == 50) { //@ 60 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); //Pushes hatch mechanism out
            }
            if (retractionTimer == 80) { //@80 ticks
                Robot.pistonExtended = false; //Unlocks cargo manipulator after task is complete
            }
            if (startTimer == 65){
                Elevator.setGoal(0);
            }
        } else {
            startPressed = false; //Sets startPressed to false (turns off method)
            startTimer = 0; //Resets startTimer for next iteration
        }
    }
}