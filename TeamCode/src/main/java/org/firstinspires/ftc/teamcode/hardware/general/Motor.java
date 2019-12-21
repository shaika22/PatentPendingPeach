package org.firstinspires.ftc.teamcode.hardware.general;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.hardware.type.Device;
import org.firstinspires.ftc.teamcode.hardware.type.Input;
import org.firstinspires.ftc.teamcode.hardware.type.Output;

// rev motor
public class Motor extends Device<DcMotor> implements Input<Double>, Output<Device.Range> {

	// motor information
	private final double tpr;
	private final double gr;
	private final double c;

	// initialize motor
	public Motor(String name, double tpr, double gr, double d, HardwareMap map) {
		super(map.dcMotor.get(name));
		this.tpr = tpr;
		this.gr = gr;
		c = d * Math.PI;
	}

	// initialize motor with default diameter
	public Motor(String name, double tpr, double gr, HardwareMap map) {this(name, tpr, gr, 1 / Math.PI, map);}

	// initialize motor with default gear ratio
	public Motor(String name, double tpr, HardwareMap map) {this(name, tpr, 1, map);}

	// sense position
	@Override public Double measure() {return (device.getCurrentPosition() / tpr) * gr * c;}

	// start motion
	@Override public void start(Device.Range motion) {device.setPower(motion.value);}
}