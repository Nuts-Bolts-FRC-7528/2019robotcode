package frc.robot.components;

import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import frc.robot.common.OI;
import frc.robot.common.robotMap;

import static edu.wpi.first.wpilibj.GenericHID.Hand.kRight;


public class CargoCatch extends PIDSubsystem {

    PWMVictorSPX motor = robotMap.cargoPivitOne;



    public CargoCatch() {
        super("CargoCatch", 2.0, 0.0, 0.0);
        setAbsoluteTolerance(0.05);
        getPIDController().setContinuous(false);

    }

    public void initDefaultCommand() {

    }

    @Override
    protected double returnPIDInput() {
        return robotMap.encoderPivitOne.getDistance() - robotMap.encoderPivitTwo.getDistance();
    }

    @Override
    protected void usePIDOutput(double output) {
        motor.pidWrite(output);

    }

    public void returnPIDOutput(double output) {
        robotMap.cargoPivitOne.setSpeed(OI.manipulatorContoller.getY(kRight) + output);


    }

/*
        driveTrain.arcadeDrive(baseSpeed, output);
    }

}
*/



}
