package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import android.graphics.Color;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="FujiAutoStones", group="PatentPending")
public class FujiAutoStones extends LinearOpMode {

    // Declare timer.
    private ElapsedTime runtime = new ElapsedTime();

    // Declare OpMode members.
    private DcMotor rfMotor;
    private DcMotor lfMotor;
    private DcMotor rbMotor;
    private DcMotor lbMotor;
    private DcMotor extender;
    private DcMotor hinge;
    private ColorSensor sensorColor;
    private DistanceSensor sensorDistance;

    // Declare wheel measurements.
    private static final double PI = 3.1415;
    private static final double ROOT_TWO = 1.4142;
    private static final double WHEEL_DIAMETER_INCH = 3.5;
    private static final double GEAR_RATIO = 1; // Gear Ratio on the motors, should be 1 or greater if gearing faster
    private static final double INCH_PER_WHEEL_REV = (WHEEL_DIAMETER_INCH * PI);
    // Declare motor measurements.
    private static final double DRIVE_SPEED = 0.7;
    private static final double COUNT_PER_REV = 1120.0; // eg: REV Motor Encoder.
    private static final double COUNT_PER_INCH = COUNT_PER_REV / (INCH_PER_WHEEL_REV * GEAR_RATIO);
    private static final double ARM_COUNT_PER_INCH = COUNT_PER_REV / INCH_PER_WHEEL_REV;
    // Declare robot measurements.
    private static final double ROBOT_EDGE_INCH = 17.7;
    // Declare color measurements.
    private static final double COLOR_SENSOR_SCALE_FACTOR = 255.0;
    private static final float[] COLOR_SENSOR_HSV = {0F, 0F, 0F};
    // Declare field measurements.
    private static final double STONE_BRIDGE_DISTANCE_INCH = 24.0;
    private static final double STONE_WALL_DISTANCE_INCH = 47.0;
    private static final double SKYSTONE_DISTANCE_STONES = 3.0;
    private static final double STONE_LENGTH_INCH = 9.0;

    // Declare block sensing.
    private double currentStone = 0.0;

    @Override
    public void runOpMode() {

        // Initialize OpMode members.
        rfMotor = hardwareMap.dcMotor.get("rf");
        lfMotor = hardwareMap.dcMotor.get("lf");
        rbMotor = hardwareMap.dcMotor.get("rb");
        lbMotor = hardwareMap.dcMotor.get("lb");
        extender = hardwareMap.dcMotor.get("ext");
        hinge = hardwareMap.dcMotor.get("hin");
        sensorColor = hardwareMap.colorSensor.get("color");
        sensorDistance = hardwareMap.get(DistanceSensor.class, "color");

        telemetry.addData("Motors", "resetting encoders.");
        telemetry.update();
        sleep(500);

        rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        extender.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hinge.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lfMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        extender.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hinge.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Motors", "encoders done resetting.");
        telemetry.update();

        // Wait for game to start (driver presses PLAY).
        waitForStart();

        // Run autonomous.
        telemetry.addData("Path", "started.");
        telemetry.update();

	    // Go up to stones.
        encoderDrive(DRIVE_SPEED, STONE_WALL_DISTANCE_INCH - ROBOT_EDGE_INCH, 0.0, 10.0);

        // Drive sideways until the robot reaches the end of the stone line.
        driveOn(0.0, -DRIVE_SPEED);
        while (sensorDistance.getDistance(DistanceUnit.INCH) < 5.0) {}
        driveOn(0.0, 0.0);
        
        // Move to the middle of the first stone.
        encoderDrive (DRIVE_SPEED, 0.0, STONE_LENGTH_INCH / 2  , 10.0);

	    // Start sensing stones.
        while (senseBlock() != 1) {
            currentStone++;
            if (currentStone >= SKYSTONE_DISTANCE_STONES) {
                encoderDrive (DRIVE_SPEED, 0.0, -STONE_LENGTH_INCH * (SKYSTONE_DISTANCE_STONES - 1.0), 10.0);
                currentStone = 0.0;
            } else {
                encoderDrive (DRIVE_SPEED, 0.0, STONE_LENGTH_INCH, 10.0);
            }
        }

        // Grab stone here.
        encoderDrive(DRIVE_SPEED, STONE_WALL_DISTANCE_INCH - ROBOT_EDGE_INCH, 0.0, 10.0);
        encoderDrive(DRIVE_SPEED, 0.0,
             -currentStone * STONE_LENGTH_INCH - STONE_BRIDGE_DISTANCE_INCH - STONE_LENGTH_INCH / 2,
             10.0);
        // Drop stone here.
        encoderDrive(DRIVE_SPEED, 0.0, ROBOT_EDGE_INCH / 2, 10.0);

        telemetry.addData("Path", "complete.");
        telemetry.update();
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
                  (rfMotor.isBusy() || lfMotor.isBusy() || rbMotor.isBusy() || lbMotor.isBusy())) {}
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

    public void driveOn (double forSpeed, double horiSpeed) {
        rfMotor.setPower((+ forSpeed - horiSpeed) / 2);
        rbMotor.setPower((+ forSpeed + horiSpeed) / 2);
        lfMotor.setPower((- forSpeed - horiSpeed) / 2);
        lbMotor.setPower((- forSpeed + horiSpeed) / 2);
    }

    public int senseBlock() {
        // Declare BlockID.
        int BlockID;

        // Get HSV value.
        telemetry.addData("Color Sensor", "sensing block.");
        Color.RGBToHSV((int)(sensorColor.red() * COLOR_SENSOR_SCALE_FACTOR),
                       (int)(sensorColor.green() * COLOR_SENSOR_SCALE_FACTOR),
                       (int)(sensorColor.blue() * COLOR_SENSOR_SCALE_FACTOR),
                        COLOR_SENSOR_HSV);
        telemetry.addData("Value", Float.toString(COLOR_SENSOR_HSV[2]));

        // Check which stone is sensed.
        if (COLOR_SENSOR_HSV[2] > 80) {
            BlockID = 0; // Stone.
        } else {
            BlockID = 1; // Skystone.
        }
        telemetry.addData("Block ID", Integer.toString(BlockID));

        // Return sensed block.
        telemetry.update();
        sleep(1000);
        return BlockID;
    }
    public void armGrab(){
        encoderDrive(DRIVE_SPEED, -7,0,2); //backup 7 inches
        encoderTurn(DRIVE_SPEED,0.5,2) //180 turn
        armMove(DRIVE_SPEED, )
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
                    (rfMotor.isBusy() || lfMotor.isBusy() || rbMotor.isBusy() || lbMotor.isBusy())) {}
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
    public void armMove(double speed, double hinRev, double extenderInch, double timeout) {

        // Ensure that the opMode is still active.
        if (opModeIsActive()) {

            // Declare motor targets.
            hingeInch = hinRev*INCH_PER_ARM_REV; //set to inches per a hypotetical full arm rotation using some calculations
            // Set targets.
            hinge.setTargetPosition((int)(hingeInch * COUNT_PER_INCH) + rfMotor.getCurrentPosition());
            extender.setTargetPosition((int)(extenderInch * COUNT_PER_INCH) + rbMotor.getCurrentPosition());
            // Set motors to RUN_TO_POSITION mode.
            hinge.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            extender.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // Reset the timer.
            runtime.reset();

            // Start motion.
            hinge.setPower(speed);
            extender.setPower(speed);
            
            // keep looping while we are still active and any motors are running.
            telemetry.addData("Update", "Started moving.");
            telemetry.update();
            while (opModeIsActive() &&
                   runtime.seconds() < timeout &&
                  (rfMotor.isBusy() || lfMotor.isBusy() || rbMotor.isBusy() || lbMotor.isBusy())) {}
            telemetry.addData("Update", "Done moving.");
            telemetry.update();
            // Stop all motion.
            hinge.setPower(0);
            extender.setPower(0);

            // Turn off RUN_USING_ENCODER mode.
            hinge.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            extender.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }
}