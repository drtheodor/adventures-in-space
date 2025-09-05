package dev.drtheo.ais.mixin;

import dev.amble.ait.core.blocks.ExteriorBlock;
import earth.terrarium.adastra.api.planets.PlanetApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExteriorBlock.class)
public class ExteriorBlockMixin {

    @Inject(method = "canFallThrough(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private static void shouldFall(Level world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (PlanetApi.API.isSpace(world))
            cir.setReturnValue(false);
    }
}
