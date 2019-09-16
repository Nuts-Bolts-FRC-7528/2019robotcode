package frc.robot.components;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.Robot;
import frc.robot.common.robotMap;
import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Provides an automatic control loop for the Elevator.
 *<br>
 *     <b>NOTE:</b> Because the elevator motor is flipped backwards, you actually set
 *     the motor in <i>reverse</i> when you want to go upwards, and vice versa.
 */
public class Elevator {
    private static int goal = 0; //Determines the desired height of the elevator

    private static double error, integral, drive, derivative, previousError = 0;
    public static double setpoint = 0; //Setpoint for PID loop

    private static final double P = 0.2; //Proportional Constant, optimal 4/10 : 0.2
    private static final double I = 0.1; //Integrator Constant, optimal 4/10 : 0.1
    private static final double D = 1; //Derivative Constant, optimal 4/10 : 1
    //TODO: Reevaluate whether we need the derivative or can suffice using just PI
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    private static boolean dRightPressed = false; //Set to true when right is pressed on the d-pad
    private static boolean dLeftPressed = false; //Set to true when left is pressed on the d-pad
    private static int dLeftTimer = 0; //Timer for the action that fires when d-pad left is pressed
    private static int dRightTimer = 0; //Timer for the action that fires when d-pad right is pressed
    private static boolean startPressed = false; //Is set to true when start is pressed
    private static int startTimer = 0;

    private static boolean isHatchMode = false; //If false, elevator will go to setpoints for placing a ball on the rocket
    //If isHatchMode is true, elevator will go to setpoints for placing a hatch on the rocket

    /**
     * Resets the elevator goal levels and encoder
     **/
    public static void reset() {
        goal = -1;
        robotMap.elevatorEncoder.reset();
        robotMap.elevator.set(ControlMode.PercentOutput, 0);
    }

    /**
     * Runs iteratively in teleopPeriodic() and in update() of actions
     */
    public static void iterate() {
        /*   [CONTROL LISTENERS]   */
        //Iterate methods that listen to a certain button

        startIsPressed();
        dLeftIsPressed();
        dRightIsPressed();

        /*   [SETTING HATCH / BALL MODE]   */
        //Depending on whether or not the cargo manipulator is holding a ball we'll switch between hatch or ball mode

        if(CargoCatch.getManipulatorState() == CargoCatch.CargoManipulatorState.HOLDING_BALL) { //If the cargo manipulator is holding a ball...
            isHatchMode = false; //Turn off hatch mode (since obviously we don't want to be putting hatches in if there's a ball being held)
        } else if(CargoCatch.getManipulatorState() == CargoCatch.CargoManipulatorState.NEUTRAL) { //If the cargo manipulator is neutral...
            isHatchMode = true; //Turn on hatch mode (assume we're holding a hatch if we aren't holding a ball)
        }

        /*   [VALUE CHECKING]   */
        //These next few lines make sure all the values are right

        if (goal > 3 && goal != 4) { //Checks if goal is higher than it should be
            goal = 3; //If it is, reset to highest possible level unless we want a height exception
        } else if (goal < 0 && goal != -1) { //Checks if goal is lower than it should be
            goal = 0; //If it is, reset to lowest possible level
        }
        setSetpoint();

        PID(); // Runs control loop
        if (goal <= 0 && robotMap.elevatorEncoder.get() < 200) { //If the elevator is on the ground and we don't want to move the elevator...
            setGoal(-1); //Sets goal to -1 (leaving it unaffected by setSetpoint())
            robotMap.elevator.set(ControlMode.PercentOutput, 0); //Turn off elevator motor to save heat
        } else { //If we do want to be moving the elevator...
            robotMap.elevator.set(ControlMode.PercentOutput, -drive); // Engages the elevator motor
            //Bear in mind the fact that the elevator motor is reversed
        }

//        [Print methods]
//        System.out.println("\n\n*******************************");
//        System.out.println("\nElevator drive:  " + drive);
//        System.out.println("\nElevator is at:  " + robotMap.elevatorEncoder.get());
//        System.out.println("\nElevator Setpoint:  " + setpoint);
//        System.out.println("\nElevator Goal:  " + goal);
//        System.out.println("\nElevator " + robotMap.elevator.getMotorOutputPercent());
//        if (isHatchMode) {
//            System.out.println("Elevator Mode: BALL");
//        } else {
//            System.out.println("Elevator Mode: HATCH");
//        }
    }

    /**
     * Based on the current goal level, with the available options being levels 0, 1, 2, and 3, gets a particular setpoint to be at.
     */
    private static void setSetpoint() {
        //The setpoint levels are derived from measurement and a little bit of guess+check on our part
        if (!isHatchMode) { //Set points for hatch height
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
        //Setpoint for Cargo ship
        if (goal == 4) {
            setpoint = 2500;
        }
    }

    /**
     * Sets the setPoint lower by 300 ticks for hatch placement.
     */
    private static void subSetpoint() {
        setpoint -= 300; //Makes the elevator go down before retraction by 300 encoder ticks
    }

    /**
     * Mutator for the goal level.
     *
     * @param height The desired goal level. Valid ranges are 1-4
     */
    public static void setGoal(int height) {
        if (height >= 0 && height <= 4) { //Checks if goal is between 0 and 3 inclusive
            goal = height; //Sets goal equal to the input level
        }
    }


    /**
     * Runs the calculations for the PID loop based on the
     * current setpoint and the current encoder value
     */
    private static void PID() {

        //PID = P * error + I * D * derivative
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

    private static void dLeftIsPressed() { //Method for retraction of hatch manipulator
        //When dLeft is Pressed, a Timer is started with a maximum of 90 ticks and the following steps will be activated
        if (dLeftPressed && dLeftTimer < 90) {
            dLeftTimer++; // Increases Timer (In teleopPeriodic)
            if (dLeftTimer == 5) { //@ 5 ticks
                Elevator.subSetpoint(); // Subtracts the setpoint (currently @ -300)
            }
            if (dLeftTimer == 20) { //@ 20 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); // Withdraws Wings
            }
            if (dLeftTimer == 40) { //@ 40 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); // Pulls in Main Base
            }
            if (dLeftTimer == 60) { //@ 60 ticks
                setGoal(0); // Sets Goal to 0, telling the elevator to go to the bottom
            }

        } else {
            dLeftPressed = false; //Sets dLeftPressed to false (Ending the method)
            dLeftTimer = 0; //Resets retractionTimer for the next use
        }
    }

    private static void dRightIsPressed() { //Method for grabbing hatches from the feeder station
        if (dRightPressed && dRightTimer < 180) { //Checks if dRightPressed is true and time is leses than 7.5 seconds
            dRightTimer++;
            if (dRightTimer == 10) { //@ 10 ticks
                setpoint = 920;
            }
            if (dRightTimer == 40) { //@40 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); // Extends Hatch Base
            }

            if (dRightTimer == 60) { //@ 60 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); // Extends wings
            }
            if (dRightTimer == 120) { //@ 120 ticks
                setpoint = 1500;
            }
        } else {
            dRightPressed = false; //Sets dRightPressed to false ( turns off the method)
            dRightTimer = 0; // resets extensionTimer for next iteration
        }
    }

    private static void startIsPressed() { //Binds actions to the start button
        //When start is pressed, the elevator will go to level one, deploy the hatch manip, push it out, and go back to lvl 0
        if (startPressed && startTimer < 75) { //Checks if startPressed is true and startTimer is under 100 ticks
            startTimer++; //Increments startTimer each iteration of code
            if (startTimer == 10) { //@ 10 ticks
                Elevator.setGoal(1); //Set the elevator to level one
            }
            if (startTimer == 30) { //@ 30 ticks
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //Pushes wings out
            }
            if (startTimer == 50) { //@ 60 ticks
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); //Pushes hatch mechanism out }
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