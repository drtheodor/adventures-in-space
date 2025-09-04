package dev.drtheo.ais.mixin;

import dev.amble.ait.core.blocks.ExteriorBlock;
import dev.amble.ait.core.tardis.Tardis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExteriorBlock.class)
public class ExteriorBlockMixin {
    @Inject(method = "onLanding", at = @At("TAIL"))
    public void onLanding(Tardis tardis, ServerLevel world, BlockPos pos, CallbackInfo ci) {
        ChunkPos chunkpos = new ChunkPos(pos);
        world.setChunkForced(chunkpos.x, chunkpos.z, true);
    }
}
