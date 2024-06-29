package dev.drtheo.ais.mixin;

import dev.drtheo.ais.AISMod;
import dev.drtheo.ais.mixininterface.OxygenExterior;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.constants.PlanetConstants;
import loqor.ait.api.tardis.TardisEvents;
import loqor.ait.core.blockentities.ExteriorBlockEntity;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.link.v2.AbstractLinkableBlockEntity;
import loqor.ait.core.data.DirectedGlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ExteriorBlockEntity.class)
public class ExteriorBlockEntityMixin extends BlockEntity implements OxygenExterior {

    @Unique private final Set<BlockPos> lastDistributedBlocks = new HashSet<>();
    @Unique private boolean shouldSyncPositions;

    public ExteriorBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    static {
        TardisEvents.TOGGLE_SHIELDS.register((tardis, active, visuals) -> {
            DirectedGlobalPos.Cached globalPos = tardis.travel2().position();
            if (!(globalPos.getWorld().getBlockEntity(globalPos.getPos()) instanceof OxygenExterior exterior))
                return;

            if (active) exterior.ais$fillOxygen();
            else exterior.ais$clearOxygenBlocks();
        });
    }

    @Override
    public void ais$clearOxygenBlocks() {
        OxygenApi.API.removeOxygen(level, lastDistributedBlocks);
        TemperatureApi.API.removeTemperature(level, lastDistributedBlocks); // TODO: move to Temperature Regulator machine
        lastDistributedBlocks.clear();
    }

    @Override
    public void ais$resetLastDistributedBlocks(Set<BlockPos> positions) {
        lastDistributedBlocks.removeAll(positions);
        this.ais$clearOxygenBlocks();

        lastDistributedBlocks.addAll(positions);
        shouldSyncPositions = true;
    }

    @Override
    public void ais$fillOxygen() {
        Set<BlockPos> positions = AISMod.blocksInRadius(this.getBlockPos(), 3);

        OxygenApi.API.setOxygen(level, positions, true);
        TemperatureApi.API.setTemperature(level, positions, PlanetConstants.COMFY_EARTH_TEMPERATURE); // TODO: move to Temperature Regulator machine

        Set<BlockPos> lastPositionsCopy = new HashSet<>(lastDistributedBlocks);
        this.ais$resetLastDistributedBlocks(positions);

        if (lastPositionsCopy.size() >= 32)
            return;

        positions.removeAll(lastPositionsCopy);
    }

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lloqor/ait/core/blockentities/ExteriorBlockEntity;)V", at = @At("TAIL"))
    public void tick(Level world, BlockPos pos, BlockState blockState, ExteriorBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide())
            return;

        //noinspection DataFlowIssue
        if (world.getServer().getTickCount() % MachineConfig.distributionRefreshRate != 0)
            return;

        Tardis tardis = ((AbstractLinkableBlockEntity) (Object) this).tardis().get();

        if (tardis == null)
            return;

        if (!tardis.areShieldsActive()) {
            this.ais$clearOxygenBlocks();
            return;
        }

        this.ais$fillOxygen();
    }

    @Inject(method = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    public void load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("LastDistributedBlocks")) {
            lastDistributedBlocks.clear();

            for (long pos : tag.getLongArray("LastDistributedBlocks")) {
                lastDistributedBlocks.add(BlockPos.of(pos));
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();

        if (this.shouldSyncPositions) {
            tag.putLongArray("LastDistributedBlocks", lastDistributedBlocks.stream()
                    .mapToLong(BlockPos::asLong).toArray());

            this.shouldSyncPositions = false;
        }

        return tag;
    }

    @Override
    public void setRemoved() {
        this.ais$clearOxygenBlocks();
        super.setRemoved();
    }
}
