package frc.robot.components;

import frc.robot.common.robotMap;

public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean holdPosition = true;
    private static  double integral, error = 0;

    private static final int P = 1;
    private static final int I = 1;

    public static void iterate() {
        PI(setpoint);
        System.out.println(drive);
        //robotMap.cargoPivotOne.set(drive);
        //robotMap.cargoPivotTwo.set(drive);
    }

    public static void setSetpoint(int set) {
        setpoint = set;
    }

    public static void setHoldPosition(boolean hold) {
        holdPosition = hold;
    }

    private static void PI(double set) {
        if(holdPosition) {
            error = 0 - robotMap.encoderPivotOneEnc.getRate();
        } else {
            error = set - robotMap.encoderPivotOneEnc.get();
            if (error < 5) {
                holdPosition = true;
            }
        }
        integral += error*.02;
        drive = P*error + I * integral;
    }
}
