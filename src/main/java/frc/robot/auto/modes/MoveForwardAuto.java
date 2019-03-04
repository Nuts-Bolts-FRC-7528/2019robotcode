package frc.robot.auto.modes;

import frc.robot.auto.AutoModeBase;
import frc.robot.auto.AutoModeDoneException;
import frc.robot.auto.actions.DriveForwardAction;
import frc.robot.auto.actions.WaitAction;

/**
 * Runs the motors forward for 5 seconds and then halts until the end of the sandstorm period
 */
public class MoveForwardAuto extends AutoModeBase {

    @Override
    protected void routine() throws AutoModeDoneException {
        runAction(new DriveForwardAction(.2,5));
        runAction(new WaitAction(10));
    }
}