package frc.robot.auto.actions;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.common.robotMap;

public class SolenoidAction implements Action{

    @Override
    public boolean finished() {
        boolean yuh = true;
        if (yuh){
            Timer.delay(5);
            yuh = false;
        }
        if (yuh)
            return true;
        else
            return false;
    }

    @Override
    public void update() {

    }

    @Override
    public void done() {
        System.out.println("SolenoidAction complete");
        robotMap.solenoid.set(DoubleSolenoid.Value.kReverse);
    }

    @Override
    public void start() {
        System.out.println("Starting SolenoidAction");
        robotMap.solenoid.set(DoubleSolenoid.Value.kForward);
    }
}
