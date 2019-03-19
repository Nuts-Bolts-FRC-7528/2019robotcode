package frc.robot.common;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;


/**
 * Defines public variables for our motor controllers
 */
public class robotMap {
    public static final PWMVictorSPX leftRearDrive = new PWMVictorSPX(3); //Defines a new PWMVictorSPX (motor controller) on PWM port 3
    public static final PWMVictorSPX rightRearDrive = new PWMVictorSPX(4); //Defines a new PWMVictorSPX(motor controller) on PWM port 4
    public static final PWMVictorSPX leftFrontDrive = new PWMVictorSPX(5); //Defines a new PWMVictorSPX (motor controller) on PWM port 5
    public static final PWMVictorSPX rightFrontDrive = new PWMVictorSPX(6); //Defines a new PWMVictorSPX (motor controller) on PWM port 6
    public static final PWMVictorSPX elevator = new PWMVictorSPX(2);
    public static final PWMVictorSPX cargoIntake = new PWMVictorSPX(0);
    public static final PWMVictorSPX cargoPivitOne = new PWMVictorSPX(7);
    public static final PWMVictorSPX cargoPivitTwo = new PWMVictorSPX(1);
    //public static final DoubleSolenoid solenoid = new DoubleSolenoid(4, 5);
    public static final DigitalInput photodiode = new DigitalInput(6);
    public static final DigitalInput elevatorBottom = new DigitalInput(9);
    public static final DigitalInput elevatorMiddle = new DigitalInput(7);
    public static final DigitalInput elevatorTop = new DigitalInput(5);
    public static final Encoder encoderPivitOne = new Encoder(1,0);
    public static final Encoder encoderPivitTwo = new Encoder(3,2);
}