package frc.robot.common;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;


/**
 * Defines public variables for our motor controllers and sensors
 */
public class robotMap {
    /* [DRIVETRAIN] */
    public static final PWMVictorSPX leftRearDrive = new PWMVictorSPX(3); //Defines a new PWMVictorSPX (motor controller) on PWM port 3
    public static final PWMVictorSPX rightRearDrive = new PWMVictorSPX(4); //Defines a new PWMVictorSPX(motor controller) on PWM port 4
    public static final PWMVictorSPX leftFrontDrive = new PWMVictorSPX(5); //Defines a new PWMVictorSPX (motor controller) on PWM port 5
    public static final PWMVictorSPX rightFrontDrive = new PWMVictorSPX(6); //Defines a new PWMVictorSPX (motor controller) on PWM port 6
    public static final PWMVictorSPX elevator = new PWMVictorSPX(2); //Elevator Victor SPX

    /* [CARGO MANIPULATOR] */
    public static final PWMVictorSPX cargoIntake = new PWMVictorSPX(0); //Intake Victor SPX
    public static final PWMVictorSPX cargoPivotOne = new PWMVictorSPX(7); //Pivot motor 1 Victor SPX
    public static final PWMVictorSPX cargoPivotTwo = new PWMVictorSPX(1); //Pivot motor 2 Victor SPX

    /* [HATCH MANIPULATOR] */
    public static final DoubleSolenoid hatchCatch = new DoubleSolenoid(4, 5); //Solenoid that catches the hatch
    public static final DoubleSolenoid hatchPushOne = new DoubleSolenoid(1,0); //Solenoid that pushes the hatch manipulator forward

    /* [ENCODERS] */
    public static final Encoder encoderPivotOne = new Encoder(1,0); //Pivot motor 1 encoder
    public static final Encoder encoderPivotTwo = new Encoder(3,2); //Pivot motor two encoder
    public static final Encoder elevatorEncoder = new Encoder(5,4); //Encoder for the elevator
}