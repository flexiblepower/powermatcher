package net.powermatcher.fpai.agent.timeshifter.test;

import java.util.Date;

import org.flexiblepower.rai.TimeShifterControlSpace;
import org.flexiblepower.rai.values.EnergyProfile;

public class TimeShifterControlSpaceBuilder implements Cloneable {
    private String applianceId;

    private Date validFrom;
    private Date validThru;
    private Date expirationTime;

    private Date startAfter;
    private Date startBefore;
    private EnergyProfile energyProfile;

    public TimeShifterControlSpaceBuilder setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        return this;
    }

    public TimeShifterControlSpaceBuilder setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public TimeShifterControlSpaceBuilder setValidThru(Date validThru) {
        this.validThru = validThru;
        return this;
    }

    public TimeShifterControlSpaceBuilder setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public TimeShifterControlSpaceBuilder setStartAfter(Date startAfter) {
        this.startAfter = startAfter;
        return this;
    }

    public TimeShifterControlSpaceBuilder setStartBefore(Date startBefore) {
        this.startBefore = startBefore;
        return this;
    }

    public TimeShifterControlSpaceBuilder setEnergyProfile(EnergyProfile energyProfile) {
        this.energyProfile = energyProfile;
        return this;
    }

    public TimeShifterControlSpace build() {
        return new TimeShifterControlSpace(applianceId,
                                           validFrom,
                                           validThru,
                                           expirationTime,
                                           energyProfile,
                                           startBefore,
                                           startAfter);
    }

    @Override
    public TimeShifterControlSpaceBuilder clone() {
        try {
            return (TimeShifterControlSpaceBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
