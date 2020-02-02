package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.hardware.Fuji;

@Autonomous(name = "FujiFoundationParkLeft", group = "PatentPending")
public class FujiAutonomousParkLeft extends FujiAutonomous{
    @Override
    public void runOpMode() throws InterruptedException {
        Fuji robot = new Fuji(hardwareMap, telemetry, this);
        robot.dropStone.start(0.5);
        waitForStart();
        robot.move(TILE_LENGTH, 0);

    }
}
