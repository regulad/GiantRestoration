package xyz.regulad.giantrestoration.nms.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class GiantAttackTurtleEggGoal extends RemoveBlockGoal {
    protected Random random;

    public GiantAttackTurtleEggGoal(PathfinderMob mob, double speed, int range, Random random) {
        super(Blocks.TURTLE_EGG, mob, speed, range);
        this.random = random;
    }

    @Override
    public void playDestroyProgressSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public void playBreakSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
    }

    @Override
    public double acceptedDistance() {
        return 1.14D;
    }
}
