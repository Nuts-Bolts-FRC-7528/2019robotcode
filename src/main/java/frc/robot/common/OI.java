package frc.robot.common;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

/**
 * Defines public variables for our driver station controllers
 */
public class OI {

    /**
     * Our joystick that our pilot uses.
     */
    public static final Joystick driveJoystick = new Joystick(0);
    /**
     * The gamepad that our manipulator pilot uses. While not an actual xbox controller, the button bindings all match
     */
    public static final XboxController manipulatorContoller = new XboxController(1);
}