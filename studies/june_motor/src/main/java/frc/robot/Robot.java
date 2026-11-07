// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import org.team100.lib.framework.TimedRobot100;
import org.team100.lib.hid.DriverXboxControl;
import org.team100.lib.logging.LogPoller;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.Logging;
import org.team100.lib.logging.RobotLog;
import org.team100.lib.logging.TotalCurrentLog;
import org.team100.lib.coherence.Takt;
import org.team100.lib.experiments.Experiment;
import org.team100.lib.experiments.Experiments;

import java.text.spi.NumberFormatProvider;

import org.team100.lib.coherence.Cache;
import org.team100.lib.motor.ctre.KrakenX60Motor;
import org.team100.lib.motor.ctre.Talon6Encoder;
import org.team100.lib.motor.ctre.Falcon500Motor;
import org.team100.lib.util.Startup;
import org.team100.lib.config.CurrentLimit;
import org.team100.lib.config.Friction;
import org.team100.lib.config.PIDConstants;
import org.team100.lib.motor.MotorPhase;
import org.team100.lib.motor.NeutralMode100;
import org.team100.lib.util.CanId;



/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot100 {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final RobotLog m_robotLog;
  private final double pctDutyCycleLimit = 20.0/100.0;

  Falcon500Motor motor;
  private final DriverXboxControl m_control;
  private double m_dutyCycle;
  private Talon6Encoder m_encoder; 

  private boolean joystickModeEnabled = true;
  private boolean Btesting = false;
  private boolean down = false;
  private boolean bbuffer = false;
  private boolean abuffer = false;

  private double target_rotations_S = 1;
  private double current_rotations_S;
  private double error_rotations_S;

  private double statorLimitBasis;
  private double statorLimit;

  private double pid_power;
  private double pid_k = 0.5;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    Startup.start();
    LoggerFactory log = Logging.root();
    m_robotLog = new RobotLog(log);
    TotalCurrentLog currentLog = m_robotLog.totalCurrentLog();

    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    CanId canId36 = new CanId(36);
    NeutralMode100 neutral = NeutralMode100.COAST;
    MotorPhase phase = MotorPhase.FORWARD;
    CurrentLimit limit = new CurrentLimit(10,10);
    Friction friction = new Friction(0,0,0,0);
    PIDConstants pid = new PIDConstants(0,0,0,0,0,0);
    motor = new Falcon500Motor(log.name("motor"), currentLog, canId36, neutral, phase, limit, friction, pid);
    m_encoder = motor.encoder();
    m_control = new DriverXboxControl(log, 0);

  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */

  @Override
  public void robotPeriodic() {
      //velocityRad_S = motor.encoder().getVelocityRad_S();

      Takt.update();
      Cache.refresh();
      LogPoller.log();

      current_rotations_S = (m_encoder.getVelocityRad_S() / (2*Math.PI))/4.0;
      error_rotations_S = target_rotations_S-current_rotations_S;
      pid_power = pid_k * error_rotations_S;

      

      SmartDashboard.putNumber("Discus/Rotations (s)/Actual", current_rotations_S);
      SmartDashboard.putNumber("Discus/Rotations (s)/Error", error_rotations_S);
      SmartDashboard.putNumber("Discus/Rotations (s)/Target", target_rotations_S);
      SmartDashboard.putNumber("Discus/Stator Current/Actual",motor.getStatorCurrent());
      SmartDashboard.putNumber("Discus/Stator Limit/Current",statorLimit);
      SmartDashboard.putNumber("Discus/Stator Limit/Basis",statorLimitBasis);
      SmartDashboard.putNumber("Discus/power/",pid_power);

      down = m_control.b();
      if (down && !bbuffer) {
        bbuffer = true;
        if (!joystickModeEnabled) {
          Btesting = !Btesting;
          System.out.println("Inverted Btesting "+Btesting);
        }
      } else if (!down) {
        bbuffer = false;
      }

      down = m_control.a();
      if (down && !abuffer) {
        abuffer = true;
        if (!Btesting) {
          joystickModeEnabled = !joystickModeEnabled;
          System.out.println("Inverted joystickEnabled "+joystickModeEnabled);
        }
      } else if (!down) {
        abuffer = false;
      }

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    m_dutyCycle = 0;
    target_rotations_S = 0;
    Btesting = false;
    joystickModeEnabled = false;
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    if (joystickModeEnabled) {
      target_rotations_S = 20 * (m_control.leftY() * pctDutyCycleLimit);
    } else if (Btesting) {
      target_rotations_S = 20 * (0.05);
    } else {
      target_rotations_S = 0;
    }

    motor.setDutyCycle((target_rotations_S + pid_power) * 0.05);
    
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
