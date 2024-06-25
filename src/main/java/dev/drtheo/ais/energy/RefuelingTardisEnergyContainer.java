package dev.drtheo.ais.energy;

import loqor.ait.tardis.link.v2.TardisRef;

import java.util.function.Supplier;

public class RefuelingTardisEnergyContainer extends TardisEnergyContainer {

    public RefuelingTardisEnergyContainer(Supplier<TardisRef> ref, long maxExtract, long maxInsert) {
        super(ref, maxExtract, maxInsert);
    }

    @Override
    public boolean allowsInsertion() {
        return this.tardis().fuel().isRefueling();
    }

    @Override
    public boolean allowsExtraction() {
        return !this.tardis().fuel().isRefueling();
    }
}
