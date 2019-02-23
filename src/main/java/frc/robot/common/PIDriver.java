package frc.robot.common;

import edu.wpi.first.wpilibj.PIDSource;

/**
 * This provides a global PI driver so that you don't have to make one in every class.
 */
public class PIDriver {
    private int P,I, setpoint, integral;
    private double error, drive = 0;
    private PIDSource PIDInput;
    private boolean inverted;

    /**
     * Creates a PIDriver with parameters for P and I constants as well as the setpoint and the PIDSource
     * @param PConstant Constant for proportional control
     * @param IConstant Constant for Integral control
     * @param targetSetpoint Setpoint you want to move the actuator to
     * @param input PIDSource (ie an Encoder or Ultrasonic) used as input for the control loop
     */
    public PIDriver(int PConstant, int IConstant, int targetSetpoint, PIDSource input, boolean isInverted) {
        P = PConstant;
        I = IConstant;
        setpoint = targetSetpoint;
        PIDInput = input;
        inverted = isInverted;
    }

    /**
     * Creates a PIDriver and automatically sets P and I constants to 1
     * @param targetSetpoint Setpoint you want to move the actuator to
     * @param input PIDSource (ie an Encoder or Ultrasonic) used as input for the control loop
     */
    public PIDriver(int targetSetpoint, PIDSource input, boolean isInverted) {
        P = 1;
        I = 1;
        setpoint = targetSetpoint;
        PIDInput = input;
        inverted = isInverted;
    }

    /**
     * This method actually updates the PI values. It needs to be called ITERATIVELY (ie in teleopiterative() or in
     * update() of an Action)
     */
    public void PIupdate() {
        if(inverted) { error = -error;}
        error = setpoint - PIDInput.pidGet();
        integral += error * 0.2;
        drive = P*error + I*integral;
    }

    /**
     * Public accessor for the amount that the PIDriver has calculated the robot needs to drive at
     * @return A double to represent the speed the motors neeed to drive at. Should be between -1.0 and 1.0
     */
    public double getPI() {
        return drive;
    }

    /**
     * Public accessor for error
     * @return The error (the distance before the actuator gets to its setpoint)
     */
    public double getError() {
        return error;
    }
}
