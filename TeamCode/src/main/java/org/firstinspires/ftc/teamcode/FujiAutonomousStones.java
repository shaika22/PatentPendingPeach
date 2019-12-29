package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.hardware.Fuji;

@Autonomous(name = "FujiStones", group = "PatentPending")
public class FujiAutonomousStones extends FujiAutonomous {

    @Override
    public void runOpMode() {
        double skystone1;
        double current;
        robot = new Fuji(hardwareMap, telemetry);
        robot.drive(5, 0.5, 0, 1, false); //drive up to the stone line with a dist of 5 away
        robot.drive(7, 0.5, -1, 0, true); // drive sideways until the the distance is greater than 7

        robot.move(0.5 * STONE_LENGTH, 0);
        current = 1;
        boolean output = robot.isSkystone();
        //get the stone
        if (output) {
            skystone1 = 1;
        } else {
            robot.move(STONE_LENGTH, 0);
            current = 2;
            output = robot.isSkystone();
            if (output) {
                skystone1 = 2;
            } else {
                skystone1 = 3;
            }
        }

        robot.move(((skystone1 - 1) * STONE_LENGTH - ROBOT_EDGE_LENGTH) - (0.5 + (current - 1) * STONE_LENGTH), 0); //final minus initial
        robot.turn(0.25, 0.5);
        robot.move(-10, 0);
        robot.move(0, 0.5 * STONE_LENGTH);
        robot.move(10, 0);
        // then move robot until it sees blue tape until bridge
        // after that turn around and drop off stone a little past the bridge
        // stop/return for second stone
    }
}
