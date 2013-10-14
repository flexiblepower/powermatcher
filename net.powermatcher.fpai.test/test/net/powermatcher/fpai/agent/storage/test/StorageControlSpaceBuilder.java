package net.powermatcher.fpai.agent.storage.test;

import java.util.Date;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.values.ConstraintList;

public class StorageControlSpaceBuilder {

    private Date validFrom = new Date(System.currentTimeMillis());
    private Date validThru = new Date(System.currentTimeMillis() + 1000);
    private Date expirationTime = null;
    private Measurable<Energy> totalCapacity = Measure.valueOf(1, NonSI.KWH);
    private float stateOfCharge = 0;
    private ConstraintList<Power> chargeSpeed = ConstraintList.create(SI.WATT).addSingle(1000).build();
    private Measurable<Power> selfDischarge = Measure.valueOf(0, SI.WATT);
    private Measurable<Duration> minOnPeriod = Measure.valueOf(0, SI.SECOND);
    private Measurable<Duration> minOffPeriod = Measure.valueOf(0, SI.SECOND);
    private Double targetStateOfCharge = null;
    private Date targetTime = null;
    private ConstraintList<Power> dischargeSpeed = ConstraintList.create(SI.WATT).addSingle(1000).build();
    private float chargeEfficiency = 0.9f;
    private float dischargeEfficiency = 0.9f;

    public void dischargeSpeed(ConstraintList<Power> dischargeSpeed) {
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

    public StorageControlSpaceBuilder totalCapacity(Measurable<Energy> totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    public StorageControlSpaceBuilder stateOfCharge(float stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
        return this;
    }

    public StorageControlSpaceBuilder chargeSpeed(ConstraintList<Power> chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
        return this;
    }

    public StorageControlSpaceBuilder selfDischarge(Measurable<Power> selfDischarge) {
        this.selfDischarge = selfDischarge;
        return this;
    }

    public StorageControlSpaceBuilder minOnPeriod(Measurable<Duration> minOnPeriod) {
        this.minOnPeriod = minOnPeriod;
        return this;
    }

    public StorageControlSpaceBuilder minOffPeriod(Measurable<Duration> minOffPeriod) {
        this.minOffPeriod = minOffPeriod;
        return this;
    }

    public StorageControlSpaceBuilder target(Double targetStateOfCharge, Date targetTime) {
        this.targetStateOfCharge = targetStateOfCharge;
        this.targetTime = targetTime;
        return this;
    }

    public StorageControlSpace build(String resourceId) {
        return new StorageControlSpace(resourceId,
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
