package dev.drtheo.ais.energy;

import loqor.ait.tardis.link.v2.TardisRef;

import java.util.function.Supplier;

public class RefuelingTardisEnergyContainer extends TardisEnergyContainer {

    public RefuelingTardisEnergyContainer(Supplier<TardisRef> ref, long maxExtract, long maxInsert) {
        super(ref, maxExtract, maxInsert);
    }

    @Override
    public long maxInsert() {
        return this.allowsInsertion() ? super.maxInsert() : 0;
    }

    @Override
    public long insertEnergy(long maxAmount, boolean simulate) {
        if (!this.allowsInsertion())
            return 0;

        return super.insertEnergy(maxAmount, simulate);
    }

    @Override
    public boolean allowsInsertion() {
        return this.tardis().fuel().isRefueling();
    }
}
