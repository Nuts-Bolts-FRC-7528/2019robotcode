package frc.robot.components;

import com.ctre.phoenix.motorcontrol.ControlMode;
import frc.robot.common.OI;
import frc.robot.common.robotMap;

/**
 * Creates a PID loop for the pivoting cargo arm.
 */
public class CargoCatch {

    /**
     * This enum represents the different states the cargo manipulator may be in at any given time
     */
    public enum CargoManipulatorState {
        HOLDING_BALL, //The cargo manipulator is currently holding the ball (VERTICAL, INTAKE AT 20%)
        COLLECTING_BALL, //The cargo manipulator is extended at 90 deg trying to suck up the ball (HORIZONTAL, INTAKE AT 65%)
        EXPELLING_BALL, //The cargo manipulator is expelling the ball (VERTICAL, INTAKE AT -100%)
        EXTENDED, //The cargo manipulator is extended past neutral but not collecting a ball (aka 45 degree [sic] mode) (EXTENDED, INTAKE AT 0%)
        NEUTRAL //The cargo manipulator is in a neutral position (VERTICAL, INTAKE AT 0%)

    }

    private static CargoManipulatorState manipulatorState = CargoManipulatorState.NEUTRAL; //Contains the cargo manipulator's state
    public static boolean xPressed = false; //Initialize booleans for Intake Out mode
    private static int xTimer = 0; //Initialize timer

    private static double drive = 0;
    private static double setpoint = 0;
    private static double integral, error, derivative = 0;
    private static double previousError = 0; //Derivative is based on finding the slope of our function.
    //Equation is (x2 - x1)/(y2 - y1) to find slope. In this case, teleopPeriodic is iterative so we only worry about the
    //x part of the equation

    //Be careful setting the proportional or integral constants > 1
    //This can cause the manipulator to violently flop and potentially
    //damage itself
    //OPTIMAL = DO NOT CHANGE EXCEPT FOR EMERGENCIES
    private static final double P = 0.22; //Proportional Constant, OPTIMAL 9/15 : 0.22
    private static final double I = 0.114; //Integrator Constant, OPTIMAL 9/15 : 0.114
    private static final double D = 0.7; //Derivative Constant, OPTIMAL 9/15 : 0.7
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    private static final double minSetpoint = 30; //Minimum Setpoint, OPTIMAL 4/10 : 30
    private static final double maxSetpoint = 520; //Maximum setpoint, OPTIMAL 9/15 520
    private static final double fortyFiveSetpoint = 250; //Setpoint for 45 degree mode

    /**
     * Called by teleopPeriodic() in the Robot class. Handles iterative logic for the manipulator.
     */
    public static void iterate() {
        PID(); //Run the PID loop once
        if (setpoint < minSetpoint) { //If setpoint is below the minimum...
            setpoint = minSetpoint; //...Set it to the minimum
        } else if (setpoint > maxSetpoint) { //Else if setpoint is above the maximum...
            setpoint = maxSetpoint; //...Set it to the maximum
        }
        robotMap.cargoPivotTwo.set(ControlMode.PercentOutput, drive); //Drive pivot one based off of the PID loop
        robotMap.cargoPivotOne.set(ControlMode.PercentOutput, drive); //Drive pivot two based off of the PID loop

        xPressed = OI.manipulatorController.getXButtonPressed(); //Bind X button listener to gamepad

        /*
                [PRINT STATEMENTS]
            Use for debugging
         */
//        System.out.println("************************");
//        System.out.println("DRIVE VALUE: " + drive);
//        System.out.println("Cargo Intake speed: " + robotMap.cargoIntake.get());
//        System.out.println("Current State: " + manipulatorState);
//        System.out.println("Encoder1:  " + robotMap.encoderPivotOne.get());
//        System.out.println("Encoder2:  " + robotMap.encoderPivotTwo.get());
//        System.out.println("Setpoint is:  " + getSetpoint());
//        System.out.println("setInMotorPickUp:  " + setInMotorPickUp);
//        System.out.println("setInMotorInBall:  " + setInMotorHolding);
//        System.out.println("pivotExtended:  " + frc.robot.Robot.pistonExtended);

        /*   [STATE BEHAVIORS]   */
        //Based on the state the manipulator is in it will do one of the following

        if (manipulatorState == CargoManipulatorState.COLLECTING_BALL) { //If we're trying to collect the ball
            robotMap.cargoIntake.set(0.65); //Set Intake to 70% power
            setpoint = maxSetpoint; //Set setpoint to maximum (90 degrees)
        } else if (manipulatorState == CargoManipulatorState.HOLDING_BALL) { //If we're in ball holding mode
            robotMap.cargoIntake.set(0.2); //Set intake to 20% power
            setpoint = minSetpoint; //Set setpoint to minimum (vertical)
        } else if (manipulatorState == CargoManipulatorState.NEUTRAL) { //If we're in neutral mode
            robotMap.cargoIntake.set(0); //Disengage intake motor
            setpoint = minSetpoint; //Set setpoint to minimum (vertical)
        } else if (manipulatorState == CargoManipulatorState.EXTENDED) { //If we're in 45 degree mode
            robotMap.cargoIntake.set(0); //Disengage intake motor
            setpoint = fortyFiveSetpoint; //Set setpoint to forty five degree angle
        } else if (manipulatorState == CargoManipulatorState.EXPELLING_BALL) { //If we're trying to expel the ball
            robotMap.cargoIntake.set(-1.0); //Set intake to -100%
        }
    }

    /**
     * Accessor for the current manipulator state
     *
     * @return The current manipulator state
     */
    public static CargoManipulatorState getManipulatorState() { return manipulatorState; }

    /**
     * Resets the current setpoint and the encoder for the arm
     * Used in teleopInit and autonomousInit
     */
    public static void reset() {
        //Resets the setpoint to the minimum setpoint
        setpoint = minSetpoint;

        //Resets the encoders
        robotMap.encoderPivotTwo.reset();
        robotMap.encoderPivotOne.reset();

        //Reset state to neutral
        manipulatorState = CargoManipulatorState.NEUTRAL;
    }

    /**
     * Mutator method for the setpoint
     *
     * @param down Whether to set the setpoint to go down (if true, it will attempt to go down)
     */
    public static void setSetpoint(boolean down) {
        if (down && setpoint < maxSetpoint) { //If we want to go down AND we are not at the max setpoint...
            manipulatorState = CargoManipulatorState.COLLECTING_BALL; //...Set state to be collecting cargo
        } else if (!down && setpoint > minSetpoint) { //Else if we want to go up AND we are not all the way up...
            manipulatorState = CargoManipulatorState.HOLDING_BALL; //...Set state to be holding cargo
        }
    }

    /**
     * Sets manipulator to 45 degree mode if not in that state. If it is in 45 degree mode
     * when this method is called it will reset it to neutral.
     * <br>
     *     <b>NOTE: </b> When turning off 45 degree mode, it will reset to neutral position, even if it was in a
     *     different mode before. Ensure the driver is aware of this.
     */
    public static void toggle45() {
        if(manipulatorState != CargoManipulatorState.EXTENDED) {
            manipulatorState = CargoManipulatorState.EXTENDED;
        } else {
            manipulatorState = CargoManipulatorState.NEUTRAL;
        }
    }

    /**
     * Runs the calculations for the PID loop based on the
     * current setpoint and the current encoder value
     */
    @SuppressWarnings("Duplicates")
    private static void PID() {
        //TODO: Incorporate feedforward
        //PID = P * error + I * (sum of all error) * D * (previous error)
        //Where P, I, and D are constants and error is the difference between the setpoint and the current position

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
        if (drive > 0.2) { //If we want to go forward too fast...
            drive = .2; //...limit it to 20% power
        } else if (drive < -.4) { //Else we want to go backwards too fast...
            drive = -.4; //...limit it to -80% power
        }
    }

    /**
     * Fires the action that occurs when X is pressed.
     * <br>
     * More specifically, this expels any ball that the manipulator may be carrying. Note that this will
     * reset the manipulator to neutral after it's done
     */
    public static void xIsPressed() {
        if (xPressed && xTimer < 21) { //If the X Button gets pressed and timer is les than 21
            manipulatorState = CargoManipulatorState.EXPELLING_BALL;
            xTimer++; //Increment timer by 1
        } else { //Once timer goes over 21 ticks
            xTimer = 0; //reset timer
            xPressed = false; //reset xPressed..
            manipulatorState = CargoManipulatorState.NEUTRAL; //Reset state to neutral
        }
    }
}

