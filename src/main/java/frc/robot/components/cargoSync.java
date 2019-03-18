package frc.robot.components;

import frc.robot.common.OI;
import frc.robot.common.robotMap;

public class cargoSync {
    private static double P = 0.1;
    private static double I = 0.0;
    private static double D = 0.0;
    public static void go() {
    //robotMap.cargoPivitOne.setSpeed(
            System.out.println("out1: " +
                    drive(output1(robotMap.encoderPivitOne.getRate(), robotMap.encoderPivitTwo.getRate()),max()));
   // robotMap.cargoPivitTwo.setSpeed(
            System.out.println("out2: " +
                    drive(output2(robotMap.encoderPivitOne.getRate(), robotMap.encoderPivitTwo.getRate()), max()));
            System.out.println("")

}
    //the motor equation is roughly equal to percentSpeed * (AnalogInput + syncOutput + setPosOutput)
    private static double targetDistanceOrCount() {

            return 100.0;
}
private static double max() {
        return 0.8;
    }
private static double drive( double output, double maxMotorSpeed) {

        if (output < maxMotorSpeed)
            output = maxMotorSpeed;
        return output;

}


    private static double output1(double encoder1, double encoder2) {
            return  percentSpeed() *
                    (OI.manipulatorController.getY()
                                    //output of motor sync PID
                                    + motor1(encoder1, encoder2)
                                    + moveCargo(targetDistanceOrCount(), encoder1)
                    );

    }
    private static double output2(double encoder1, double encoder2) {
        return  percentSpeed() *
                (OI.manipulatorController.getY()
                                //output of motor sync PID
                                + motor2(encoder1, encoder2)
                                + moveCargo(targetDistanceOrCount(), encoder1)
                );

    }





    private static double motor1(double encoder1, double encoder2) {

        double pv1 = (robotMap.encoderPivitOne.getRate() + robotMap.encoderPivitTwo.getRate()) / 2;
        double output = pidMethod(P, I, D, RLPC(), pv1) + pidMethod(P, I, D, encoder2, encoder1);
        return output;
    }
    private static double motor2 (double encoder1, double encoder2) {

        double pv2 = (robotMap.encoderPivitOne.getRate() + robotMap.encoderPivitTwo.getRate()) / 2;
        double output = pidMethod(P, I, D, RLPC(), pv2) + pidMethod(P, I, D, encoder1, encoder2);
        return output;
    }
    private static double percentSpeed() {
        return 0.2; // % motor speed recommended max .9
    }


   // private double encoder1tix = robotMap.encoderPivitOne.getRate(); //What we read from the first motor
    //private double encoder2tix = robotMap.encoderPivitTwo.getRate(); // What we read from the second motor
    public static double pidMethod( double P, double I, double D, double sp, double pv){
        double error;
        double integral = 0;
        double integralLimit = 1;
        double derivative;
        double previous_error = 0;
        double output;


        error = sp - pv;
        integral += error * .02;
        if (integral < - integralLimit)
            integral = -integralLimit;
        if (integral > integralLimit)
            integral = integralLimit;
        derivative = (error - previous_error)/.02;
        previous_error = error;
        output = P * error + I * integral + D * derivative;
        return output/100.0;



    }




    private static double RLPC() {
        double change; // math


        // initialize this:

        double RLPC = 0;  // Rate-Limited Position Command




        double rateLimit = 1;  // allowable position change per iteration
        // rate-limit the change in position command


        change = OI.manipulatorController.getY() - RLPC;
        if(change >rateLimit)change =rateLimit;
        else if(change< -rateLimit)change =-rateLimit;
        RLPC +=change;
        return RLPC;
    }
    private static double moveCargo(double targetcount, double encoder1) {


        return pidMethod(P,I,D, targetcount, encoder1);
    }
}