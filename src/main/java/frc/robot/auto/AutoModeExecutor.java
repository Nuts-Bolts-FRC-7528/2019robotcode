package frc.robot.auto;

public class AutoModeExecutor {
    private AutoModeBase autoMode;
    private Thread thread = null;

    public AutoModeExecutor(AutoModeBase mode) {
        autoMode = mode;
    }

    public void stop() {
        if(autoMode != null) {
            autoMode.stop();
        }
    }

    public void start() {
        if(thread == null) {
            thread = new Thread(autoMode);
            if(autoMode != null) {
                autoMode.run();
            }
            thread.start();
        }
    }
}
