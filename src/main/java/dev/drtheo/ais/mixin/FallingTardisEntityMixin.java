package dev.drtheo.ais.mixin;

import dev.amble.ait.core.entities.FallingTardisEntity;
import dev.amble.ait.core.tardis.Tardis;
import dev.amble.ait.core.tardis.handler.travel.TravelHandler;
import dev.amble.lib.data.CachedDirectedGlobalPos;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.utils.ModUtils;
import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.text.AsyncBoxView;
import java.util.List;

@Mixin(FallingTardisEntity.class)
public abstract class FallingTardisEntityMixin {

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    private void ais$onBelowWorld(CallbackInfo ci) {
        FallingTardisEntity entity = ((FallingTardisEntity) (Object) this);
        entity.setNoGravity(true);

        if (entity.tardis().isEmpty())
            return;

        Tardis tardis = entity.tardis().get();

        if (!(entity.level() instanceof ServerLevel serverLevel))
            return;

        if (!PlanetApi.API.isSpace(serverLevel))
            return;

        Planet planet = PlanetApi.API.getPlanet(serverLevel.dimension());

        if (planet == null)
            return;

        BlockPos spacePos = entity.getOnPos();
        ChunkPos spacechunkPos = new ChunkPos(spacePos);
        serverLevel.setChunkForced(spacechunkPos.x, spacechunkPos.z, true);// force load space

        planet.getOrbitPlanet().ifPresent(targetLevelKey -> {
            MinecraftServer server = serverLevel.getServer();
            ServerLevel targetLevel = server.getLevel(targetLevelKey);

            if (targetLevel == null)
                return;

            TravelHandler travel = tardis.travel();
            CachedDirectedGlobalPos pos = travel.position();

            List<Entity> passengers = entity.getPassengers();
            entity.setPos(entity.getX(), AdAstraConfig.atmosphereLeave, entity.getZ());

            ChunkPos planetChunkPos = new ChunkPos(entity.getOnPos());
            targetLevel.setChunkForced(planetChunkPos.x, planetChunkPos.z, true); // forceload planet

            Entity teleportedEntity = ModUtils.teleportToDimension(entity, targetLevel);
            teleportedEntity.setNoGravity(false);

            for (Entity passenger : passengers) {
                Entity teleportedPassenger = ModUtils.teleportToDimension(passenger, targetLevel);
                teleportedPassenger.startRiding(teleportedEntity);
            }

            travel.setCrashing(true);
            travel.forcePosition(pos.world(targetLevel));

            serverLevel.setChunkForced(spacechunkPos.x, spacechunkPos.z, false); // un-forceload space
            ci.cancel();
        });
    }
}
