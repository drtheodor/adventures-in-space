package dev.drtheo.ais.mixin;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.utils.ModUtils;
import loqor.ait.core.data.AbsoluteBlockPos;
import loqor.ait.core.entities.FallingTardisEntity;
import loqor.ait.core.util.ForcedChunkUtil;
import loqor.ait.tardis.Tardis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FallingTardisEntity.class)
public abstract class FallingTardisEntityMixin {

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    private void ais$onBelowWorld(CallbackInfo ci) {
        FallingTardisEntity entity = ((FallingTardisEntity) (Object) this);
        entity.setNoGravity(true);

        if (!(entity.level() instanceof ServerLevel serverLevel))
            return;

        if (!PlanetApi.API.isSpace(serverLevel))
            return;

        Planet planet = PlanetApi.API.getPlanet(serverLevel.dimension());

        if (planet == null)
            return;

        planet.getOrbitPlanet().ifPresent(targetLevelKey -> {
            MinecraftServer server = serverLevel.getServer();
            ServerLevel targetLevel = server.getLevel(targetLevelKey);

            List<Entity> passengers = entity.getPassengers();
            entity.setPos(entity.getX(), AdAstraConfig.atmosphereLeave, entity.getZ());

            Entity teleportedEntity = ModUtils.teleportToDimension(entity, targetLevel);

            for (Entity passenger : passengers) {
                Entity teleportedPassenger = ModUtils.teleportToDimension(passenger, targetLevel);
                teleportedPassenger.startRiding(teleportedEntity);
            }

            Tardis tardis = entity.getTardis();
            AbsoluteBlockPos.Directed pos = tardis.travel().getPosition();
            BlockPos target = teleportedEntity.getOnPos();

            teleportedEntity.setNoGravity(false);

            tardis.travel().setPosition(new AbsoluteBlockPos.Directed(
                    target, targetLevel, pos.getRotation()
            ));

            ForcedChunkUtil.keepChunkLoaded(targetLevel, target);
            ci.cancel();
        });
    }
}
