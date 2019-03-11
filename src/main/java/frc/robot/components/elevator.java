package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * Provides an automatic control loop for the elevator. 
 */
public class Elevator {
    private static int level = 1;
    private static int goal = 1;

    public static void reset() {
        level = 1;
        goal = 1;
    }

    /**
     * Runs iteratively in teleopPeriodic() and in update() of actions
     * Note that the elevator runs forward when going down and backwards
     * when going up - this is because the elevator motor is flipped
     * backwards.
     */
    public static void iterate() {
        if(goal > 2) { //Checks if goal is higher than it should be
            goal = 2; //If it is, reset to highest possible level
        } else if (goal < 1) { //Checks if goal is lower than it should be
            goal = 1; //If it is, reset to lowest possible level
        }
        if (goal > level) { //If goal level is HIGHER than the current level
            robotMap.elevator.setSpeed(-.6); //Run elevator upwards (runs backwards because motor is flipped around)
        }
        else if (goal < level) { //If goal level is LOWER than the current level
            robotMap.elevator.setSpeed(.6); //Run elevator downwards (runs forwards because motor is flipped around)
        }
        if(robotMap.elevatorBottom.get()) { //If the limit switch on the bottom is hit
            level = 1; //Set the level value to one
        }
        if(!robotMap.elevatorMiddle.get()) { //If the hall effect sensor on the middle is hit (flipped because that's how the sensor is)
            level = 2; //Set the level value to two
        }
    }

    /**
     * Public mutator method to set the goal level. Will set it up or down one level at a time
     *
     * @param up If this is true, it will increment the goal by one. If not, it will decrement it by one
     */
    public static void setGoal(boolean up) {
        if(up && level < 3) { 
            goal++; 
        } else if (!up && level > 0) {
            goal--;
        }
    }

    /**
     * Public accessor method for the current level of the elevator
     *
     * @return The current level of the elevator
     */
    public static int getLevel() { return level; }
}