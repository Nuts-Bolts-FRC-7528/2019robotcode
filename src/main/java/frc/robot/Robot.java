package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.robot.common.OI;
import frc.robot.common.robotMap;
import frc.robot.components.CargoCatch;
import frc.robot.components.Drivetrain;
import frc.robot.components.Elevator;

public class Robot extends TimedRobot {
    private final SpeedControllerGroup m_left = new SpeedControllerGroup(robotMap.leftFrontDrive, robotMap.leftRearDrive);
    //Defines a SpeedControllerGroup for the left side
    private final SpeedControllerGroup m_right = new SpeedControllerGroup(robotMap.rightFrontDrive, robotMap.rightRearDrive);
    //Defines a SpeedControllerGroup for the right drive
    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
    //Creates a DifferentialDrive using both SpeedControllerGroups
    public static boolean pistonExtended = false;
    private NetworkTable table; //This table is for object recognition

    @Override
    public void robotInit() {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        table = ntinst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        CameraServer.getInstance().startAutomaticCapture(); //This likes to be red. Activates camera
    }

    @Override
    public void autonomousInit() {
        Elevator.reset(); //Reset elevator position and encoder
        CargoCatch.reset(); //Reset manipulator position and encoder
    }

    @Override
    public void autonomousPeriodic() {
        teleopPeriodic(); //Run all normal teleop functions
    }

    @Override
    public void teleopInit() {
        CargoCatch.reset(); //Temporary reset for easy testing of cargo
        Elevator.reset(); //Temporary reset for easy testing of elevator
        pnuematicsProtectionTimer = 0;
    }

    /*
        The hatch mechanism sometimes slides out when the robot is enabled. I believe that this has to do something with pneumatics and how unreliable
        it is. It's random when this happens. Using this so that when this timer reaches a certain time, the system will automatically retract
         */
    public static int pnuematicsProtectionTimer;

    @Override
    public void teleopPeriodic() { //Happens roughly every 1/20th of a second while teleop is active


        /*  [PNUEMATICS STARTUP PROTECTION] */

        //This is mostly for testing to ensure that the hatch mechanism
        // automatically retracts so we don't break it


        pnuematicsProtectionTimer++; //Increments pneumaticsProtectionTimer
        if(pnuematicsProtectionTimer == 60){ //Once the timer reaches 70 ticks
            robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse); //Pull the claw back in
        }
        if(pnuematicsProtectionTimer == 80){
            robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); //Pull the hatch mechanism back in
        }


        /*  [ROBOT DRIVING] */


        m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Actually drives the robot. Uses the joystick.
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y
        robotMap.elevator.setSpeed(OI.manipulatorController.getY(GenericHID.Hand.kRight) * .5); //Allows manual control of the elevator

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




        /*  [MANIPULATOR USE]   */


        if(!pistonExtended) {
            if (OI.manipulatorController.getAButtonPressed()) { //If A button is pressed...
//                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse);
                CargoCatch.setSetpoint(true); //...go down

            }

            if (OI.manipulatorController.getBButtonPressed()) { //If B button is pressed...
                CargoCatch.setSetpoint(false); //...go up
            }

            if (OI.manipulatorController.getXButtonPressed()) { //If X button is pressed
                CargoCatch.xPressed = true; //Set xPressed to true(used in xIsPressed method)
                CargoCatch.setInMotorHolding = false; //Prevent motors from sucking
                CargoCatch.setInMotorPickUp = false; //Prevent motors from sucking x2
            }
        }

        robotMap.cargoIntake.set(OI.manipulatorController.getY(GenericHID.Hand.kLeft) / 2);//Run the intake wheels


        /*  [ITERATING METHODS] */


        CargoCatch.xIsPressed();//Runs the intake to pop out ball
        Elevator.iterate(); //Update where the elevator should be
        CargoCatch.iterate(); //Update where the cargo manipulator should be


        /*  [OBJECT RECOGNITION]    */


        NetworkTableEntry centerCargo = table.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
        NetworkTableEntry centerHatch = table.getEntry("hatchCenterPix"); //Fetch NetworkTableEntry for center pixel of hatch
        NetworkTableEntry centerTargets = table.getEntry("vtCenterPix"); //Fetch NetworkTablEntry for center pixel of vision targets
        int ballCenterPix = (int) centerCargo.getDouble(-1); //Gets the actual number from the cargo NetworkTableEntry
        int hatchCenterPix = (int) centerHatch.getDouble(-1); //Gets the actual number from the hatch NetworkTableEntry
        int vtCenterPix = (int) centerTargets.getDouble(-1); //Gets the actual number from the vision targets NetworkTableEntry


        /*  [CARGO ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(2)) { //If thumb button is pressed
            Drivetrain.align(ballCenterPix); //Align to cargo
        }


        /*  [HATCH ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(3)) { //If button 3 is pressed
            Drivetrain.align(hatchCenterPix); //Align to hatch
        }


        /*  [VISION TARGET ALIGNMENT]   */


        if (OI.driveJoystick.getRawButton(1)) { //If trigger is pressed
            Drivetrain.align(vtCenterPix); //Align to vision targets
        }


        /*  [PNEUMATICS]    */

        Elevator.yIsPressed();
        if (CargoCatch.getSetpoint() == CargoCatch.MinSetpoint && robotMap.encoderPivotTwo.get() < CargoCatch.MinSetpoint + 40) { //If cargo manipulator is trying to go up
            if (OI.manipulatorController.getBumperPressed(GenericHID.Hand.kLeft)) { //If left bumper pressed
                pistonExtended = true;
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //Push out hatch catching solenoid
            }

            if (OI.manipulatorController.getBumperPressed(GenericHID.Hand.kRight)) { //If right bumper pressed
                pistonExtended = true;
                Elevator.subSetpoint();
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse);//Pull in hatch catching solenoid
            }

            if (OI.manipulatorController.getPOV() == 180) { //If d-pad is pressed down
                pistonExtended = false;
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse); //Pull hatch mechanism in
            }

            if (OI.manipulatorController.getPOV() == 0) { //If d-pad is pressed up
                pistonExtended = true;
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward); //Push hatch mechanism out
            }

            if (OI.manipulatorController.getYButtonPressed()){
                Elevator.yPressed = true;
                pistonExtended = false;

            }
        }

    }


    @Override
    public void robotPeriodic() {
//        System.out.println("Right analog stick: " + OI.manipulatorController.getY(GenericHID.Hand.kRight) * .5);
    }


    @Override
    public void testPeriodic() {
        robotMap.elevator.setSpeed(OI.manipulatorController.getY() * .6); //Elevator Motor (throttle limited to 60%)
        m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Drives the robot arcade style using the joystick
    }
}
