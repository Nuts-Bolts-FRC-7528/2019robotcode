package frc.robot.common;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

/**
 * Defines public variables for our driver station controllers
 */
public class OI {

    public static final Joystick driveJoystick = new Joystick(0);
    public static final XboxController manipulatorContoller = new XboxController(1);
}