# 2019robotcode

This repository consists of FRC 7528's code for our 2019 robot, x. X's code is written in Java and is based off of the WPILib framework. It was written using IntelliJ IDEA and VSCode. This README file explains setup instructions, a breakdown of what each package does, as well as acknowledgments.

**Also See:** The repository [2019Vision](https://github.com/Uberlyuber/2019Vision), which contains the source code ran on our vision coprocessor. It was successfully able to track the hatch and the cargo.

## Setup ##

**Step one - All IDEs**

- Clone this repository with `git clone https://github.com/Uberlyuber/2019robotcode`
- Run `gradlew build` to build this code or `gradlew tasks` to see build options.
- Enjoy!

**Step Two - IntelliJ IDEA**

- From the main screen, click Import Project.
- Select build.gradle and click OK.
- Select "Use default gradle wrapper"
- IntelliJ should automatically download the required libraries.

**Step Two - VSCode**

- From the main screen, click "Open folder" and open the directory where you cloned the repository to.
- If you are using the FRC VSCode extension, you should be good to go from here.
- If you are not using the aforementioned extension, run `gradlew` to download all required packages automatically.

**Step Two - Other IDES**

- Open the folder you cloned the repository to.
- Run `gradlew` to download all required packages automatically.

**Step Three - Build and deploy this code**

- If you are using the FRC VSCode extension, simply use the `Build Robot Code` and `Deploy Robot Code` options from the WPILib Command palette.
- Otherwise, run `gradlew build` to build the code. Using the `--info` flag will give you more details
- Run `gradlew deploy` to deploy this code to the roboRIO.

## Package Breakdown ##

- java.frc.robot

Contains the central control classes for the robot. The `Robot` class controls all robot functions based on the state of the match.

- frc.robot.auto

Contains classes that handle the execution of automodes during the sandstorm period of each match.

- frc.robot.auto.actions

Contains singular actions (ie, drive forward, shoot ball, open solenoids, etc) that the robot executes while in autonomous mode. All Actions share an Interface entitled `Action.java` which is also present in this class.

- frc.robot.auto.modes

Contains whole automodes, which consist of a routine of autonomous actions arranged in a certain order.

- frc.robot.common

Contains code that is shared between different classes. Usually used for constants.

- frc.robot.components

Contains code for the different mechanical components of our robot.

## Acknowledgments ##

We would like to thank Team 254 for inspiring our autonomous code.

We would also like to thank the sponsors and mentors of Team 7528 for making this robot possible :)