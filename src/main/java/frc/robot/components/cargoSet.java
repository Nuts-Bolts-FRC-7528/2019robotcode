package frc.robot.components;

import static frc.robot.components.cargoSync.pidMethod;

public class cargoSet {

    private static double desired_distance = 0;

    public static void moveCargo(double encoder1) {

        pidMethod(0.1,0.0,0.0,encoder1,)
    }
}
