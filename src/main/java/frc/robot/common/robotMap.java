package frc.robot.common;

import edu.wpi.first.wpilibj.PWMVictorSPX;

/*
 * This class holds a bunch of variables for the motors. This
 * way you can change it here without editing every single
 * class that references it, and it also cuts down on the
 * amount of imports you need to make, saving memory. Note
 * that all the variables here are public and static so that
 * they can be referenced outside this class.
 */

public class robotMap {
    public static final PWMVictorSPX leftRearDrive = new PWMVictorSPX(3); //Defines a new PWMVictorSPX (motor controller) on PWM port 3
    public static final PWMVictorSPX rightRearDrive = new PWMVictorSPX(4); //Defines a new PWMVictorSPX(motor controller) on PWM port 4
    public static final PWMVictorSPX leftFrontDrive = new PWMVictorSPX(5); //Defines a new PWMVictorSPX (motor controller) on PWM port 5
    public static final PWMVictorSPX rightFrontDrive = new PWMVictorSPX(6); //Defines a new PWMVictorSPX (motor controller) on PWM port 6
    public static final PWMVictorSPX miscellaneous = new PWMVictorSPX(2);
    /* 
    To reference these variables outside this class:

    1.) import frc.robot.common.robotMap
    2.) Use (class).(variable), for example robotMap.backLeftDrive
    */
}