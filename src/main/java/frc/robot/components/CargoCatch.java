package frc.robot.components;

import frc.robot.common.robotMap;

public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean holdPosition = false;
    private static double integral, error, derivative, previous_error = 0;


    //NEVER SET P,I, OR D > 1,
    // b/c iterate every 0.02 seconds == exponential change in acceleration
    // == breaks robot
    private static final double P = 0.5;
    private static final double I = 0.5;
    private static final double D = 0.1;
    

    public static void iterate() {
        //setpoint = 250;
        
        PID(setpoint);
        System.out.println("Encoder value: " + robotMap.encoderPivotOneEnc.get());
        System.out.println("Encoder rate: " + robotMap.encoderPivotOneEnc.getRate());
        System.out.println("Drive value: " + drive);
        System.out.println("Integral: " + integral);
        System.out.println("Derivative: " + derivative);
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

    private static void PID(double set) {
        
        //if(holdPosition) {
          //  error = 0 - robotMap.encoderPivotOneEnc.getRate();
        //} else {
            error = setpoint - robotMap.encoderPivotOneEnc.get();
          //  if (error < 5) {
            //    holdPosition = true;
            //}
        //}
        integral += error*.02;
        derivative = (error - previous_error)/.02;
        previous_error = error;
        drive = (P * error + I * integral + D * derivative) / 100.0;
        if(drive > 0.8) { 
            drive = .8;
        } else if (drive < -.8) {
            drive = -.8;
        }
    }
}
