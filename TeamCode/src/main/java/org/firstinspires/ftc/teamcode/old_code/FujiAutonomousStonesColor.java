package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.hardware.Fuji;

@Autonomous(name = "FujiStones", group = "PatentPending")
public class FujiAutonomousStonesColor extends FujiAutonomous {
    @Override
    public void runOpMode() {
        //don't put claw movements in yet, just test color sensor and movements
        double skystone;
        double current;
        current = 1; //robot always begins its plans thinking its on the first stone
//        robot = new Fuji(hardwareMap, telemetry, this);
        robot.pinch.start(1.0);
        waitForStart();

//        robot.drive(5, -0.5, 0, 1, false); //drive up to the stone line with a dist of 5 away
//        robot.drive(7, 0.5, -1, 0, true); // drive sideways until the the distance is greater than 7
//
//        robot.move(0.5 * STONE_LENGTH, 0);

        robot.move(0, 1.75 * TILE_LENGTH - ROBOT_EDGE_LENGTH);

        //get the stone
        if (robot.isSkystone()) {
            skystone = 1;
        } else {
            robot.move(STONE_LENGTH, 0);
            current = 2;
            if (robot.isSkystone()) {
                skystone = 2;
            } else {
                skystone = 3;
            }
        }
        robot.move(((skystone) * STONE_LENGTH) - ((current) * STONE_LENGTH), 5); //final minus initial
        //bring down the claw and pick up stone
        robot.pinch.start(0.0);
        sleep(1500);
        robot.move(0, -5);
        robot.move(-skystone * STONE_LENGTH - STONE_BRIDGE_DISTANCE_INCH, 0);
        robot.move(-FOUNDATION_BRIDGE_DISTANCE_INCH - FOUNDATION_LENGTH_INCH / 2, 5);
        //run to position on the arm, NO WHILE LOOP so it can be parallel
        //then drop stone
        robot.pinch.start(1.0);
        //repeat
    }
}