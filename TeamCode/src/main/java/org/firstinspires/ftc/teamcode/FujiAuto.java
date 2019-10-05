package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import android.graphics.Color;

//@Disabled
@Autonomous(name="FujiAuto", group="Pushbot")
public class FujiAuto extends LinearOpMode {

    // Declare timer.
    private ElapsedTime runtime = new ElapsedTime();

    // Declare OpMode members.
    private DcMotor rfMotor;
    private DcMotor lfMotor;
    private DcMotor rbMotor;
    private DcMotor lbMotor;
    private ColorSensor sensorColor;

    // Declare wheel measurements.
    private static final double PI = 3.1415;
    private static final double ROOT_TWO = 1.4142;
    private static final double WHEEL_DIAMETER_INCH = 3.5;
    private static final double INCH_PER_WHEEL_REV = WHEEL_DIAMETER_INCH * PI;
    // Declare motor measurements.
    private static final double DRIVE_SPEED = 0.7;
    private static final double COUNT_PER_REV = 1120.0;  // eg: REV Motor Encoder.
    private static final double COUNT_PER_INCH = COUNT_PER_REV / INCH_PER_WHEEL_REV;
    // Declare robot measurements.
    private static final double ROBOT_EDGE_INCH = 17.7;
    private static final double WHEEL_SQUARE_DIAGONAL_INCH = 19.0;
    private static final double INCH_PER_ROBOT_REV = WHEEL_SQUARE_DIAGONAL_INCH * PI;
    // Declare color measurements.
    private static final double COLOR_SENSOR_SCALE_FACTOR = 255.0;
    private static final float[] COLOR_SENSOR_HSV = {0F, 0F, 0F};
    // Declare field measurements.
    private static final double STONE_WALL_DISTANCE_INCH = 45.0;
    private static final double SKYSTONE_DISTANCE_STONES = 3.0;
    private static final double STONE_LENGTH_INCH = 8.0;

    // Declare block sensing.
    private double firstSkystone;
    private double currentStone = 0.0;

    @Override
    public void runOpMode() {

        // Initialize OpMode members.
        rfMotor = hardwareMap.dcMotor.get("rf");
        lfMotor = hardwareMap.dcMotor.get("lf");
        rbMotor = hardwareMap.dcMotor.get("rb");
        lbMotor = hardwareMap.dcMotor.get("lb");
        sensorColor = hardwareMap.colorSensor.get("color");

        telemetry.addData("Motors", "resetting encoders.");
        telemetry.update();
        sleep(500);

        rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Motors", "encoders done resetting.");
        telemetry.update();

        // Wait for game to start (driver presses PLAY).
        waitForStart();

        // Run autonomous.
        telemetry.addData("Path", "started.");
        telemetry.update();

        encoderDrive(DRIVE_SPEED, STONE_WALL_DISTANCE_INCH - ROBOT_EDGE_INCH, 0.0, 10.0);
        while (true) {
            if (senseBlock() == 1) {
                firstSkystone = currentStone;
                break;
            }
            currentStone++;
            if (currentStone >= SKYSTONE_DISTANCE_STONES) {
                encoderDrive (DRIVE_SPEED, 0.0, -STONE_LENGTH_INCH * (SKYSTONE_DISTANCE_STONES - 1.0), 10.0);
                currentStone = 0.0;
            } else {
                encoderDrive (DRIVE_SPEED, 0.0, STONE_LENGTH_INCH, 10.0);
            }
        }
        // Extend arm to second skystone.
        // Grab blocks.
        // Bring arm in.
        encoderDrive(DRIVE_SPEED, -20.0, 0.0, 10.0);
        encoderDrive(DRIVE_SPEED, 0.0, -firstSkystone * STONE_LENGTH_INCH - 50.0, 10.0);
        // Drop blocks.

        telemetry.addData("Path", "complete.");
        telemetry.update();
    }

    public void encoderTurn(double speed, double revolutions, double timeout) {

        // Ensure that the opMode is still active.
        if (opModeIsActive()) {

            // Declare motor targets.
            double rfInch = revolutions * INCH_PER_ROBOT_REV;
            double lfInch = revolutions * INCH_PER_ROBOT_REV;
            double rbInch = revolutions * INCH_PER_ROBOT_REV;
            double lbInch = revolutions * INCH_PER_ROBOT_REV;
            // Set targets.
            rfMotor.setTargetPosition((int)(rfInch * COUNT_PER_INCH) + rfMotor.getCurrentPosition());
            rbMotor.setTargetPosition((int)(rbInch * COUNT_PER_INCH) + rbMotor.getCurrentPosition());
            lfMotor.setTargetPosition((int)(lfInch * COUNT_PER_INCH) + lfMotor.getCurrentPosition());
            lbMotor.setTargetPosition((int)(lbInch * COUNT_PER_INCH) + lbMotor.getCurrentPosition());
            // Set motors to RUN_TO_POSITION mode.
            rfMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            lfMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            lbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // Reset the timer.
            runtime.reset();

            // Start motion.
            rfMotor.setPower(speed);
            lfMotor.setPower(speed);
            rbMotor.setPower(speed);
            lbMotor.setPower(speed);
            // keep looping while we are still active and any motors are running.
            telemetry.addData("Turn", "started turning.");
            telemetry.update();
            while (opModeIsActive() &&
                    runtime.seconds() < timeout &&
                    (rfMotor.isBusy() || lfMotor.isBusy() || rbMotor.isBusy() || lbMotor.isBusy())) {
                continue;
            }
            telemetry.addData("Turn", "done turning.");
            telemetry.update();
            // Stop all motion.
            rfMotor.setPower(0);
            lfMotor.setPower(0);
            rbMotor.setPower(0);
            lbMotor.setPower(0);

            // Turn off RUN_TO_POSITION mode.
            rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            lbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void encoderDrive(double speed, double forInch, double horiInch, double timeout) {

        // Ensure that the opMode is still active.
        if (opModeIsActive()) {

            // Declare motor targets.
            double rfInch = (+ forInch - horiInch) / ROOT_TWO;
            double lfInch = (- forInch - horiInch) / ROOT_TWO;
            double rbInch = (+ forInch + horiInch) / ROOT_TWO;
            double lbInch = (- forInch + horiInch) / ROOT_TWO;
            // Set targets.
            rfMotor.setTargetPosition((int)(rfInch * COUNT_PER_INCH) + rfMotor.getCurrentPosition());
            rbMotor.setTargetPosition((int)(rbInch * COUNT_PER_INCH) + rbMotor.getCurrentPosition());
            lfMotor.setTargetPosition((int)(lfInch * COUNT_PER_INCH) + lfMotor.getCurrentPosition());
            lbMotor.setTargetPosition((int)(lbInch * COUNT_PER_INCH) + lbMotor.getCurrentPosition());
            // Set motors to RUN_TO_POSITION mode.
            rfMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            lfMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            lbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // Reset the timer.
            runtime.reset();

            // Start motion.
            rfMotor.setPower(speed);
            lfMotor.setPower(speed);
            rbMotor.setPower(speed);
            lbMotor.setPower(speed);
            // keep looping while we are still active and any motors are running.
            telemetry.addData("Move", "started moving.");
            telemetry.update();
            while (opModeIsActive() &&
                   runtime.seconds() < timeout &&
                  (rfMotor.isBusy() || lfMotor.isBusy() || rbMotor.isBusy() || lbMotor.isBusy())) {
                continue;
            }
            telemetry.addData("Move", "done moving.");
            telemetry.update();
            // Stop all motion.
            rfMotor.setPower(0);
            lfMotor.setPower(0);
            rbMotor.setPower(0);
            lbMotor.setPower(0);

            // Turn off RUN_USING_ENCODER mode.
            rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            lbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public int senseBlock() {
        // Declare BlockID.
        int BlockID;

        // Get HSV value.
        telemetry.addData("Color Sensor", "sensing block.");
        Color.RGBToHSV((int) (sensorColor.red() * COLOR_SENSOR_SCALE_FACTOR),
                       (int) (sensorColor.green() * COLOR_SENSOR_SCALE_FACTOR),
                       (int) (sensorColor.blue() * COLOR_SENSOR_SCALE_FACTOR),
                              COLOR_SENSOR_HSV);
        telemetry.addData("Value", Float.toString(COLOR_SENSOR_HSV[2]) + ".");

        // Check which stone is sensed.
        if (COLOR_SENSOR_HSV[2] > 80) {
            BlockID = 0; // Stone.
        } else {
            BlockID = 1; // Skystone.
        }
        telemetry.addData("Block ID", Integer.toString(BlockID) + ".");

        // Return sensed block.
        telemetry.update();
        sleep(1000);
        return BlockID;
    }
}
