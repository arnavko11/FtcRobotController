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
 *   dpad left/right - pure strafe, no drift from the stick's diagonal
 */
@TeleOp(name = "Mecanum Drive", group = "Drive")
public class MecanumDrive extends LinearOpMode {

    /**
     * Mecanum rollers push at 45 degrees, so a sideways command loses force to scrub and the
     * robot strafes slower than it drives for the same stick throw. Scaling x up evens that out.
     * Tune on the field: raise it if strafing still lags, lower it if the robot over-slides.
     */
    private static final double STRAFE_CORRECTION = 1.1;

    /** Speed used by the dpad strafe buttons. */
    private static final double DPAD_STRAFE_POWER = 0.6;

    private DcMotorEx frontLeft, frontRight, backLeft, backRight;

    @Override
    public void runOpMode() {
        frontLeft  = hardwareMap.get(DcMotorEx.class, "1");
        frontRight = hardwareMap.get(DcMotorEx.class, "2");
        backLeft   = hardwareMap.get(DcMotorEx.class, "3");
        backRight  = hardwareMap.get(DcMotorEx.class, "4");

        // Direction lives HERE and nowhere else - never negate a wheel down in the mixing,
        // where it is easy to lose track of which compensations are already applied.
        //
        // The only rule that matters: on positive power, every wheel must roll the robot
        // FORWARD. Verify with MecanumWheelTest, do not reason it out from the motors - the
        // gearing between gearbox and wheel reverses the output, and it does not necessarily
        // do so on all four. Once all four roll forward, the mixing below is correct as-is.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        // Back pair is flipped relative to the front: verified on the robot with
        // MecanumWheelTest, where both back wheels rolled backward before this change.
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.REVERSE);

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
            double strafe =  gamepad1.left_stick_x * STRAFE_CORRECTION;
            double turn   =  gamepad1.right_stick_x;

            // Dpad overrides the stick for a clean, straight strafe with no forward drift.
            if (gamepad1.dpad_left || gamepad1.dpad_right) {
                drive  = 0;
                turn   = 0;
                strafe = (gamepad1.dpad_right ? DPAD_STRAFE_POWER : -DPAD_STRAFE_POWER)
                        * STRAFE_CORRECTION;
            }

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

        // Strafing right drives the diagonals against each other: FL and BR forward, FR and
        // BL backward. This is plain, unmodified mecanum mixing - every hardware correction
        // has already been handled by setDirection above. Do not add signs here.
        frontLeft.setPower((drive + strafe + turn) / denominator * scale);
        frontRight.setPower((drive - strafe - turn) / denominator * scale);
        backLeft.setPower((drive - strafe + turn) / denominator * scale);
        backRight.setPower((drive + strafe - turn) / denominator * scale);
    }
}
