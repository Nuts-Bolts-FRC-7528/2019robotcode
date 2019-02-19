package frc.robot.auto;

import frc.robot.auto.actions.Action;

/**
 * Defines a base for all automodes. 
 */
public abstract class AutoModeBase {
    protected boolean active = false;
    protected double updateRate = 1.0 / 50.0;

    /**
     * Defines things to actually occour for the automode. All things the automode does lives in this method
     */
    protected abstract void routine();

    /**
     * Runs the automode
     */
    public void run() {
        active = true;
        try {
            routine();
        } catch(Exception e) {
            e.printStackTrace();
        }
        done();
        System.out.println("Auto mode complete");
    }

    public abstract void done();

    public boolean isActive() {
        return active;
    }

    public boolean isActiveThrowsException() throws AutoModeDoneException {
        if(!active) {
            throw new AutoModeDoneException();
        }

        return active;
    }

    public void runAction(Action action) throws AutoModeDoneException {
        isActiveThrowsException();
        action.start();

        while (isActiveThrowsException() &&  !action.finished()) {
            action.update();
            long waitTime = (long) (updateRate * 1000.0);

            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        action.done();
    }
}