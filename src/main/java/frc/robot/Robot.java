package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
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

    private NetworkTable table; //This table is for object recognition

    @Override
    public void robotInit() {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault(); //Gets global NetworkTable instance
        table = ntinst.getTable("vision"); //Gets vision table from vision coprocessor (Raspberry Pi)
        //getInstance().startAutomaticCapture();
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
        CargoCatch.reset(); //Temporary reset for easy testing of PID loop(so we don't have to reset robot code everytime we enable)
    }

    public static boolean toggleOnAButton = false;
    public static boolean togglePressedAButton = false;
    public static boolean toggleOnBButton = false;
    public static boolean togglePressedBButton = false;

    @Override
    public void teleopPeriodic() { //Happens roughly every 1/20th of a second while teleop is active
        /*
            Checks if AButton on manipulator controller was pressed previously
            if it was pressed, engages CargoInTake motors
            //only AButton for now, will work out B button later, skater
         */
//        frc.robot.components.CargoCatch.updateToggleAButton();
//
//        if (toggleOnAButton) {
//            robotMap.cargoIntake.set(0.5);
//        } else {
//            robotMap.cargoIntake.set(0);
//        }
//        /*
//        Checks if B Button is pressed
//        if it was pressed, engages CargoIntake motos
//         */
//        frc.robot.components.CargoCatch.updateToggleBButton();
//
//        if (toggleOnBButton) {
//            robotMap.cargoIntake.set(0.2);
//        } else {
//            robotMap.cargoIntake.set(0);
//        }
        /*
                [ROBOT DRIVING]
         */
        m_drive.arcadeDrive((-OI.driveJoystick.getY()), (OI.driveJoystick.getX())); //Actually drives the robot. Uses the joystick.
        //We suspect that there may be an issue with the Joystick, b/c it is inverted/reversed. We resolved this by flipping Y,X to X,Y and putting a negative on Y
        robotMap.elevator.setSpeed(OI.manipulatorController.getY(GenericHID.Hand.kRight) * .5); //Allows manual control of the elevator

        if (OI.driveJoystick.getRawButtonPressed(7)) { //If joystick button 7 is pressed
            Elevator.setGoal(3); //Sets the Elevator to level 3
        }
        if (OI.driveJoystick.getRawButtonPressed(9)) { //If joystick button 9 is pressed
            Elevator.setGoal(2); //Sets the Elevator to level 2
        }
        if (OI.driveJoystick.getRawButtonPressed(11)) { //If joystick button 11 is pressed
            Elevator.setGoal(1); //Sets the Elevator to level 1
        }

        if (OI.manipulatorController.getAButtonPressed()) { //If A button is pressed...
            CargoCatch.setSetpoint(true); //...go down
        }
        if (OI.manipulatorController.getBButtonPressed()) { //If B button is pressed...
            CargoCatch.setSetpoint(false); //...go up
        }
        robotMap.cargoIntake.set(OI.manipulatorController.getY(GenericHID.Hand.kLeft) / 2); //Run the intake wheels

        Elevator.iterate(); //Update where the elevator should be
        CargoCatch.iterate(); //Update where the cargo manipulator should be

        /*
                [OBJECT RECOGNITION]
         */
        NetworkTableEntry centerCargo = table.getEntry("cargoCenterPix"); //Fetch the NetworkTableEntry of the centerPix of the cargo from the coprocesor
        NetworkTableEntry centerHatch = table.getEntry("hatchCenterPix"); //Fetch NetworkTableEntry for center pixel of hatch
        NetworkTableEntry centerTargets = table.getEntry("vtCenterPix"); //Fetch NetworkTablEntry for center pixel of vision targets
        int ballCenterPix = (int) centerCargo.getDouble(-1); //Gets the actual number from the cargo NetworkTableEntry
        int hatchCenterPix = (int) centerHatch.getDouble(-1); //Gets the actual number from the hatch NetworkTableEntry
        int vtCenterPix = (int) centerTargets.getDouble(-1); //Gets the actual number from the vision targets NetworkTableEntry

        //CARGO ALIGNMENT
        if (OI.driveJoystick.getRawButton(2)) { //If thumb button is pressed
            Drivetrain.align(ballCenterPix); //Align to cargo
        }

        //HATCH ALIGNMENT
        if (OI.driveJoystick.getRawButton(3)) { //If button 3 is pressed
            Drivetrain.align(hatchCenterPix); //Align to hatch
        }

        //VISION TARGET ALIGNMENT
        if (OI.driveJoystick.getRawButton(1)) { //If trigger is pressed
            Drivetrain.align(vtCenterPix); //Align to vision targets
        }

        /*
                [PNEUMATICS]
         */

        if (CargoCatch.getSetpoint() < 20) { //If cargo manipulator is trying to go up
            if (OI.manipulatorController.getBumperPressed(GenericHID.Hand.kLeft)) { //If left bumper pressed
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kForward); //Push out hatch catching solenoid
            }

            if (OI.manipulatorController.getBumperReleased(GenericHID.Hand.kRight)) {
                robotMap.hatchCatch.set(DoubleSolenoid.Value.kReverse);
            }

            if (OI.manipulatorController.getPOV() == 0) {
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kForward);
            }

            if (OI.manipulatorController.getPOV() == 180) {
                robotMap.hatchPushOne.set(DoubleSolenoid.Value.kReverse);
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
