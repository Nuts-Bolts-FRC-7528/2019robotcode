package frc.robot.components;

import frc.robot.common.robotMap;

/**
 * Creates a PID loop for the pivoting cargo arm.
 */
public class CargoCatch {
    private static double drive, setpoint = 0;
    private static boolean terminate = true;
    private static double integral, error = 0;


    //NEVER SET P,I, OR D > 1,
    // b/c iterate every 0.02 seconds == exponential change in acceleration
    // == breaks robot
    private static final double P = 0.8; //Proportional Constant
    private static final double I = 1; //Integrator Constant
    private static final double D = 0.5; //Derivative Constant
    private static final double integrator_limit = 1.0; //Used to prevent integrator windup

    /**
     * Is called by teleopPeriodic. Handles iterative logic for the arm.
     */
    public static void iterate() {
        if(!terminate) {
            PI(setpoint);
            /*System.out.println("Encoder value: " + robotMap.encoderPivotTwoEnc.get());
            System.out.println("Encoder rate: " + robotMap.encoderPivotTwoEnc.getRate());
            System.out.println("Drive value: " + drive);
            System.out.println("Integral: " + integral);
            System.out.println("Derivative: " + derivative);
            System.out.println("**********");*/
            if(setpoint < 10) {
                setpoint = 10;
            }
            robotMap.cargoPivotOne.set(drive);
            robotMap.cargoPivotTwo.set(drive);
        }
    }

    public static void reset() {
        setpoint = 0;
        robotMap.encoderPivotTwo.reset();
    }

    /**
     * Mutator method for the setpoint
     * @param down Whether to set the setpoint to go down (if true, it will attempt to go down)
     */
    public static void setSetpoint(boolean down) {
        if(down && setpoint < 250) {
            setpoint += 60;
        } else if (!down && setpoint > 0) {
            setpoint -= 60;
        }
    }

    public static void setTerminate(boolean t) {
        terminate = t;
    }

    /**
     * Handles the actual PID logic for the arm. While full PID is usually not necessary, we were getting
     * too much oscillation with the PI loop and thus decided to add the derivative
     * @param set The current setpoint
     */
    private static void PI(double set) {

        error = setpoint - robotMap.encoderPivotTwo.get();
        integral += error*.02;
        if(integral > integrator_limit) {
            integral = integrator_limit;
        } else if(integral < -integrator_limit) {
            integral = -integrator_limit;
        }
        drive = (P * error + I * integral /*+ D * derivative*/) / 100.0;
        if(drive > 0.8) { 
            drive = .2;
        } else if (drive < -.8) {
            drive = -.6;
        }
    }
}
