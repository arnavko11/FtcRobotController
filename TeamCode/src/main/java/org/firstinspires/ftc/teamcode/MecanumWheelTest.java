package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * Bring-up tool: spins one wheel at a time so you can confirm every wheel drives the robot
 * FORWARD on positive power before trusting the mecanum mixing in {@link MecanumDrive}.
 *
 * Strafing only works if all four wheels agree on what "positive" means. The Strafer's bevel
 * gearing reverses the motor's output relative to the shaft, so the sign is not obvious from
 * the motor alone - it has to be observed.
 *
 * Procedure:
 *   1. Put the robot on blocks so the wheels spin free.
 *   2. Press a button to spin that one wheel at 30% power.
 *   3. Watch the TOP of the wheel. It should roll toward the FRONT of the robot.
 *   4. If it rolls backward, that wheel's sign is wrong - fix it in MecanumDrive.
 *
 * Buttons:  X = front left   Y = front right   A = back left   B = back right
 */
@TeleOp(name = "Mecanum Wheel Test", group = "Drive")
public class MecanumWheelTest extends LinearOpMode {

    private static final double TEST_POWER = 0.3;

    @Override
    public void runOpMode() {
        DcMotorEx frontLeft  = hardwareMap.get(DcMotorEx.class, "1");
        DcMotorEx frontRight = hardwareMap.get(DcMotorEx.class, "2");
        DcMotorEx backLeft   = hardwareMap.get(DcMotorEx.class, "3");
        DcMotorEx backRight  = hardwareMap.get(DcMotorEx.class, "4");

        // Same direction setup as MecanumDrive, so what you observe here is what you get there.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        // Back pair is flipped relative to the front: verified on the robot with
        // MecanumWheelTest, where both back wheels rolled backward before this change.
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.REVERSE);

        for (DcMotorEx m : new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        telemetry.addLine("Robot on blocks. Watch the TOP of each wheel roll toward the FRONT.");
        telemetry.addLine("X=FL(1)  Y=FR(2)  A=BL(3)  B=BR(4)");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            frontLeft.setPower(gamepad1.x ? TEST_POWER : 0);
            frontRight.setPower(gamepad1.y ? TEST_POWER : 0);
            backLeft.setPower(gamepad1.a ? TEST_POWER : 0);
            backRight.setPower(gamepad1.b ? TEST_POWER : 0);

            telemetry.addLine("Each wheel should roll the robot FORWARD.");
            telemetry.addData("FL (1)", gamepad1.x ? "SPINNING" : "-");
            telemetry.addData("FR (2)", gamepad1.y ? "SPINNING" : "-");
            telemetry.addData("BL (3)", gamepad1.a ? "SPINNING" : "-");
            telemetry.addData("BR (4)", gamepad1.b ? "SPINNING" : "-");
            telemetry.update();
        }
    }
}
