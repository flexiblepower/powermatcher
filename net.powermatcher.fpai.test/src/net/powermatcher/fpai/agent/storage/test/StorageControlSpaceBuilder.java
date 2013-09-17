package net.powermatcher.fpai.agent.storage.test;

import java.util.Date;

import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;

public class StorageControlSpaceBuilder {

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
    private PowerConstraintList dischargeSpeed = new PowerConstraintList(new PowerConstraint(new PowerValue(1000,
                                                                                                            PowerUnit.WATT)));
    private float chargeEfficiency = 0.9f;
    private float dischargeEfficiency = 0.9f;

    public void dischargeSpeed(PowerConstraintList dischargeSpeed) {
        this.dischargeSpeed = dischargeSpeed;
    }

    public void chargeEfficiency(float chargeEfficiency) {
        this.chargeEfficiency = chargeEfficiency;
    }

    public void dischargeEfficiency(float dischargeEfficiency) {
        this.dischargeEfficiency = dischargeEfficiency;
    }

    public StorageControlSpaceBuilder validFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public StorageControlSpaceBuilder validThru(Date validThru) {
        this.validThru = validThru;
        return this;
    }

    public StorageControlSpaceBuilder expirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public StorageControlSpaceBuilder totalCapacity(EnergyValue totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    public StorageControlSpaceBuilder stateOfCharge(float stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
        return this;
    }

    public StorageControlSpaceBuilder chargeSpeed(PowerConstraintList chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
        return this;
    }

    public StorageControlSpaceBuilder selfDischarge(PowerValue selfDischarge) {
        this.selfDischarge = selfDischarge;
        return this;
    }

    public StorageControlSpaceBuilder minOnPeriod(Duration minOnPeriod) {
        this.minOnPeriod = minOnPeriod;
        return this;
    }

    public StorageControlSpaceBuilder minOffPeriod(Duration minOffPeriod) {
        this.minOffPeriod = minOffPeriod;
        return this;
    }

    public StorageControlSpaceBuilder target(Float targetStateOfCharge, Date targetTime) {
        this.targetStateOfCharge = targetStateOfCharge;
        this.targetTime = targetTime;
        return this;
    }

    public StorageControlSpace build(String applianceId) {
        return new StorageControlSpace(applianceId,
                                       validFrom,
                                       validThru,
                                       expirationTime,
                                       totalCapacity,
                                       stateOfCharge,
                                       chargeSpeed,
                                       dischargeSpeed,
                                       selfDischarge,
                                       chargeEfficiency,
                                       dischargeEfficiency,
                                       minOnPeriod,
                                       minOffPeriod,
                                       targetTime,
                                       targetStateOfCharge);
    }
}
