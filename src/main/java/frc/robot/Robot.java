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
import frc.robot.components.Elevator;
import frc.robot.components.drivetrain;
import frc.robot.common.OI;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Robot extends TimedRobot{
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    //Creates a DifferentialDrive using both SpeedControllerGroups
    NetworkTable table;
    SendableChooser<AutoModeExecutor> autoChooser;

    
    
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
        autoChooser.getSelected().start(); //Starts the selected automode
    }

    @Override
    public void autonomousPeriodic() {
        if(OI.driveJoystick.getRawButton(12)) { //Aborts the automode if button 12 is pressed
            autoChooser.getSelected().stop();
        }
    }

    @Override
    public void teleopInit(){
        autoChooser.getSelected().stop(); //Stops the automode
    }

    @Override
    public void teleopPeriodic(){ //Happens roughly every 1/20th of a second while teleop is active

        /*
                [ ROBOT DRIVE ]
         */
        Elevator.moveElevator(OI.manipulatorContoller.getY(GenericHID.Hand.kLeft)); //Moves the elevator manually with left analog stick
        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y.


        /*
                [ VISION ]
         */
        NetworkTableEntry center = table.getEntry("centerPix"); //Get centerPix NetworkTableEntry from coprocessor
        int ballCenterPix = (int)center.getDouble(0); //Get the double from the entry above

        if(OI.driveJoystick.getRawButton(2)) { //If thumb button on joystick is pressed
            if(ballCenterPix > 80) { //If center pix of ball is PAST the center of the image (to the RIGHT of the robot)
                System.out.println("Turning right!");
                drivetrain.setRightMotorSpeed(.3);
                drivetrain.setLeftMotorSpeed(.4); //Put more speed on the LEFT side of the chassis so it turns RIGHT

            } else if (ballCenterPix < 80) { //If center pix of ball is BEFORE the center of the image (to the LEFT of the robot)
                System.out.println("Turning left!");
                drivetrain.setLeftMotorSpeed(.3);
                drivetrain.setRightMotorSpeed(.4); //Put more speed on the RIGHT side of the chassis so it turns LEFT
            } else {
                System.out.println("Ball not found!");
            }
        }

        /*
                [ PNEUMATICS ]
         */
        if (OI.manipulatorContoller.getBumperPressed(GenericHID.Hand.kLeft)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kForward); //Solenoid goes forward when left bumper is pressed.
        }

        if (OI.manipulatorContoller.getBumperReleased(GenericHID.Hand.kLeft)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
            Timer.delay(3);
            robotMap.solenoid.set(DoubleSolenoid.Value.kOff); //Solenoid goes back and turns off in ~3 seconds after bumper is released.
        }

        /*
                [ELEVATOR HALL EFFECT]
         */
        if(robotMap.elevatorHallEffect.get()) { //If hall effect is tripped
            if(robotMap.elevator.getSpeed() > 0) { //If we were moving UP...
                Elevator.setLevel(false); //...Increment the static int level (in the Elevator class) by one
            } else if (robotMap.elevator.getSpeed() < 0) { //Else if we were moving DOWN...
                Elevator.setLevel(true); //...Decrement the static int level (also in the Elevator class) by one
            }
        }
    }
}