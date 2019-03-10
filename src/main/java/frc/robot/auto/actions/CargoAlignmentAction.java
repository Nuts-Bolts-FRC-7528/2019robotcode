package frc.robot.auto.actions;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import frc.robot.common.robotMap;
import frc.robot.components.drivetrain;

public class CargoAlignmentAction implements Action {

    public CargoAlignmentAction() {

    }

    @Override
        public boolean finished() {
            if (robotMap.photodiode.get() == false);{
            return true;}
        }
        
    
    NetworkTable table; // Used for object recognition

    /**
     * Fetches the NetworkTableEntry for the centerPix of the cargo, gets actual
     * number from network table entry, then drives in the direction of cargo.
     */
    @Override
    public void update() {
        NetworkTableEntry centerCargo = table.getEntry("cargoCenterPix");
        int ballCenterPix = (int) centerCargo.getDouble(0);

        drivetrain.align(ballCenterPix);
    }

    @Override
    public void done() {
    }

    @Override
    public void start() {
    }

   
}