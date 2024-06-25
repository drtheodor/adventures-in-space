package dev.drtheo.ais.energy;

import dev.drtheo.ais.util.EnergyUtil;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergySnapshot;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.link.v2.TardisRef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class TardisEnergyContainer implements EnergyContainer {

    private final Supplier<TardisRef> ref;

    private final long maxInsert;
    private final long maxExtract;

    public TardisEnergyContainer(Supplier<TardisRef> ref, long maxExtract, long maxInsert) {
        this.maxExtract = maxExtract;
        this.maxInsert = maxInsert;

        this.ref = ref;
    }

    @Override
    public long insertEnergy(long maxAmount, boolean simulate) {
        long inserted = (long) Mth.clamp(maxAmount, 0,
                Math.min(maxInsert(), getMaxCapacity() - getStoredEnergy())
        );

        if (simulate)
            return inserted;

        this.setEnergy(this.getStoredEnergy() + inserted);
        return inserted;
    }

    @Override
    public long extractEnergy(long maxAmount, boolean simulate) {
        long extracted = (long) Mth.clamp(maxAmount, 0,
                Math.min(this.maxExtract(), this.getStoredEnergy())
        );

        if (simulate)
            return extracted;

        this.setEnergy(this.getStoredEnergy() - extracted);
        return extracted;
    }

    @Override
    public long internalInsert(long maxAmount, boolean simulate) {
        long inserted = (long) Mth.clamp(maxAmount, 0,
                this.getMaxCapacity() - this.getStoredEnergy()
        );

        if (simulate)
            return inserted;

        this.setEnergy(this.getStoredEnergy() + inserted);
        return inserted;
    }

    @Override
    public long internalExtract(long maxAmount, boolean simulate) {
        long extracted = (long) Mth.clamp(maxAmount, 0,
                getStoredEnergy()
        );

        if (simulate)
            return extracted;

        this.setEnergy(this.getStoredEnergy() - extracted);
        return extracted;
    }

    @Override
    public void setEnergy(long energy) {
        this.tardis().fuel().setCurrentFuel(EnergyUtil.toArtron(energy));
    }

    @Override
    public long getStoredEnergy() {
        return EnergyUtil.toBotarium(this.tardis().fuel().getCurrentFuel());
    }

    @Override
    public long getMaxCapacity() {
        return EnergyUtil.toBotarium(50_000);
    }

    @Override
    public long maxInsert() {
        return maxInsert;
    }

    @Override
    public long maxExtract() {
        return maxExtract;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    @Override
    public EnergySnapshot createSnapshot() {
        return new SimpleEnergySnapshot(this);
    }

    @Override
    public void clearContent() { }

    @Override
    public void deserialize(CompoundTag nbt) { }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return nbt;
    }

    protected Tardis tardis() {
        return this.ref.get().get();
    }
}
