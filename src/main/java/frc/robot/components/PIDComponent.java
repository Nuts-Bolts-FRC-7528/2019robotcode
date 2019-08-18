package frc.robot.components;

import edu.wpi.first.wpilibj.PIDSource;

/**
 * @author Ethan Hanlon
 * Defines the outline of a component system that uses PIDF in any fashion. The goal is to abstract away a lot
 * of the nitty gritty control theory stuff so we can avoid human error. TO USE:
 * <br>
 * <ol>
 *     <li>Create a new class that extends PIDComponent</li>
 *     <li>Create a constructor with a super call and put your constants in there</li>
 *     <li>Inside the constructor, call setMaxDrive() and setMinDrive() to set their respective values, or leave them to the default at 100%</li>
 *     <li>Inside the constructor, call setMinSetpoint to set the minimum setpoint, or leave this at 0</li>
 *     <li>In robot, create a new instance of the class</li>
 *     <li>Run the iterate() method an iterative method of robot (ie teleopPeriodic())</li>
 *     <li>Use the getPID() method to get the current calculated drive value of the loop and apply it to your motor</li>
 * </ol>
 * <br>
 * As a matter of good practice, any classes extending this should be titled "PID" and then the name of the component,
 * so for example "PIDElevator"
 */
public class PIDComponent {
    private double P,I,D,F; //Fields for proportional, integral, derivative, and feedforward constants
    private double integratorLimit = 1.0; //Prevents the integrator from going above 1 or below -1. Prevents windup
    private double drive; //Drive value as calculated by the PIDF loop.
    private PIDSource source; //Source where error is collected from
    private double setpoint, error, previous_error; //Setpoint and error for the PIDF loop. previous_error is used for derivative
    private double maxDrive = 1.0; //Maximum drive value allowed for the component. Defaults to 100%
    private double minDrive = -1.0; //Lowest drive value allowed for the component. Defaults to -100%
    private double minSetpoint = 0; //Minimum setpoint. Used to keep mechanisms out a slight bit. Defaults to 0

    /**
     * Constructor for a PID Component. Use the params to set the respective constants of the component.
     * @param pidSource Sensor to get feedback from. Needs to implement PIDSource (most encoders and non-I2C sensors should be good)
     * @param Pconstant Proportional constant
     * @param Iconstant Integrator constant. Setting to 0 will disable integrator.
     * @param Dconstant Derivative constant. Setting to 0 will disable derivative.
     * @param feedforward Feedforward constant
     */
    public PIDComponent(PIDSource pidSource, double Pconstant, double Iconstant, double Dconstant, double feedforward) {
        source = pidSource;
        P = Pconstant;
        I = Iconstant;
        D = Dconstant;
        F = feedforward;
    }

    /**
     * Constructor for a PID Component. Use the params to set the respective constants of the component.
     * Leaves feedforward set to 1.0.
     * @param pidSource Sensor to get feedback from. Needs to implement PIDSource (most encoders and non-I2C sensors should be good)
     * @param PConstant Proportional constant.
     * @param IConstant Integrator constant. Setting to 0 will disable integrator.
     * @param DConstant Derivative constant. Setting to 0 will disable derivative.
     */
    public PIDComponent(PIDSource pidSource, double PConstant, double IConstant, double DConstant) {
        source = pidSource;
        P = PConstant;
        I = IConstant;
        D = DConstant;
        F = 1.0;
    }

    /**
     * Sets the minimum setpoint used by this component
     * @param minSet Minimum setpoint for the component
     */
    protected void setMinSetpoint(double minSet) {
        minSetpoint = minSet;
    }

    /**
     * Returns the current setpoint
     * @return The current setpoint
     */
    public double getSetpoint() {
        return setpoint;
    }

    /**
     * Set highest speed the PIDF loop will set the component to.
     * @param max Maximum speed the component will drive at. Should be a positive number between 0 - 1.0
     */
    protected void setMaxDrive(double max) {
        maxDrive = max;
    }

    /**
     * Set highest speed the component will drive in REVERSE.
     * @param min Maximum speed the component will reverse at. Should be a negative number betewen -1.0 - 0, unless the component never reverses
     */
    protected void setMinDrive(double min) {
        minDrive = min;
    }

    /**
     * Returns the value calculated in the PID loop
     * @return Drive value calculated in the PID loop. Double between -1.0 and 1.0
     */
    public double getPID() {
        return drive;
    }

    /**
     * Sets the current setpoint
     * @param newSetpoint Desired setpoint
     */
    protected void setSetpoint(double newSetpoint) {
        setpoint = newSetpoint;
    }

    /**
     * Runs the PIDF loop
     */
    public void iterate() {
        PIDF(); //Calls PIDF to calculate the control algorithm
    }

    /**
     * Calculates the values for the PIDF loop. Note that this needs to be run iteratively - otherwise it will
     * only calculate it once and stop. Hence the iterate() method
     */
    private void PIDF() {
        if(setpoint < minSetpoint) { //If setpoint is below the minimum...
            setpoint = minSetpoint; //...Set it to the minimum
        }
        previous_error = error; //Calculate previous error for derivative
        error = setpoint - source.pidGet(); //Calculate current error by subtracting source's current position from setpoint
        double prop = P * error; //Calculate the proportional value based off the P constant
        double integral = error * .02; //Calculate integral by taking error and multiplying by *.02, the amount of seconds that will have passed
        double derivative = error - previous_error; //Calculate derivative by subtracting current error from previous error

        if(integral > integratorLimit) { //If integrator exceeds the limit...
            integral = integratorLimit; //... Set it to the max value allowed
        }  else if (integral < -integratorLimit) { //Else if the integrator is below the lower limit...
            integral = -integratorLimit; //... Set it to the lowest value allowed
        }

        drive = (F * (prop + (I * integral) + (D * derivative))) / 100.0; //Calculate final drive value and divide by 100 to make it a percent

        if(drive > maxDrive) { //If our drive value exceeds the limit...
            drive = maxDrive; //...Set it to the max value allowed
        } else if (drive < minDrive) { //Else if our drive value is below the lower limit...
            drive = minDrive; //...Set it to the lowest value allowed
        }
    }
}
