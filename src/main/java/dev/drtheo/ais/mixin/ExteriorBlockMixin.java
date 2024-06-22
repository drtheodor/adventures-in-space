package dev.drtheo.ais.mixin;

import loqor.ait.core.blocks.ExteriorBlock;
import loqor.ait.core.util.ForcedChunkUtil;
import loqor.ait.tardis.Tardis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExteriorBlock.class)
public class ExteriorBlockMixin {

    @Inject(method = "onLanding", at = @At("TAIL"))
    public void onLanded(Tardis tardis, Level world, BlockPos pos, CallbackInfo ci) {
        if (world instanceof ServerLevel level)
            ForcedChunkUtil.stopForceLoading(level, pos);
    }
}
