package frc.robot.auto.modes;

import frc.robot.auto.AutoModeBase;
import frc.robot.auto.AutoModeDoneException;
import frc.robot.auto.actions.DriveForwardAction;
import frc.robot.auto.actions.WaitAction;

public class MoveForwardAuto extends AutoModeBase {

    @Override
    protected void routine() throws AutoModeDoneException {
        runAction(new DriveForwardAction(.2,5));
        runAction(new WaitAction(10));
    }

    @Override
    public void done() {}

}