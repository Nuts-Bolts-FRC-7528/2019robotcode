package frc.robot.common;
//Note that whenever you make a new class you need the above line
//in order to tell java where the class actually is.

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

public class OI {
    /*
                This is the OI, or Operator Interface class. This basically
                just holds a bunch of variables for operator control so we 
                can more easily reference it in other classes (ie robot.java, etc). 
                Note that the variables here are public and static so that they can 
                be referenced outside of this class.
    */
    public static final Joystick driveJoystick = new Joystick(0);
    public static final XboxController manipulatorContoller = new XboxController(1);
    /*
    To make a new variable here:
    Make it public and static so other classes can reference it
    If it's not supposed to change, make it final
    Make the rest of the variable as you would any variable in Java

    To reference variables:
    Import this class (import frc.robot.common.OI)
    Use OI.*name of your variable*
    For example:

    OI.driveJoystick - references the joystick defined in line 13
    */
}