package dev.drtheo.ais.energy;

import earth.terrarium.botarium.Botarium;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergySnapshot;
import loqor.ait.tardis.link.v2.TardisRef;
import loqor.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class TardisEnergyContainer implements EnergyContainer {

    private TardisRef ref;

    private final long maxInsert;
    private final long maxExtract;

    public TardisEnergyContainer(TardisRef ref) {
        this(ref, 1024, 1024);
    }

    public TardisEnergyContainer(TardisRef ref, long maxExtract, long maxInsert) {
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
        this.ref.get().fuel().setCurrentFuel((double) energy / 100);
    }

    @Override
    public long getStoredEnergy() {
        return (long) (ref.get().fuel().getCurrentFuel() * 100);
    }

    @Override
    public long getMaxCapacity() {
        return /*(long) (ref.get().fuel().getMaxFuel() * 100)*/ 50000_00;
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
    public CompoundTag serialize(CompoundTag root) {
        CompoundTag tag = root.getCompound(Botarium.BOTARIUM_DATA);
        tag.putUUID("tardis", this.ref.getId());

        root.put(Botarium.BOTARIUM_DATA, tag);
        return root;
    }

    @Override
    public void deserialize(CompoundTag root) {
        CompoundTag tag = root.getCompound(Botarium.BOTARIUM_DATA);

        this.ref = new TardisRef(tag.getUUID("tardis"),
                uuid -> ServerTardisManager.getInstance().demandTardis(null, uuid));
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
}
