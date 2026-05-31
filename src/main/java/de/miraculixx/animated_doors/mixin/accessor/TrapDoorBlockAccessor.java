package de.miraculixx.animated_doors.mixin.accessor;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TrapDoorBlock.class)
public interface TrapDoorBlockAccessor {
    @Invoker("getType")
    BlockSetType animatedDoors$getType();
}
