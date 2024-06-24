package dev.drtheo.ais.mixin;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import loqor.ait.core.blockentities.DoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DoorBlockEntity.class)
public class DoorBlockEntityMixin extends BlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {

    @Unique protected WrappedBlockEnergyContainer container;

    public DoorBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        if (container != null)
            return container;

        return container = new WrappedBlockEnergyContainer(
                this, new SimpleEnergyContainer(
                10000, 750, 500
        ));
    }
}