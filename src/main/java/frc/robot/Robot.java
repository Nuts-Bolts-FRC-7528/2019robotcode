package frc.robot;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.robot.common.robotMap;
import frc.robot.components.drivetrain;
import frc.robot.common.OI;
import edu.wpi.first.cameraserver.CameraServer;
//import camera server - BT 1/26/19
//Fix depreceated import statement - EH 1/27/19

public class Robot extends TimedRobot{
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    //Creates a DifferentialDrive using both SpeedControllerGroups
    
    @Override
    public void robotInit(){ 
        CameraServer.getInstance().startAutomaticCapture();
    } //Defines stuff to happen when the robot is first turned on (initiating the cameraserver here)
    //Added camera - BT 1/26/19

    @Override
    public void teleopInit(){ } //Defines stuff to happen when teleop is enabled (nothing in this case)

    @Override
    public void teleopPeriodic(){ //Happens roughly every 1/20th of a second while teleop is active
        robotMap.manipulatorA.setSpeed(OI.manipulatorContoller.getY()); //Dummy manipulator (uses gamepad)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()),(OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y.
        if(OI.manipulatorContoller.getAButton()) {
            drivetrain.turnLeftForSecond();
        }
    }
}