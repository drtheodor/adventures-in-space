package dev.drtheo.ais.mixin;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.utils.ModUtils;
import loqor.ait.core.data.AbsoluteBlockPos;
import loqor.ait.core.entities.FallingTardisEntity;
import loqor.ait.core.util.ForcedChunkUtil;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisTravel;
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

        BlockPos spacePos = entity.getOnPos();
        ForcedChunkUtil.keepChunkLoaded(serverLevel, spacePos); // force load space

        planet.getOrbitPlanet().ifPresent(targetLevelKey -> {
            MinecraftServer server = serverLevel.getServer();
            ServerLevel targetLevel = server.getLevel(targetLevelKey);

            Tardis tardis = entity.getTardis();
            TardisTravel travel = tardis.travel();

            AbsoluteBlockPos.Directed pos = travel.getPosition();

            travel.setCrashing(true);
            travel.setPosition(new AbsoluteBlockPos.Directed(
                    pos, targetLevel, pos.getRotation()
            ));

            List<Entity> passengers = entity.getPassengers();
            entity.setPos(entity.getX(), AdAstraConfig.atmosphereLeave, entity.getZ());

            ForcedChunkUtil.keepChunkLoaded(targetLevel, entity.getOnPos()); // forceload planet

            Entity teleportedEntity = ModUtils.teleportToDimension(entity, targetLevel);
            teleportedEntity.setNoGravity(false);

            for (Entity passenger : passengers) {
                Entity teleportedPassenger = ModUtils.teleportToDimension(passenger, targetLevel);
                teleportedPassenger.startRiding(teleportedEntity);
            }

            ForcedChunkUtil.stopForceLoading(serverLevel, spacePos); // un-forceload space
            ci.cancel();
        });
    }
}
