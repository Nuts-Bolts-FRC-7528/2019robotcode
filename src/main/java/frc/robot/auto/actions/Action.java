package frc.robot.auto.actions;

/**
 * Action Interface, an interface that describes an iterative action. It is run by an autonomous action, called by the
 * method runAction in AutoModeBase (or more commonly in autonomous modes that extend AutoModeBase)
 */
public interface Action {
    /**
     * Returns whether or not the action is finished
     */
    public abstract boolean finished();

    /**
     * Called by runAction in AutoModeBase iteratively until finished is true. Iterative logic goes into this method
     */
    public abstract void update();

    /**
     * Runs code when the action completes
     */
    public abstract void done();

    /**
     * Runs code when the action starts
     */
    public abstract void start();
}