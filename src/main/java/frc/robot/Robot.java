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
    public void robotInit(){ 
        //CameraServer.getInstance().startAutomaticCapture();
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
        table = ntinst.getTable("vision");
        autoChooser = new SendableChooser<AutoModeExecutor>();
        autoChooser.addOption("Move Forward auto",new AutoModeExecutor(new MoveForwardAuto()));

        //solenoid.set(DoubleSolenoid.Value.kReverse);
        //solenoid.set(DoubleSolenoid.Value.kOff);
    } //Defines stuff to happen when the robot is first turned on (initiating the cameraserver here)
    //Added camera - BT 1/26/19

    @Override
    public void autonomousInit() {
        auto.start();
        //autoChooser.getSelected().start();
    }

    @Override
    public void autonomousPeriodic() {
        if(OI.driveJoystick.getRawButton(12)) {
            //autoChooser.getSelected().stop();
            auto.start();
        }
    }

    @Override
    public void teleopInit(){ } //Defines stuff to happen when teleop is enabled (nothing in this case)

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

        if (OI.manipulatorContoller.getBumperReleased(GenericHID.Hand.kLeft)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
            Timer.delay(3);
            robotMap.solenoid.set(DoubleSolenoid.Value.kOff); //Solenoid goes back and turns off in ~3 seconds after bumper is released.
        }
    }
}