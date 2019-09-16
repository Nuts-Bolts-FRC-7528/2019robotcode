package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.common.OI;
import frc.robot.common.robotMap;
import frc.robot.components.CargoCatch;
import frc.robot.components.Drivetrain;
import frc.robot.components.Elevator;

import static frc.robot.components.CargoCatch.xPressed;

public class Robot extends TimedRobot {
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    //Creates a DifferentialDrive using both SpeedControllerGroups
    private NetworkTable visionTable; //This table is for object recognition

    //TODO: Add safety code override

    @Override
    public void robotInit() {
        /*   [NETWORKTABLES / USB CAMERAS]   */
        NetworkTableInstance ntInst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        visionTable = ntInst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        CameraServer.getInstance().startAutomaticCapture(); //Activates USB camera streaming from RIO

        /*   [BREAK MODES]   */
        robotMap.cargoPivotOne.setNeutralMode(NeutralMode.Brake); //Set cargo pivot one to break mode (in case it wasn't already)
        robotMap.cargoPivotTwo.setNeutralMode(NeutralMode.Brake); //Set cargo pivot two to break mode (in case it wasn't already)
        robotMap.elevator.setNeutralMode(NeutralMode.Brake); //Set elevator motor to break mode

        /*   [MANIPULATOR RESETS]  */
        Elevator.reset(); //Reset elevator position and encoder
        CargoCatch.reset(); //Reset manipulator position and encoder
    }

    @Override
    public void autonomousInit() {
        robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //So wings start out as OPEN

    }

    @Override
    public void autonomousPeriodic() {
        teleopPeriodic(); //Run all normal teleop functions
    }

    @Override
    public void teleopPeriodic() { //Happens roughly every 1/20th of a second while teleop is active
        /*  [ROBOT DRIVING] */

            /*   {JOYSTICK CONTROL}    */
        if(Elevator.setpoint > 4300){ //If the elevator is at or above rocket level 2...
            m_drive.arcadeDrive((-OI.driveJoystick.getY() * 0.5), (OI.driveJoystick.getX() * 0.5)); //...Drive the robot at 50% speed for stability
        }
        else { //...Else, drive the robot at regular speed
            m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Actually drives the robot. Uses the joystick.
        }
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y

            /*    {FINE CONTROL}     */

        //Fine control is mapped to the D-Pad of the joystick and allows the driver to make more precise movements
        if(OI.driveJoystick.getPOV() == 0){
            Drivetrain.setLeftMotorSpeed(0.25);
            Drivetrain.setRightMotorSpeed(0.25);
        }
        if(OI.driveJoystick.getPOV() == 90){
            Drivetrain.setLeftMotorSpeed(0.35);
            Drivetrain.setRightMotorSpeed(-0.35);
        }
        if(OI.driveJoystick.getPOV() == 180){
            Drivetrain.setLeftMotorSpeed(-0.25);
            Drivetrain.setRightMotorSpeed(-0.25);
        }
        if(OI.driveJoystick.getPOV() == 270){
            Drivetrain.setLeftMotorSpeed(-0.35);
            Drivetrain.setRightMotorSpeed(0.35);
        }

        /*  [ELEVATOR]  */

            /*  {LEVEL SETTER}   */
        if (OI.driveJoystick.getRawButtonPressed(7)) { //If joystick button 7 is pressed
            Elevator.setGoal(3); //Sets the Elevator to level 3
        }

        if (OI.driveJoystick.getRawButtonPressed(9)) { //If joystick button 9 is pressed
            Elevator.setGoal(2); //Sets the Elevator to level 2
        }

        if (OI.driveJoystick.getRawButtonPressed(11)) { //If joystick button 11 is pressed
            Elevator.setGoal(1); //Sets the Elevator to level 1
        }

        if (OI.driveJoystick.getRawButtonPressed(8)) { //If joystick button 11 is pressed
            Elevator.setGoal(0); //Sets the Elevator to level 0
        }
        //CARGO SHIP BUTTON
        if (OI.driveJoystick.getRawButtonPressed(10)) { //If joystick button 10 is pressed
            Elevator.setGoal(5); //Sets the Elevator to level 5
        }

        /*  [CARGO MANIPULATOR]   */

        if(OI.manipulatorController.getAButtonPressed()) { //When the A button is pressed...
            if(safeToDeployCargo()) { //...If it is safe to deploy cargo manipulator...
                CargoCatch.setSetpoint(true); //...Deploy cargo manipulator
            }
        }

        if(OI.manipulatorController.getBButtonPressed()) { //When the B button is pressed...
            CargoCatch.setSetpoint(false); //...Bring cargo manipulator up
        }

        if(OI.manipulatorController.getBackButtonPressed()) { //When back button is pressed...
            if(safeToDeployCargo()) { //If it is safe to deploy cargo manipulator...
                CargoCatch.toggle45(); //...Toggle 45 degree mode
            }
        }

        /*  [ITERATING METHODS] */


        CargoCatch.xIsPressed();//Runs the intake to pop out ball
        Elevator.iterate(); //Update where the elevator should be
        CargoCatch.iterate(); //Update where the cargo manipulator should be


        /*  [OBJECT RECOGNITION]    */


        NetworkTableEntry centerCargo = visionTable.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
        NetworkTableEntry centerHatch = visionTable.getEntry("hatchCenterPix"); //Fetch NetworkTableEntry for center pixel of hatch
        NetworkTableEntry centerTargets = visionTable.getEntry("vtCenterPix"); //Fetch NetworkTablEntry for center pixel of vision targets
        int ballCenterPix = (int) centerCargo.getDouble(-1); //Gets the actual number from the cargo NetworkTableEntry. Defaults to -1 if faulty connection
        int hatchCenterPix = (int) centerHatch.getDouble(-1); //Gets the actual number from the hatch NetworkTableEntry
        int vtCenterPix = (int) centerTargets.getDouble(-1); //Gets the actual number from the vision targets NetworkTableEntry

        robotMap.alignmentLeds.set(OI.driveJoystick.getRawButton(5)); //Turn on alignment LED while button 5 is pressed

        /*  [CARGO ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(2)) { //If thumb button is pressed
            Drivetrain.align(ballCenterPix, false); //Align to cargo
        }


        /*  [HATCH ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(3)) { //If button 3 is pressed
            Drivetrain.align(hatchCenterPix, false); //Align to hatch
        }


        /*  [VISION TARGET ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(1) && SmartDashboard.getBoolean("Vision Tape Tracking:",false)) { //If trigger is pressed
            Drivetrain.align(vtCenterPix, false); //Align to vision targets
        }


        /*  [PNEUMATICS]    */

        if(OI.manipulatorController.getBumperPressed(GenericHID.Hand.kLeft)) { //If left bumper is pressed...
            if(safeToDeployHatch()) { //...If it is safe to deploy the hatch manipulator...
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //...Deploy hatch wings
            }
        }

        if(OI.manipulatorController.getBumperPressed(GenericHID.Hand.kRight)) { //If right bumper is pressed...
            robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); //...Retract hatch wings
        }

        if(OI.manipulatorController.getPOV() == 0) { //If forward is pressed on D-Pad...
            if(safeToDeployHatch()) { ///...If it is safe to deploy the hatch manipulator...
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); //...Push hatch mechanism out
            }
        }

        if(OI.manipulatorController.getPOV() == 180) { //If backwards is pressed on D-Pad...
            robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); //...Pull hatch mechanism in
        }
    }

    @Override
    public void testPeriodic() {
        robotMap.elevator.set(ControlMode.PercentOutput, OI.manipulatorController.getY() * .6); //Elevator Motor (throttle limited to 60%)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    }

    /**
     * Evaluates whether it is safe to deploy the cargo manipulator given the state of the hatch manipulator
     * @return If true, it is safe to deploy the cargo manip. False otherwise
     */
    private boolean safeToDeployCargo() {
        //If the hatch mechanism is retracted, it is safe to deploy
        return robotMap.hatchPushOne.get() == DoubleSolenoid.Value.kReverse && robotMap.hatchCatch.get() == DoubleSolenoid.Value.kReverse;
    }

    /**
     * Evaluates whether it is safe to deploy the hatch manipulator given the state of the cargo manipulator
     * @return If true, it is safe to deploy the hatch manip. False otherwise
     */
    private boolean safeToDeployHatch() {
        //If the cargo mechanism is in a neutral state, it is safe to deploy
        return CargoCatch.getManipulatorState() == CargoCatch.CargoManipulatorState.NEUTRAL;
    }
}
