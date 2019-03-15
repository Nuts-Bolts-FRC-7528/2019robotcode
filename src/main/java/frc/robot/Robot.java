package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.auto.AutoModeExecutor;
import frc.robot.common.robotMap;
import frc.robot.components.Drivetrain;
import frc.robot.components.Elevator;
import frc.robot.common.OI;
import static edu.wpi.first.wpilibj.GenericHID.Hand.kRight;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Robot extends TimedRobot {
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    public static double RightAnalog = OI.manipulatorContoller.getY(kRight);


    //Creates a DifferentialDrive using both SpeedControllerGroups
    NetworkTable table; //This table is for object recognition
    SendableChooser<AutoModeExecutor> autoChooser; //Creates a SendableChooser that allows drivers to select an automode
    //private AutoModeExecutor auto;


    @Override
    public void robotInit() {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        table = ntinst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        //autoChooser = new SendableChooser<AutoModeExecutor>(); //Sets a new chooser on the driver station for auto mode selection
        //autoChooser.addOption("Move Forward auto", new AutoModeExecutor(new MoveForwardAuto())); //Adds the move forward auto autmode to the chooser

        //SmartDashboard.putData("Select Automode: ",autoChooser);
        //solenoid.set(DoubleSolenoid.Value.kReverse);
        //solenoid.set(DoubleSolenoid.Value.kOff);
    }

    @Override
    public void autonomousInit() {
        //auto = new AutoModeExecutor(new MoveForwardAuto());
        //auto.start();
        //autoChooser.getSelected().start(); //Starts the selected automode
    }

    @Override
    public void autonomousPeriodic() {
        if (OI.driveJoystick.getRawButton(12)) { //Aborts the automode if button 12 is pressed
            //auto.stop();
            //autoChooser.getSelected().stop();
        }
    }

    @Override
    public void teleopInit() {
        //auto.stop();
        Elevator.reset();
        robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
        //autoChooser.getSelected().stop(); //Stops the automode
    }

    @Override
    public void teleopPeriodic() { //Happens roughly every 1/20th of a second while teleop is active


        /*
                [ROBOT CARGO-PICKUP]
         */

        //I reallly really hope this works - By Kobe Nguyen

////////////////////////////////////////////////////////////////////////////////
        double change; // math
        double PosCmd = RightAnalog; // THE DESIRED INPUT OF THE MANIPULATOR
        double e1pos = robotMap.encoderPivitOne.getRate(); //What we read from the first motor
        double e2pos = robotMap.encoderPivitTwo.getRate(); // What we read from the second motor


        //These variables are part of the math i.e. no touch!
        double error1a;
        double error1ai = 0;
        double error1ad = 0;
        double previous_error1a = 0;
        double cmd1a;

        double error1b;
        double error1bi = 0;
        double error1bd = 0;
        double previous_error1b = 0;
        double cmd1b;

        double error2a;
        double error2ai = 0;
        double error2ad = 0;
        double previous_error2a = 0;
        double cmd2a;

        double error2b;
        double error2bi = 0;
        double error2bd = 0;
        double previous_error2b = 0;
        double cmd2b;


        //The calculated (output) in getRate() which the motors should be run at.
        double cmd1;  // motor1
        double cmd2;   // motor2




// initialize these:

        double RLPC = 0;  // Rate-Limited Position Command


// tuning constants:

        double rateLimit = 1;  // allowable position change per iteration

        double i1aLimit = 1;     // integrator clamp for M1 a
        double i1bLimit = 1;     // integrator clamp for M1 b

        double i2aLimit = 1;   // integrator clamping value for M2 a
        double i2bLimit = 1;   // integrator clamping value for M2 b

        // Theoretically, the gains should all share the same constant, so if you want, you should change them to 1 variable

        double Kp1a = 1;  // PID gains 1a
        double Ki1a = 0;
        double Kd1a = 0;

        double Kp1b = 1;  // PID gains 1b
        double Ki1b = 0;
        double Kd1b = 0;


        double Kp2a = 1; // PID gains 2a
        double Ki2a = 0;
        double Kd2a = 0;


        double Kp2b = 1; // PID gains 2b
        double Ki2b = 0;
        double Kd2b = 0;


// this is your control loop:

// rate-limit the change in position command

        change = PosCmd - RLPC;
        if (change > rateLimit) change = rateLimit;
        else if (change < -rateLimit) change = -rateLimit;
        RLPC += change;

// PID controller for M1

        //part1 Motor1 PID //see Ether's graphic and Java PID wikipedia article

        error1a = RLPC - (e1pos + e2pos) / 2; //closed loop error
        error1ai += error1a; // integrate the error
        if (error1ai > i1aLimit) error1ai = i1aLimit; //clamp the integrated error
        else if (error1ai < -i1aLimit) error1ai = -i1aLimit;
        error1a = error1a - previous_error1a; //rate of change in error1 for D term
        previous_error1a = error1a; // save for next iteration
        cmd1a = Kp1a * error1a + Ki1a * error1ai + Kd1a * error1ad;

        //part2 Motor 1 PID partial master/slave input
        error1b = e2pos - e1pos; //closed loop error
        error1bi += error1b; // integrate the error
        if (error1bi > i1aLimit) error1bi = i1bLimit; // clamp the integrated error
        else if (error1bi < -i1bLimit) error1bi = -i1bLimit;
        error1bd = error1b - previous_error1a; // rate of change in error1a for D tern
        previous_error1a = error1b; //save for next iteration
        cmd1b = Kp1b * error1b + Ki1b * error1bi + Kd1b * error1bd;

        cmd1 = 0.9 * (cmd1a + cmd1b); // limit to 90% b/c 100% motor power is always different
// PID controller for M2

        //part1 motor1
        error2a = RLPC - (e1pos + e2pos) / 2;//closed loop error
        error2ai += error2a;// integrate the error
        if (error2ai > i2aLimit) error2ai = i2aLimit;// clamp the integrated error
        else if (error2ai < -i2aLimit) error2ai = -i2aLimit;
        error2a = error2a - previous_error2a;// rate of change in error1a for D tern
        previous_error2a = error2a;//save for next iteration
        cmd2a = Kp2a * error2a + Ki2a * error2ai + Kd2a * error2ad;

        //part 2 Motor2
        error2b = e1pos - e2pos;//closed loop error
        error2bi += error2b;// integrate the error
        if (error2bi > i2bLimit) error2bi = i2bLimit;// clamp the integrated error
        else if (error2bi < -i2bLimit) error2bi = -i2bLimit;
        error2b = error2b - previous_error2b;// rate of change in error1a for D tern
        previous_error2b = error2a;//save for next iteration
        cmd2b = Kp2b * error2b + Ki2b * error2bi + Kd2b * error2bd;

        cmd2 = 0.9 * (cmd2a + cmd2b); // limit to 90% b/c 100% motor power is ALWAYS different

        ///////////////////////////////////////////////////
        double howfast = .2; // constant for base motor speed.
        // Ideally, the output of the PID should ADD onto baseSpeed,
        // however, I am not sure sure, more like kinda sure, but not really.
        // Just know that it also happens to be a double :D

        robotMap.cargoPivitTwo.setSpeed(RightAnalog*howfast+cmd1);
        robotMap.cargoPivitOne.setSpeed(RightAnalog*howfast+cmd2);


    /////////////////////////////////////////////////////////


        /*
                [ROBOT DRIVING]
         */

    //Basically block this this from happening
       /*
        if (robotMap.solenoid.get() == DoubleSolenoid.Value.kForward){
            robotMap.Elevator.setSpeed(0);
        }
        */
        if(robotMap.solenoid.get()==Value.kReverse)

    {
        robotMap.elevator.setSpeed(OI.manipulatorContoller.getY() * .5); //Elevator Motor (throttle limited to 60%)
        Elevator.iterate();
    }
        if(OI.driveJoystick.getRawButtonPressed(7))

    { //If joystick button 7 is pressed
        Elevator.setGoal(3); //Sets the Elevator to level 3
    }
        if(OI.driveJoystick.getRawButtonPressed(9))

    { //If joystick button 9 is pressed
        Elevator.setGoal(2); //Sets the Elevator to level 2
    }
        if(OI.driveJoystick.getRawButtonPressed(11))

    { //If joystick button 11 is pressed
        Elevator.setGoal(1); //Sets the Elevator to level 1
    }



        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y.

    /*
            [OBJECT RECOGNITION]
     */
    NetworkTableEntry centerCargo = table.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
    NetworkTableEntry centerHatch = table.getEntry("hatchCenterPix");
    int ballCenterPix = (int) centerCargo.getDouble(0); //Gets the actual number from the NetworkTableEntry
    int hatchCenterPix = (int) centerHatch.getDouble(0);

    //CARGO ALIGNMENT
        if(OI.driveJoystick.getRawButton(2))

    { //If thumb button is pressed
        Drivetrain.align(ballCenterPix);
    }

    //HATCH ALIGNMENT
        if(OI.driveJoystick.getRawButton(3))

    {
        Drivetrain.align(hatchCenterPix);
    }

        /*
                [PNEUMATICS]
         */

        if(OI.manipulatorContoller.getBumperPressed(GenericHID.Hand.kLeft))

    {
        robotMap.solenoid.set(DoubleSolenoid.Value.kForward); //Solenoid goes forward when left bumper is pressed.
    }

        if(OI.manipulatorContoller.getBumperReleased(GenericHID.Hand.kRight))

    {
        robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);

    }

}

    @Override
    public void testPeriodic() {
        robotMap.elevator.setSpeed(OI.manipulatorContoller.getY()*.6); //Elevator Motor (throttle limited to 60%)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    }
}
