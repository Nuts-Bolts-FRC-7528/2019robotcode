package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
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

    public static boolean hatchPushPistonExtended = false;
    private static boolean fortyFiveModeOn = false;
    private NetworkTable visionTable; //This table is for object recognition
//    private static int pnuematicsProtectionTimer; TODO: Find if this var is still necessary


    /**
     * Fires once on roboRIO startup. Sets CAN break modes and gets NetworkTables from
     * vision coprocessor
     */
    @Override
    public void robotInit() {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        visionTable = ntinst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        CameraServer.getInstance().startAutomaticCapture(); //This likes to be red. Activates camera
//        robotMap.alignmentLeds.set(true);
        robotMap.cargoPivotOne.setNeutralMode(NeutralMode.Brake);
        robotMap.cargoPivotTwo.setNeutralMode(NeutralMode.Brake);
        robotMap.elevator.setNeutralMode(NeutralMode.Brake);

        Elevator.reset(); //Reset elevator position and encoder
        CargoCatch.reset(); //Reset manipulator position and encoder
//        pnuematicsProtectionTimer = 0;
        robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //So wings start out as OPEN
        hatchPushPistonExtended = true;
        fortyFiveModeOn = false;
    }

    /**
     * Fires once at the beginning of autonomous (sandstorm) period. Resets variables for the beginning of the
     * match
     */
    @Override
    public void autonomousInit() {
        robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //So wings start out as OPEN
        CargoCatch.reset();


    }

    /**
     * Fires periodically during autonomous (sandstorm) period. Simply calls teleopPeriodic()
     */
    @Override
    public void autonomousPeriodic() {
        teleopPeriodic(); //Run all normal teleop functions
    }


    @Override
    public void teleopPeriodic() { //Happens roughly every 1/20th of a second while teleop is active
        /*   [SHUFFLEBOARD STATISTICS REPORTING]   */

        //CARGO MANIPULATOR ANGLE
        //Working on the assumption that 520 ticks = 90 deg, and 30 ticks = 0 deg
        SmartDashboard.putNumber("CARGO MANIPULATOR ANGLE", 90.0/(robotMap.encoderPivotOne.get()-30));

        //ELEVATOR MODE
        if(Elevator.isHatchMode) {
            SmartDashboard.putString("ELEVATOR MODE", "HATCH");
        } else {
            SmartDashboard.putString("ELEVATOR MODE", "CARGO");
        }

        /*   [PNUEMATICS STARTUP PROTECTION]
        TODO: Figure out if we still need this
        if(pnuematicsProtectionTimer < 70){
        pnuematicsProtectionTimer++; //Increments pneumaticsProtectionTimer
        if(pnuematicsProtectionTimer == 50){ //Once the timer reaches 70 ticks
//            robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); //Pull the claw back in
            pistonExtended = true;
        }
        if(pnuematicsProtectionTimer == 40){
            robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); //Pull the hatch mechanism back in
        }
        } */


        /*  [ROBOT DRIVING] */

        if(Elevator.setpoint > 5000){
            m_drive.arcadeDrive((-OI.driveJoystick.getY() * 0.5), (OI.driveJoystick.getX() * 0.5)); //Actually drives the robot. Uses the joystick.
        }
        else {
            m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Actually drives the robot. Uses the joystick.
        }
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y
        robotMap.elevator.set(ControlMode.PercentOutput, OI.manipulatorController.getY(GenericHID.Hand.kRight) * .5); //Allows manual control of the elevator
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

        /*  [ELEVATOR USE]  */


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


        /*   [45 DEGREE CODE]   */
        //TODO: Diagnose and fix IntelliJ warnings regarding fortyFiveModeOn always being false
        if( OI.manipulatorController.getBackButtonPressed()) {
            if (!fortyFiveModeOn) { //Checks if BackButtonPressed AND 45Out is false
                CargoCatch.setpoint = 250; //Sets setpoint to 250, which is about 45 degrees
                fortyFiveModeOn = true; // Sets 45Out is true for next button press
            } else if (fortyFiveModeOn) { //Checks if BBPressed AND 45Out is true
                CargoCatch.setpoint = CargoCatch.MinSetpoint; //Sets setpoint back to Minimum for hatching
                fortyFiveModeOn = false; // Sets 45Out to false for next time
            }
        }

        robotMap.alignmentLeds.set(OI.driveJoystick.getRawButton(5)); //Turn on alignment LED while button 5 is pressed


        /*  [CARGO MANIPULATOR]   */


        if(!hatchPushPistonExtended) { //If the hatch push piston has NOT been extended,
            if (OI.manipulatorController.getAButtonPressed()) { //If A button is pressed... (Manipulator to Neutral)
                CargoCatch.setSetpoint(true); //...go down
                Elevator.isHatchMode = true;//Sets elevator setpoints to ball height for rocket
            }

            if (OI.manipulatorController.getBButtonPressed()) { //If B button is pressed... (Ball Collect)
                CargoCatch.setSetpoint(false); //...go up
                Elevator.isHatchMode = true; //Sets Elevator setpoints to ball height for rocket

            }

            if (OI.manipulatorController.getXButtonPressed()) { //If X button is pressed (Ball Eject)
                xPressed = true; //Set xPressed to true(used in xIsPressed method)
                CargoCatch.setInMotorHolding = false; //Prevent motors from sucking
                CargoCatch.setInMotorPickUp = false; //Prevent motors from sucking x2
                Elevator.isHatchMode = true; //Sets elevator setpoints to ball height for rocket
            }
        }

        // [CARGO INTAKE MANUAL (TEST)
        //robotMap.cargoIntake.set(OI.manipulatorController.getY(GenericHID.Hand.kLeft) / 2);//Run the intake wheels


        /*  [ITERATING METHODS] */


        CargoCatch.xIsPressed();//Runs the intake to pop out ball
        Elevator.iterate(); //Update where the elevator should be
        CargoCatch.iterate(); //Update where the cargo manipulator should be


        /*  [OBJECT RECOGNITION]    */


        NetworkTableEntry centerCargo = visionTable.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
        NetworkTableEntry centerHatch = visionTable.getEntry("hatchCenterPix"); //Fetch NetworkTableEntry for center pixel of hatch
        NetworkTableEntry centerTargets = visionTable.getEntry("vtCenterPix"); //Fetch NetworkTablEntry for center pixel of vision targets
        int ballCenterPix = (int) centerCargo.getDouble(-1); //Gets the actual number from the cargo NetworkTableEntry
        int hatchCenterPix = (int) centerHatch.getDouble(-1); //Gets the actual number from the hatch NetworkTableEntry
        int vtCenterPix = (int) centerTargets.getDouble(-1); //Gets the actual number from the vision targets NetworkTableEntry


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
        Elevator.startIsPressed(); //Iterates startIsPresse, checks if startPressed is true, otherwise does nothing
        Elevator.dRightIsPressed(); //Iterates dRightisPressed, checks if dRightPressed is true, otherwise does nothing
        Elevator.dLeftIsPressed(); //Iterates dLeftisPressed, checks if dLeftPressed is true, otherwise does nothing
        if (CargoCatch.getSetpoint() == CargoCatch.MinSetpoint && robotMap.encoderPivotTwo.get() < CargoCatch.MinSetpoint + 40) { //If cargo manipulator is trying to go up
            if (OI.manipulatorController.getBumperPressed(GenericHID.Hand.kLeft)) { //If left bumper pressed
                hatchPushPistonExtended = true; //Restricts cargo manipulator
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //Push out hatch catching solenoid
                Elevator.isHatchMode = false; //Sets elevator setpoints to hatch height for rocket
            }

            if (OI.manipulatorController.getBumperPressed(GenericHID.Hand.kRight)) { //If right bumper pressed
                hatchPushPistonExtended = false; //Restricts cargo manipulator
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse);//Pull in hatch catching solenoid
                Elevator.isHatchMode = false;//Sets elevator setpoints to hatch height for rocket
            }

            if (OI.manipulatorController.getPOV() == 180) { //If d-pad is pressed down
                hatchPushPistonExtended = false; //Allows cargo manipulator to function
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); //Pull hatch mechanism in
                Elevator.isHatchMode = false;//Sets elevator setpoints to hatch height for rocket
            }

            if (OI.manipulatorController.getPOV() == 0) { //If d-pad is pressed up
                hatchPushPistonExtended = true; //Restricts cargo manipulator
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); //Push hatch mechanism out
                Elevator.isHatchMode = false;//Sets elevator setpoints to hatch height for rocket
            }

            if (OI.manipulatorController.getPOV() == 270){ //Checks if left on d-pad is pressed
                Elevator.dLeftPressed = true; //Sets dLeftPressed to true engages dLeftIsPressed method
                hatchPushPistonExtended = true; //Prevents ball manipulator from being used
                Elevator.isHatchMode = false; //Sets elevator setpoints to hatch height for rocket
            }

            if (OI.manipulatorController.getPOV() == 90){ //Checks if right on d-pad is pressed
                Elevator.dRightPressed = true; //Sets dRightPressed to true engages dRightIsPressed method
                hatchPushPistonExtended = true; //Prevents ball manipulator from being used
                Elevator.isHatchMode = false; //Sets elevator setpoints to hatch height for rocket
                Elevator.goal = 4;
            }

            if(OI.manipulatorController.getStartButtonPressed()){ //If the start button is pressed
                Elevator.startPressed = true; //Sets startPressed to true, which engages startIsPressed method
                hatchPushPistonExtended = true; //Prevents ball manipulator from being used
                Elevator.isHatchMode = false; //Sets elevator setpoints ot hatch height for rocket
            }
        }

    }

    /**
     * Fires periodically during test mode.
     */
    @Override
    public void testPeriodic() {
        robotMap.elevator.set(ControlMode.PercentOutput, OI.manipulatorController.getY() * .6); //Elevator Motor (throttle limited to 60%)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    }
}
