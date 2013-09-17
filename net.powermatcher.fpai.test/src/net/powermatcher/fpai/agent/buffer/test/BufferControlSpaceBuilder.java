package net.powermatcher.fpai.agent.buffer.test;

import java.util.Date;

import org.flexiblepower.rai.BufferControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;

public class BufferControlSpaceBuilder {

    private Date validFrom = new Date(System.currentTimeMillis());
    private Date validThru = new Date(System.currentTimeMillis() + 1000);
    private Date expirationTime = null;
    private EnergyValue totalCapacity = new EnergyValue(1, EnergyUnit.KILO_WATTHOUR);
    private float stateOfCharge = 0;
    private PowerConstraintList chargeSpeed = new PowerConstraintList(new PowerConstraint(new PowerValue(1000,
                                                                                                         PowerUnit.WATT)));
    private PowerValue selfDischarge = new PowerValue(0, PowerUnit.WATT);
    private Duration minOnPeriod = new Duration(0, TimeUnit.MINUTES);
    private Duration minOffPeriod = new Duration(0, TimeUnit.MINUTES);
    private Float targetStateOfCharge = null;
    private Date targetTime = null;

    public BufferControlSpaceBuilder validFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public BufferControlSpaceBuilder validThru(Date validThru) {
        this.validThru = validThru;
        return this;
    }

    public BufferControlSpaceBuilder expirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public BufferControlSpaceBuilder totalCapacity(EnergyValue totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    public BufferControlSpaceBuilder stateOfCharge(float stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
        return this;
    }

    public BufferControlSpaceBuilder chargeSpeed(PowerConstraintList chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
        return this;
    }

    public BufferControlSpaceBuilder selfDischarge(PowerValue selfDischarge) {
        this.selfDischarge = selfDischarge;
        return this;
    }

    public BufferControlSpaceBuilder minOnPeriod(Duration minOnPeriod) {
        this.minOnPeriod = minOnPeriod;
        return this;
    }

    public BufferControlSpaceBuilder minOffPeriod(Duration minOffPeriod) {
        this.minOffPeriod = minOffPeriod;
        return this;
    }

    public BufferControlSpaceBuilder target(Float targetStateOfCharge, Date targetTime) {
        this.targetStateOfCharge = targetStateOfCharge;
        this.targetTime = targetTime;
        return this;
    }

    public BufferControlSpace build(String applianceId) {
        return new BufferControlSpace(applianceId,
                                      validFrom,
                                      validThru,
                                      expirationTime,
                                      totalCapacity,
                                      stateOfCharge,
                                      chargeSpeed,
                                      selfDischarge,
                                      minOnPeriod,
                                      minOffPeriod,
                                      targetTime,
                                      targetStateOfCharge);
    }
}
