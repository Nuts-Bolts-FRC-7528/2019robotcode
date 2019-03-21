package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * Provides an automatic control loop for the Elevator.
 */
public class Elevator {
    private static int level = 1;
    private static int goal = 1;

    private static double setpoint, error, integral, drive = 0;
    private static double integrator_limit = 0;
    private static final double P = 0.8;
    private static final double I = 1.0;

    public static void reset() {
        level = 1;
        goal = 1;
        robotMap.elevatorEncoder.reset();
    }

    /**
     * Runs iteratively in teleopPeriodic() and in update() of actions
     * Note that the Elevator runs forward when going down and backwards
     * when going up - this is because the Elevator motor is flipped
     * backwards.
     */
    public static void iterate() {
        if(goal > 3) { //Checks if goal is higher than it should be
            goal = 3; //If it is, reset to highest possible level
        } else if (goal < 1) { //Checks if goal is lower than it should be
            goal = 1; //If it is, reset to lowest possible level
        }
        setSetpoint();
        PI();

        //robotMap.elevator.set(drive);

        System.out.println("Encoder value: " + robotMap.elevatorEncoder.get());
        System.out.println("Drive value: " + drive);
        System.out.println("Integral: " + integral);
        System.out.println("**********");
    }

    private static void setSetpoint() {
        if(goal == 1) {
            setpoint = 10;
        } else if (goal == 2) {
            setpoint = 100;
        } else if (goal == 3) {
            setpoint = 150;
        }
    }

    public static void setGoal(int height){
        if(height < 4 && height > 0){ //Checks if goal is between 1 and 3 inclusive
            goal = height; //Sets goal equal to the input level
        }
    }

    /**
     * Public accessor method for the current level of the Elevator
     *
     * @return The current level of the Elevator
     */
    public static int getLevel() { return level; }

    private static void PI() {

        error = setpoint - robotMap.elevatorEncoder.get();
        integral += error*.02;
        if(integral > integrator_limit) {
            integral = integrator_limit;
        } else if(integral < -integrator_limit) {
            integral = -integrator_limit;
        }
        drive = (P * error + I * integral /*+ D * derivative*/) / 100.0;
        if(drive > 0.4) {
            drive = .4;
        } else if (drive < -.3) {
            drive = -.3;
        }
    }
}