package frc.robot.components;

import frc.robot.common.robotMap;

public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean holdPosition = false;
    private static double integral, error = 0;

    private static final int P = 3;
    private static final int I = 3;

    public static void iterate() {
        //setpoint = 250;
        PI(setpoint);
        System.out.println("Encoder value: " + robotMap.encoderPivotOneEnc.get());
        System.out.println("Encoder rate: " + robotMap.encoderPivotOneEnc.getRate());
        System.out.println("Drive value: " + drive);
        System.out.println("Integral: " + integral);
        System.out.println("**********");
        robotMap.cargoPivotOne.set(drive);
        robotMap.cargoPivotTwo.set(drive);
    }

    public static void setSetpoint(int set) {
        setpoint = set;
    }

    public static void setHoldPosition(boolean hold) {
        holdPosition = hold;
    }

    private static void PI(double set) {
        //if(holdPosition) {
          //  error = 0 - robotMap.encoderPivotOneEnc.getRate();
        //} else {
            error = setpoint - robotMap.encoderPivotOneEnc.get();
          //  if (error < 5) {
            //    holdPosition = true;
            //}
        //}
        integral += error*.02;
        drive = (P * error + I * integral) / 100.0;
        if(drive > 0.8) { 
            drive = .8;
        } else if (drive < -.8) {
            drive = -.8;
        }
    }
}
