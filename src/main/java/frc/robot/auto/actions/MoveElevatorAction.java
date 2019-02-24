package frc.robot.auto.actions;

import frc.robot.common.PIDriver;
import frc.robot.common.robotMap;
import frc.robot.components.Elevator;

import static frc.robot.common.robotMap.elevator;
import static frc.robot.common.robotMap.elevatorEncoder;

public class MoveElevatorAction implements Action {
    private int position; //Amount of encoder ticks to go to
    private int tolerance = 5; //Amount of tolerance the elevator has for slight misallignment
    private boolean finished = false;
    private boolean isCargo;
    private PIDriver driver;

    /**
     * Constructor for a new MoveElevatorAction
     * @param level The level we desire to move the elevator to
     * @param cargo Whether or not we want to place cargo. Leave false if you are placing hatches
     */
    public MoveElevatorAction(int level, boolean cargo) {
        position = level;
        isCargo = cargo;
    }

    /**
     * Returns whether or not the Action has finished
     * @return Whether the Action has finished
     */
    @Override
    public boolean finished() {
        return finished;
    }

    /**
     * Checks if the elevator has moved into the right spot yet
     */
    @Override
    public void update() {
        if(Elevator.getLevel() == position) { //If the elevator has hit the appropriate hall effect sensor
            if(isCargo) { //If we are going to place cargo (which requires we move the elevator further)
                elevatorEncoder.reset(); //Reset the encoder for the elevator
                if(driver.getError() > tolerance) { //While the PIDriver's error level is outside our tolerance range
                    driver.PIupdate(); //Update the PI loop
                    elevator.set(driver.getPI()); //Set the motor based on the output of the PI loop
                } else { //If the elevator HAS reached the cargo position...
                    finished = true; //Set the finished flag to true
                }
            } else { //If we didn't want to go to the cargo and just wanted to score a hatch...
                finished = true; //Set the finished flag to true
            }
        }
    }

    @Override
    public void done() {
        System.out.println("MoveElevatorAction Complete");
    }

    /**
     * Tells the elevator to move into position
     */
    @Override
    public void start() {
        System.out.println("Initializing MoveElevatorAction");
        Elevator.moveElevatorToPosition(position,isCargo);
        if(isCargo) {
            driver = new PIDriver(robotMap.cargoOffset, elevatorEncoder,false); //Create a new PIDriver moving it to the desired location
        }
    }
}
