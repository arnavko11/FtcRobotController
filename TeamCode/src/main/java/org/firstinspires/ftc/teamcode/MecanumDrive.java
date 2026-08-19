package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * Field-simple mecanum drive.
 *
 * Hardware map names (configure these on the Driver Station):
 *   "1" = front left    "2" = front right
 *   "3" = back left     "4" = back right
 *
 * Controls:
 *   left stick  - translate (forward/back, strafe)
 *   right stick - rotate
 *   left trigger - slow mode (scales down to 30% at full pull)
 */
@TeleOp(name = "Mecanum Drive", group = "Drive")
public class MecanumDrive extends LinearOpMode {

    private DcMotorEx frontLeft, frontRight, backLeft, backRight;

    @Override
    public void runOpMode() {
        frontLeft  = hardwareMap.get(DcMotorEx.class, "1");
        frontRight = hardwareMap.get(DcMotorEx.class, "2");
        backLeft   = hardwareMap.get(DcMotorEx.class, "3");
        backRight  = hardwareMap.get(DcMotorEx.class, "4");

        // Motors on the left side face the opposite direction from the right side,
        // so reverse the left ones to make positive power = forward on all four.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        for (DcMotorEx m : new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        telemetry.addLine("Initialized - waiting for start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double drive  = -gamepad1.left_stick_y;   // stick y is inverted on the gamepad
            double strafe =  gamepad1.left_stick_x;
            double turn   =  gamepad1.right_stick_x;

            // Full pull on the left trigger scales everything down to 30% for fine control.
            double scale = 1.0 - 0.7 * gamepad1.left_trigger;

            setDrivePower(drive, strafe, turn, scale);

            telemetry.addData("drive/strafe/turn", "%.2f / %.2f / %.2f", drive, strafe, turn);
            telemetry.addData("scale", "%.2f", scale);
            telemetry.addData("FL / FR", "%.2f / %.2f", frontLeft.getPower(), frontRight.getPower());
            telemetry.addData("BL / BR", "%.2f / %.2f", backLeft.getPower(), backRight.getPower());
            telemetry.update();
        }

        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Standard mecanum mixing. Denominator keeps the ratio between wheels intact when the
     * combined command would otherwise exceed 1.0, so the robot never clips its own heading.
     */
    private void setDrivePower(double drive, double strafe, double turn, double scale) {
        double denominator = Math.max(Math.abs(drive) + Math.abs(strafe) + Math.abs(turn), 1.0);

        frontLeft.setPower((drive + strafe + turn) / denominator * scale);
        frontRight.setPower((drive - strafe - turn) / denominator * scale);
        backLeft.setPower((drive - strafe + turn) / denominator * scale);
        backRight.setPower((drive + strafe - turn) / denominator * scale);
    }
}
