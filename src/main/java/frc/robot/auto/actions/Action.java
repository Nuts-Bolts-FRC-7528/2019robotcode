package frc.robot.auto.actions;

/**
 * This interface sets the base methods that an Action is required to have. All Actions must override the methods in
 * this interface
 */
public interface Action {
    /**
     * Returns whether or not the action is finished. When your action has completed performing its task, this
     * needs to return true.
     *
     * @return Whether the action has finished or not
     */
    boolean finished();

    /**
     * Called by runAction in AutoModeBase iteratively until finished is true. Iterative logic goes into this method
     */
    void update();

    /**
     * Runs code when the action completes
     */
    void done();

    /**
     * Runs code when the action starts
     */
    void start();
}