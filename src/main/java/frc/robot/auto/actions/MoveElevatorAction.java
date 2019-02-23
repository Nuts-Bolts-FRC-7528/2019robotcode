package frc.robot.auto.actions;

import frc.robot.common.PIDriver;

import static frc.robot.common.robotMap.elevator;
import static frc.robot.common.robotMap.elevatorEncoder;

public class MoveElevatorAction implements Action {
    private PIDriver driver; //PIDriver to help the elevator go to the right place
    private int setpoint; //Amount of encoder ticks to go to
    private int tolerance = 5; //Amount of tolerance the elevator has for slight misallignment
    private boolean finished = false;

    public MoveElevatorAction(int level, boolean cargo) {
        if(level == 0) {

        }
    }

    @Override
    public boolean finished() {
        return finished;
    }

    //PI = P * error + I * sigma(error)
    //P = P *error
    //sigma(error) = s(error) += (error * updatetime)
    @Override
    public void update() {
        driver.PIupdate();
        elevator.set(driver.getPI());
        if(driver.getPI() == (setpoint-tolerance) || driver.getPI() == (setpoint+tolerance)) {
            finished = true;
        }
    }

    @Override
    public void done() {
        System.out.println("MoveElevatorAction Complete");
    }

    @Override
    public void start() {
        System.out.println("Initializing MoveElevatorAction");
        elevatorEncoder.reset();
    }
}
