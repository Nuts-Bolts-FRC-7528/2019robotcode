package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.auto.AutoModeExecutor;
import frc.robot.auto.modes.MoveForwardAuto;
import frc.robot.common.robotMap;
import frc.robot.components.drivetrain;
import frc.robot.common.OI;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
//import camera server - BT 1/26/19
//Fix depreciated import statement - EH 1/27/19

public class Robot extends TimedRobot{
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    //Creates a DifferentialDrive using both SpeedControllerGroups
    NetworkTable table;
    SendableChooser<AutoModeExecutor> autoChooser;
    private AutoModeExecutor auto = new AutoModeExecutor(new MoveForwardAuto());

    
    
    @Override
    public void robotInit() {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        table = ntinst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        autoChooser = new SendableChooser<AutoModeExecutor>(); //Sets a new chooser on the driver station for auto mode selection
        autoChooser.addOption("Move Forward auto", new AutoModeExecutor(new MoveForwardAuto())); //Adds the move forward auto autmode to the chooser

        //solenoid.set(DoubleSolenoid.Value.kReverse);
        //solenoid.set(DoubleSolenoid.Value.kOff);
    }

    @Override
    public void autonomousInit() {
        auto.start();
        //autoChooser.getSelected().start(); //Starts the selected automode
    }

    @Override
    public void autonomousPeriodic() {
        if(OI.driveJoystick.getRawButton(12)) { //Aborts the automode if button 12 is pressed
            auto.stop();
            //autoChooser.getSelected().stop();
        }
    }

    @Override
    public void teleopInit(){
        auto.stop();
        robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
        //autoChooser.getSelected().stop(); //Stops the automode
    }

    @Override
    public void teleopPeriodic(){ //Happens roughly every 1/20th of a second while teleop is active
        NetworkTableEntry center = table.getEntry("centerPix");
        int ballCenterPix = (int)center.getDouble(0);
        robotMap.manipulatorA.setSpeed(OI.manipulatorContoller.getY()); //Dummy manipulator  (uses gamepad)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y.
        if (OI.manipulatorContoller.getAButton()) {
            drivetrain.turnLeftForSecond();
        }

        if(OI.driveJoystick.getRawButton(2)) {
            if(ballCenterPix > 80) { //Turn right
                System.out.println("Turning right!");
                drivetrain.setRightMotorSpeed(.3);
                drivetrain.setLeftMotorSpeed(.4);
            } else if (ballCenterPix < 80) { //Turn left
                System.out.println("Turning left!");
                drivetrain.setLeftMotorSpeed(.3);
                drivetrain.setRightMotorSpeed(.4);
            } else {
                System.out.println("Ball not found!");
            }
        }
        if (OI.manipulatorContoller.getBumperPressed(GenericHID.Hand.kLeft)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kForward); //Solenoid goes forward when left bumper is pressed.
        }

        if (OI.manipulatorContoller.getBumperReleased(GenericHID.Hand.kRight)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
           
        }
    }
}