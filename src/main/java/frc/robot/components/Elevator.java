package frc.robot.components;

import frc.robot.common.PIDriver;
import frc.robot.common.robotMap;

public class Elevator {
    /*
            Elevator stages:
            0 - Resting
            1 - Rocket level 1 / Cargo Ship
            2 - Rocket level 2
            3 - Rocket level 3
     */
    private static int level = 0;
    private static boolean isAtCargo = false;

    /**
     * Public accessor for the encoder position that the elevator is currently at
     * @return The amount of encoder ticks the elevator is currently at
     */
    public static int getLevel() { return level; }

    /**
     * Manually moves the elevator.
     *
     * @param speed The PWM signal to send to the motor controller. Valid ranges are -1.0 to 1.0
     */
    public static void moveElevator(double speed) {
        robotMap.elevator.set(speed); //Set the elevator motor to the desired speed
        isAtCargo = false; //Set isAtHatch to false as we can't verify that a human will put it in the exact spot
    }

    /**
     * Moves the level variable up or down by one. Should only be called in teleopPeriodic()
     *
     * @param down If the elevator is moving DOWN
     */
    public static void setLevel(boolean down) {
        if(down) {
            level--;
        } else {
            level++;
        }
    }

    /**
     * Moves the elevator to a certain position
     * @param desiredLevel The level you wish to move the elevator to
     * @param isCargo Set this to true if you wish to move the elevator to a scoring position for the cargo
     */
    public static void moveElevatorToPosition(int desiredLevel, boolean isCargo) {
        System.out.println("Beginning elevator placement to level " + desiredLevel);
        boolean isDown = false;
        if(desiredLevel < level) { isDown = true; }
        while(desiredLevel != level) {
            if(isDown) {
                robotMap.elevator.set(-0.5);
            } else {
                robotMap.elevator.set(0.5);
            }
        }
        if(isCargo) {
            robotMap.elevatorEncoder.reset();
            PIDriver driver = new PIDriver(robotMap.cargoOffset,robotMap.elevatorEncoder,false);
            while(driver.getError() > 1) {
                driver.PIupdate();
                robotMap.elevator.set(driver.getPI());
            }
            isAtCargo = true;
            System.out.println("Elevator placement complete");
        } else {
            System.out.println("Elevator placement complete");
            isAtCargo = false;
        }
    }
}
