package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * PIDComponent class for the cargo manipulator. Uses the PIDF loop found in PIDComponent
 */
public class PIDCargoCatch extends PIDComponent {

    /**
     * Constructor for PIDCargoCatch. Calls super with the constants set in this
     */
    public PIDCargoCatch() {
        super(robotMap.encoderPivotOne, 0.22, 0.114, 0.7); //Calls super with our PID values
        setMinSetpoint(30);
        setMaxDrive(0.2);
        setMinDrive(-0.4);
    }

    /**
     * Resets all the pivot encoders.
     */
    public void reset() {
        robotMap.encoderPivotTwo.reset();
        robotMap.encoderPivotOne.reset();
    }

    /**
     * Spits out the ball currently being held in the cargo manipulator, and stops spinning the wheels.
     * Currently bound to the X button
     */
    public void ballSpit() {

    }
}
