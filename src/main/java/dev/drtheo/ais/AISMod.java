package dev.drtheo.ais;

import earth.terrarium.botarium.common.energy.EnergyApi;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.util.Updatable;
import loqor.ait.core.AITBlockEntityTypes;
import loqor.ait.core.blockentities.ExteriorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class AISMod implements ModInitializer {


    @Override
    public void onInitialize() {
        EnergyApi.registerEnergyBlockEntity(() -> AITBlockEntityTypes.EXTERIOR_BLOCK_ENTITY_TYPE, (level, pos, state, entity, direction) -> {
            if (entity instanceof ExteriorBlockEntity exterior)
                return (EnergyContainer & Updatable<BlockEntity>) exterior;

            return null;
        });
    }

    public static Set<BlockPos> blocksInRadius(BlockPos start, int radius) {
        Set<BlockPos> pos = new LinkedHashSet<>();

        for (int x = start.getX() - radius; x < start.getX() + radius; x++) {
            for (int y = start.getY() - radius; y < start.getY() + radius; y++) {
                for (int z = start.getZ() - radius; z < start.getZ() + radius; z++) {
                    pos.add(new BlockPos(x, y, z));
                }
            }
        }

        return pos;
    }
}
