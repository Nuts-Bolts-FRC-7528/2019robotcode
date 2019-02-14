package frc.robot.common;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import frc.robot.sensors.REVColor;


/**
 * Defines public variables for our motor controllers
 */
public class robotMap {
    public static final PWMVictorSPX leftRearDrive = new PWMVictorSPX(3); //Defines a new PWMVictorSPX (motor controller) on PWM port 3
    public static final PWMVictorSPX rightRearDrive = new PWMVictorSPX(4); //Defines a new PWMVictorSPX(motor controller) on PWM port 4
    public static final PWMVictorSPX leftFrontDrive = new PWMVictorSPX(5); //Defines a new PWMVictorSPX (motor controller) on PWM port 5
    public static final PWMVictorSPX rightFrontDrive = new PWMVictorSPX(6); //Defines a new PWMVictorSPX (motor controller) on PWM port 6
    public static final PWMVictorSPX manipulatorA = new PWMVictorSPX(2);
    public static final REVColor colorA = new REVColor(I2C.Port.kOnboard);
    public static final REVColor colorB = new REVColor(I2C.Port.kMXP);
}