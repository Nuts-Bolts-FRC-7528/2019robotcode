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
import frc.robot.components.elevator;
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
    NetworkTable table; //This table is for object recognition
    SendableChooser<AutoModeExecutor> autoChooser; //Creates a SendableChooser that allows drivers to select an automode
    //private AutoModeExecutor auto;
// This is michael and I'm testing using git from my house at home
    
    
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
        if(OI.driveJoystick.getRawButton(12)) { //Aborts the automode if button 12 is pressed
            //auto.stop();
            //autoChooser.getSelected().stop();
        }
    }

    @Override
    public void teleopInit(){
        //auto.stop();
        elevator.reset();
        robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
        //autoChooser.getSelected().stop(); //Stops the automode
    }

    @Override
    public void teleopPeriodic(){ //Happens roughly every 1/20th of a second while teleop is active


        /*
                [ROBOT DRIVING]
         */

        //Basically block this this from happening
       /*
        if (robotMap.solenoid.get() == DoubleSolenoid.Value.kForward){
            robotMap.elevator.setSpeed(0);
        }
        */
        if(robotMap.solenoid.get() == Value.kReverse){
            robotMap.elevator.setSpeed(OI.manipulatorContoller.getY()*.6); //Elevator Motor (throttle limited to 60%)
            elevator.iterate();
        }
        // if(OI.driveJoystick.getPOV() == 0) { //<--- bad code
            // elevator.setGoal(true);
        // }
        // if(OI.driveJoystick.getPOV() == 180) { //<---- more bad code jk Ethan is an awesome dood XD
            // elevator.setGoal(false);
        // }
        if(OI.driveJoystick.getRawButtonPressed(7)){ //If joystick button 7 is pressed
            elevator.setGoal(3); //Sets the elevator to level 3
        }
        if(OI.driveJoystick.getRawButtonPressed(9)){ //If joystick button 9 is pressed
            elevator.setGoal(2); //Sets the elevator to level 2
        }
        if(OI.driveJoystick.getRawButtonPressed(11)){ //If joystick button 11 is pressed
            elevator.setGoal(1); //Sets the elevator to level 1
        }

        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y.

        /*
                [OBJECT RECOGNITION]
         */
        NetworkTableEntry centerCargo = table.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
        NetworkTableEntry centerHatch = table.getEntry("hatchCenterPix");
        int ballCenterPix = (int)centerCargo.getDouble(0); //Gets the actual number from the NetworkTableEntry
        int hatchCenterPix = (int)centerHatch.getDouble(0);

        //CARGO ALIGNMENT
        if(OI.driveJoystick.getRawButton(2)) { //If thumb button is pressed
            drivetrain.align(ballCenterPix);
        }

        //HATCH ALIGNMENT
        if(OI.driveJoystick.getRawButton(3)) {
            drivetrain.align(hatchCenterPix);
        }

        /*
                [PNEUMATICS]
         */

        if (OI.manipulatorContoller.getBumperPressed(GenericHID.Hand.kLeft)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kForward); //Solenoid goes forward when left bumper is pressed.
        }

        if (OI.manipulatorContoller.getBumperReleased(GenericHID.Hand.kRight)){
            robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
           
         }
    }

    @Override
    public void testPeriodic() {
        robotMap.elevator.setSpeed(OI.manipulatorContoller.getY()*.6); //Elevator Motor (throttle limited to 60%)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    }
}
