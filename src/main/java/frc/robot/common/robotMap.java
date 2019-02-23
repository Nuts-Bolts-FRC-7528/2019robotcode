package frc.robot.common;

import edu.wpi.first.wpilibj.*;


/**
 * Defines public variables for our motor controllers
 */
public class robotMap {
    public static final PWMVictorSPX leftRearDrive = new PWMVictorSPX(3); //Defines a new PWMVictorSPX (motor controller) on PWM port 3
    public static final PWMVictorSPX rightRearDrive = new PWMVictorSPX(4); //Defines a new PWMVictorSPX(motor controller) on PWM port 4
    public static final PWMVictorSPX leftFrontDrive = new PWMVictorSPX(5); //Defines a new PWMVictorSPX (motor controller) on PWM port 5
    public static final PWMVictorSPX rightFrontDrive = new PWMVictorSPX(6); //Defines a new PWMVictorSPX (motor controller) on PWM port 6
    public static final PWMVictorSPX manipulatorA = new PWMVictorSPX(2);
    public static final DoubleSolenoid solenoid = new DoubleSolenoid(4, 5);

    public static final PWMVictorSPX elevator = new PWMVictorSPX(1);
    public static final Encoder elevatorEncoder = new Encoder(1,2,false);
    public static final DigitalInput elevatorHallEffect  = new DigitalInput(3);
    public static final int cargoOffset = 25; //How many extra encoder ticks to move should we want to go to the hatch
}