package net.powermatcher.fpai.agent.buffer.test;

import java.util.Date;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.flexiblepower.rai.BufferControlSpace;
import org.flexiblepower.rai.values.ConstraintList;

public class BufferControlSpaceBuilder {

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

    public BufferControlSpaceBuilder totalCapacity(Measurable<Energy> totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    public BufferControlSpaceBuilder stateOfCharge(float stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
        return this;
    }

    public BufferControlSpaceBuilder chargeSpeed(ConstraintList<Power> chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
        return this;
    }

    public BufferControlSpaceBuilder selfDischarge(Measurable<Power> selfDischarge) {
        this.selfDischarge = selfDischarge;
        return this;
    }

    public BufferControlSpaceBuilder minOnPeriod(Measurable<Duration> minOnPeriod) {
        this.minOnPeriod = minOnPeriod;
        return this;
    }

    public BufferControlSpaceBuilder minOffPeriod(Measurable<Duration> minOffPeriod) {
        this.minOffPeriod = minOffPeriod;
        return this;
    }

    public BufferControlSpaceBuilder target(Double targetStateOfCharge, Date targetTime) {
        this.targetStateOfCharge = targetStateOfCharge;
        this.targetTime = targetTime;
        return this;
    }

    public BufferControlSpace build(String resourceId) {
        return new BufferControlSpace(resourceId,
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
