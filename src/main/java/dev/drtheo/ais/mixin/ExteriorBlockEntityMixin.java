package dev.drtheo.ais.mixin;

import dev.amble.ait.api.tardis.TardisEvents;
import dev.amble.ait.api.tardis.link.v2.block.AbstractLinkableBlockEntity;
import dev.amble.ait.core.blockentities.ExteriorBlockEntity;
import dev.amble.ait.core.tardis.Tardis;
import dev.amble.lib.data.CachedDirectedGlobalPos;
import dev.drtheo.ais.AISMod;
import dev.drtheo.ais.mixininterface.OxygenExterior;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.constants.PlanetConstants;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ExteriorBlockEntity.class)
public class ExteriorBlockEntityMixin extends AbstractLinkableBlockEntity implements OxygenExterior {

    @Unique private final Set<BlockPos> lastDistributedBlocks = new HashSet<>();
    @Unique private boolean shouldSyncPositions;

    public ExteriorBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Unique
    private static @Nullable OxygenExterior asExterior(Tardis tardis) {
        CachedDirectedGlobalPos globalPos = tardis.travel().position();

        if (!(globalPos.getWorld().getBlockEntity(globalPos.getPos()) instanceof OxygenExterior exterior))
            return null;

        return exterior;
    }

    static {
        TardisEvents.TOGGLE_SHIELDS.register((tardis, active, visuals) -> {
            OxygenExterior exterior = asExterior(tardis);

            if (exterior == null)
                return;

            if (active) exterior.ais$fillOxygen();
            else exterior.ais$clearOxygenBlocks();
        });

        TardisEvents.DEMAT.register(tardis -> {
            OxygenExterior exterior = asExterior(tardis);

            if (exterior == null)
                return TardisEvents.Interaction.PASS;

            exterior.ais$clearOxygenBlocks();
            return TardisEvents.Interaction.PASS;
        });

        TardisEvents.LANDED.register(tardis -> {
            OxygenExterior exterior = asExterior(tardis);

            if (exterior == null)
                return;

            if (tardis.areShieldsActive())
                exterior.ais$fillOxygen();
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

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ldev/amble/ait/core/blockentities/ExteriorBlockEntity;)V", at = @At("TAIL"))
    public void tick(Level world, BlockPos pos, BlockState blockState, ExteriorBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClientSide())
            return;

        //noinspection DataFlowIssue
        if (world.getServer().getTickCount() % MachineConfig.distributionRefreshRate != 0)
            return;

        Tardis tardis = this.tardis().get();

        if (tardis == null)
            return;

        if (!tardis.areShieldsActive()) {
            this.ais$clearOxygenBlocks();
            return;
        }

        this.ais$fillOxygen();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

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
