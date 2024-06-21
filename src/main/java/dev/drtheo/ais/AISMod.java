package dev.drtheo.ais;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.BlockPos;

import java.util.LinkedHashSet;
import java.util.Set;

public class AISMod implements ModInitializer {


    @Override
    public void onInitialize() {

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
