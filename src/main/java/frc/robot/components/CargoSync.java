package frc.robot.components;

import frc.robot.common.OI;
import frc.robot.common.robotMap;

public class cargoSync {
    public static void synchronize() {
        motor1(robotMap.encoderPivitOne.getRate(),robotMap.encoderPivitTwo.getRate());
        motor2(robotMap.encoderPivitOne.getRate(), robotMap.encoderPivitTwo.getRate());
    }

    public static void motor1(double encoder1, double encoder2) {
        double P1 = 0.1;
        double I1 = 0.0;
        double D1 = 0.0;
        double pv1 = (robotMap.encoderPivitOne.getRate() + robotMap.encoderPivitTwo.getRate()) / 2;
        double output = pidMethod(P1, I1, D1, RLPC(), pv1) + pidMethod(P1, I1, D1, encoder2, encoder1);
        robotMap.cargoPivitOne.setSpeed((percentSpeed()*OI.manipulatorContoller.getY())+output);
    }
    public static void motor2 (double encoder1, double encoder2) {
        double P2 = 0.1;
        double I2 = 0.0;
        double D2 = 0.0;
        double pv2 = (robotMap.encoderPivitOne.getRate() + robotMap.encoderPivitTwo.getRate()) / 2;
        double output = pidMethod(P2, I2, D2, RLPC(), pv2) + pidMethod(P2, I2, D2, encoder1, encoder2);
        robotMap.cargoPivitTwo.setSpeed((percentSpeed()*OI.manipulatorContoller.getY())+output);
    }
    public static double percentSpeed() {
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
        integral += error;
        if (integral < - integralLimit)
            integral = -integralLimit;
        if (integral > integralLimit)
            integral = integralLimit;
        derivative = error - previous_error;
        previous_error = error;
        output = P * error + I * integral + D * derivative;
        return output;



    }




    public static double RLPC() {
        double change; // math
        double PosCmd = OI.manipulatorContoller.getY(); // THE DESIRED INPUT OF THE MANIPULATOR

        // initialize these:

        double RLPC = 0;  // Rate-Limited Position Command




        double rateLimit = 1;  // allowable position change per iteration
        // rate-limit the change in position command


        change =PosCmd -RLPC;
        if(change >rateLimit)change =rateLimit;
        else if(change< -rateLimit)change =-rateLimit;
        RLPC +=change;
        return RLPC;
    }


/*
                [ROBOT CARGO-PICKUP]
         */

    //I reallly really hope this works - By Kobe Nguyen

    ////////////////////////////////////////////////////////////////////////////////



    //These variables are part of the math i.e. no touch!
    double error1a;
    double error1ai = 0;
    double error1ad = 0;
    double previous_error1a = 0;
    double cmd1a;

    double error1b;
    double error1bi = 0;
    double error1bd = 0;
    double previous_error1b = 0;
    double cmd1b;

    double error2a;
    double error2ai = 0;
    double error2ad = 0;
    double previous_error2a = 0;
    double cmd2a;

    double error2b;
    double error2bi = 0;
    double error2bd = 0;
    double previous_error2b = 0;
    double cmd2b;


    //The calculated (output) in getRate() which the motors should be run at.
    double cmd1;  // motor1
    double cmd2;   // motor2



    // tuning constants:
    double i1aLimit = 1;     // integrator clamp for M1 a
    double i1bLimit = 1;     // integrator clamp for M1 b

    double i2aLimit = 1;   // integrator clamping value for M2 a
    double i2bLimit = 1;   // integrator clamping value for M2 b

    // Theoretically, the gains should all share the same constant, so if you want, you should change them to 1 variable

    double Kp1a = 1;  // PID gains 1a
    double Ki1a = 0;
    double Kd1a = 0;

    double Kp1b = 1;  // PID gains 1b
    double Ki1b = 0;
    double Kd1b = 0;


    double Kp2a = 1; // PID gains 2a
    double Ki2a = 0;
    double Kd2a = 0;


    double Kp2b = 1; // PID gains 2b
    double Ki2b = 0;
    double Kd2b = 0;


// this is your control loop:
/*


// PID controller for M1

    //part1 Motor1 PID //see Ether's graphic and Java PID wikipedia article

    error1a =RLPC -(e1pos +e2pos)/2; //closed loop error
    error1ai +=error1a; // integrate the error
        if(error1ai >i1aLimit)error1ai =i1aLimit; //clamp the integrated error
        else if(error1ai< -i1aLimit)error1ai =-i1aLimit;
    error1a =error1a -previous_error1a; //rate of change in error1 for D term
    previous_error1a =error1a; // save for next iteration
    cmd1a =Kp1a *error1a +Ki1a *error1ai +Kd1a *error1ad;

    //part2 Motor 1 PID partial master/slave input
    error1b =e2pos -e1pos; //closed loop error
    error1bi +=error1b; // integrate the error
        if(error1bi >i1aLimit)error1bi =i1bLimit; // clamp the integrated error
        else if(error1bi< -i1bLimit)error1bi =-i1bLimit;
    error1bd =error1b -previous_error1a; // rate of change in error1a for D tern
    previous_error1a =error1b; //save for next iteration
    cmd1b =Kp1b *error1b +Ki1b *error1bi +Kd1b *error1bd;

    cmd1 =0.9*(cmd1a +cmd1b); // limit to 90% b/c 100% motor power is always different
// PID controller for M2

    //part1 motor1
    error2a =RLPC -(e1pos +e2pos)/2;//closed loop error
    error2ai +=error2a;// integrate the error
        if(error2ai >i2aLimit)error2ai =i2aLimit;// clamp the integrated error
        else if(error2ai< -i2aLimit)error2ai =-i2aLimit;
    error2a =error2a -previous_error2a;// rate of change in error1a for D tern
    previous_error2a =error2a;//save for next iteration
    cmd2a =Kp2a *error2a +Ki2a *error2ai +Kd2a *error2ad;

    //part 2 Motor2
    error2b =e1pos -e2pos;//closed loop error
    error2bi +=error2b;// integrate the error
        if(error2bi >i2bLimit)error2bi =i2bLimit;// clamp the integrated error
        else if(error2bi< -i2bLimit)error2bi =-i2bLimit;
    error2b =error2b -previous_error2b;// rate of change in error1a for D tern
    previous_error2b =error2a;//save for next iteration
    cmd2b =Kp2b *error2b +Ki2b *error2bi +Kd2b *error2bd;

    cmd2 =0.9*(cmd2a +cmd2b); // limit to 90% b/c 100% motor power is ALWAYS different

    ///////////////////////////////////////////////////
    double howfast = .2; // constant for base motor speed.
    // Ideally, the output of the PID should ADD onto baseSpeed,
    // however, I am not sure sure, more like kinda sure, but not really.
    // Just know that it also happens to be a double :D

        robotMap.cargoPivitTwo.setSpeed(OI.manipulatorContoller.getY()*howfast+cmd1);
        robotMap.cargoPivitOne.setSpeed(OI.manipulatorContoller.getY()*howfast+cmd2);


/////////////////////////////////////////////////////////
    */
}