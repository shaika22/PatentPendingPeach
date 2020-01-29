package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.general.ServoM;
import org.firstinspires.ftc.teamcode.hardware.general.Motor;
import org.firstinspires.ftc.teamcode.hardware.general.Gyro;
import org.firstinspires.ftc.teamcode.hardware.general.Color;
import org.firstinspires.ftc.teamcode.hardware.general.Distance;

// robot
public final class Fuji {

    // OpMode members
    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    public final DriveTrain driveTrain;
    public final Motor lift;
    public final ServoM dropStone;
    public final ServoM pinch;
    public final ServoM hook1;
    public final ServoM hook2;
    public final Gyro gyro;
//    public final Color tape;
    public final Color stone;
    public final Distance distance;
    boolean autoSoundFound = false;
    int soundAutonomous;
    // robot constants
    private static final double gyroAdjust = 5;

    // initialize robot
    public Fuji(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

//        soundAutonomous = hardwareMap.appContext.getResources().getIdentifier("autonomous", "raw", hardwareMap.appContext.getPackageName());
//        if (soundAutonomous != 0)
//            autoSoundFound = SoundPlayer.getInstance().preload(hardwareMap.appContext, soundAutonomous);
        telemetry.addData("auto resource", autoSoundFound ? "Found" : "NOT found\n Add autonomous.wav to /src/main/res/raw");
        telemetry.update();

        Motor rf = new Motor("rf", 1440, 1, 2.95, hardwareMap);
        Motor rb = new Motor("rb", 1440, 1, 2.95, hardwareMap);
        Motor lf = new Motor("lf", 1440, 1, 2.95, hardwareMap);
        Motor lb = new Motor("lb", 1440, 1, 2.95, hardwareMap);
        driveTrain = new DriveTrain(rf, rb, lf, lb);


        //CHECK THESE VALUES **************************************************
        lift = new Motor("lift", 1120, 1, 2, hardwareMap);
        pinch = new ServoM("pinch", hardwareMap);
        hook1 = new ServoM("hook1", hardwareMap);
        hook2 = new ServoM("hook2", hardwareMap);
        gyro = new Gyro("imu", hardwareMap);
        dropStone = new ServoM("drop", hardwareMap);
//        tape = new Color("colorDown", hardwareMap);
        stone = new Color("colorFor", hardwareMap);
        distance = new Distance("distance", hardwareMap);
    }

    // turn with gyro, speed should be positive
    public void turn(double orientation, double speed) {
        while (Math.abs(headingError(orientation)) > 0.02) {
            if (headingError(orientation) > 0) {speed = -speed;}
            telemetry.addData("Gyro Sensor", "turning");
            telemetry.addData("Angle", gyro.measure());
            telemetry.update();
            driveTrain.start(new DriveTrain.Vector(0, 0, speed).speeds());
        }
        driveTrain.start(new DriveTrain.Vector(0, 0, 0).speeds());
    }

    // drive with distance, ORIENTATION IS ONLY FOR KEEPING A HEADING, NOT FOR GOING TO A HEADING
    public void drive(double hori, double vert, double target, double orientation, boolean far) {
        while (far ? distance.measure() < target : distance.measure() > target) {
            telemetry.addData("Distance Sensor", "driving");
            telemetry.addData("Distance", distance.measure());
            telemetry.addData("head error:", -headingError(orientation));
            telemetry.update();
            double turn = Range.clip(-headingError(orientation) * gyroAdjust, -1, 1);
            driveTrain.start(new DriveTrain.Vector(hori, vert, turn).speeds());
        }
        driveTrain.start(new DriveTrain.Vector(0, 0, 0).speeds());
    }

    // drive with encoders
    public void move(double hori, double vert) {
        telemetry.addData("Encoders", "moving");
        telemetry.addData("Horizontal", hori);
        telemetry.addData("Vertical", vert);
        driveTrain.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

//        /*
        double length = Math.hypot(vert, hori);
        double angle = Math.atan2(vert, hori) - Math.PI/4;
        if (length == 0) {
            return;
        }
        telemetry.addData("operation", "length: %.8f, angle: %.8f", length, angle + Math.PI/4);

        double lf = length * -Math.cos(angle);
        double lb = length * -Math.sin(angle);
        double rf = length * Math.sin(angle);
        double rb = length * Math.cos(angle);

        telemetry.addData("drives", "lf: %.8f, lb: %.8f, rf: %.8f, rb: %.8f", lf, lb, rf, rb);
        telemetry.update();
        driveTrain.setTarget(new DriveTrain.Square<Double>(rf, rb, lf, lb));
//        */


//        driveTrain.setTarget(new DriveTrain.Direction(hori, vert, 0).speeds());
        driveTrain.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        driveTrain.start(new DriveTrain.Square<Double>(0.8, 0.8, 0.8, 0.8));

        // do gyro adjustment in here |
        //                            v
        while (driveTrain.isBusy()) {}
        //turn (-headingError(angle)) * gyroAdjust)
        driveTrain.start(new DriveTrain.Square<Double>(0.0, 0.0, 0.0, 0.0));
        driveTrain.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // if the color sensor sees a skystone
    public boolean isSkystone() {
        boolean block;
        telemetry.addData("Color Sensor", "sensing block");
        Color.HSV color = stone.measure();
        telemetry.addData("Hue", color.h());
        block = color.h() >= 60;
        telemetry.addData("Block", block);
        telemetry.update();
        return block;
    }

    // get current offset from target orientation
    public double headingError(double orientation) {
        double rawError = orientation - gyro.measure();
        if (rawError < -0.5) {rawError += 1;}
        if (rawError > 0.5) {rawError -= 1;}
        return rawError;
    }

    public void playAutoSound() {
        if (autoSoundFound) {
            SoundPlayer.getInstance().startPlaying(hardwareMap.appContext, soundAutonomous);
            telemetry.addData("Sound", "auto");
            telemetry.update();
        }
    }

    public void hook(double target) {
        hook1.start(target);
        hook2.start(target);
    }
}