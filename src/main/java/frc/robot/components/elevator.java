package frc.robot.components;

import edu.wpi.first.wpilibj.command.PIDSubsystem;
import frc.robot.common.robotMap;

public class Elevator extends PIDSubsystem {
    public Elevator() {
        super("Elevator",1.0,0.0,0.0,12/6000.0);
        setAbsoluteTolerance(0.05);

    }

    @Override
    public void initDefaultCommand() { }

    @Override
    public double returnPIDInput() {
        return robotMap.enc.get();
    }

    @Override
    public void usePIDOutput(double output) {
        robotMap.manipulatorA.pidWrite(output);
    }
}