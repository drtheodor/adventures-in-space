package dev.drtheo.ais.mixininterface;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface OxygenExterior {
    void ais$resetLastDistributedBlocks(Set<BlockPos> positions);
    void ais$clearOxygenBlocks();
    void ais$fillOxygen();
}
