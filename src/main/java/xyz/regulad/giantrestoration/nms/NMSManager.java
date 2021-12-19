package xyz.regulad.giantrestoration.nms;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftGiant;
import org.bukkit.entity.Giant;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.giantrestoration.nms.goal.GiantAttackGoal;
import xyz.regulad.giantrestoration.nms.goal.GiantAttackTurtleEggGoal;

public class NMSManager {
    public static void setupGoals(final @NotNull Giant giant) {
        final @NotNull CraftGiant craftGiant = (CraftGiant) giant;
        final @NotNull net.minecraft.world.entity.monster.Giant nmsGiant = craftGiant.getHandle();

        final @NotNull CraftWorld craftWorld = (CraftWorld) giant.getWorld();
        final @NotNull ServerLevel nmsWorld = craftWorld.getHandle();

        // registerGoals
        if (nmsWorld.paperConfig.zombiesTargetTurtleEggs) nmsGiant.goalSelector.addGoal(4, new GiantAttackTurtleEggGoal(nmsGiant, 1.0D, 3, nmsGiant.getRandom())); // Paper
        nmsGiant.goalSelector.addGoal(8, new LookAtPlayerGoal(nmsGiant, Player.class, 8.0F));
        nmsGiant.goalSelector.addGoal(8, new RandomLookAroundGoal(nmsGiant));
        // on initialization
        nmsGiant.goalSelector.addGoal(1, new BreakDoorGoal(nmsGiant, com.google.common.base.Predicates.in(nmsWorld.paperConfig.zombieBreakDoors)));
        // addBehaviourGoals
        nmsGiant.goalSelector.addGoal(2, new GiantAttackGoal(nmsGiant, 1.0D, false));
        nmsGiant.goalSelector.addGoal(6, new MoveThroughVillageGoal(nmsGiant, 1.0D, true, 4, () -> nmsWorld.getDifficulty() == Difficulty.HARD));
        nmsGiant.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(nmsGiant, 1.0D));
        nmsGiant.targetSelector.addGoal(1, (new HurtByTargetGoal(nmsGiant)).setAlertOthers(ZombifiedPiglin.class));
        nmsGiant.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsGiant, Player.class, true));
        if ( nmsWorld.spigotConfig.zombieAggressiveTowardsVillager ) nmsGiant.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(nmsGiant, AbstractVillager.class, false)); // Spigot
        nmsGiant.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(nmsGiant, IronGolem.class, true));
        nmsGiant.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(nmsGiant, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }
}
