package xyz.regulad.giantrestoration;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class GiantRestoration extends JavaPlugin implements Listener {
    final @NotNull Random random = new Random();
    final HashMap<Giant, BukkitRunnable> ambientRunnables = new HashMap<>();

    @Override
    public void onEnable() {
        final @NotNull Metrics metrics = new Metrics(this, 13647);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void setupGoals(final @NotNull EntityAddToWorldEvent entityAddToWorldEvent) {
        if (entityAddToWorldEvent.getEntity() instanceof final @NotNull Giant giant) {
            xyz.regulad.giantrestoration.nms.NMSManager.setupGoals(giant);
            // We want this to be semi-recoverable if this fails.
        }
    }

    @EventHandler
    public void setupAmbientTask(final @NotNull EntityAddToWorldEvent entityAddToWorldEvent) {
        if (entityAddToWorldEvent.getEntity() instanceof final @NotNull Giant giant) {
            final @NotNull BukkitRunnable ambientRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                     if (GiantRestoration.this.random.nextFloat() < 0.3f) {
                         giant.getWorld().playSound(Sound.sound(Key.key("entity.zombie.ambient"), Sound.Source.HOSTILE, 5f, 0.3f), giant.getLocation().getX(), giant.getLocation().getY(), giant.getLocation().getZ());
                     }
                }
            };

            ambientRunnable.runTaskTimer(this, 0, 20 * 20);

            this.ambientRunnables.put(giant, ambientRunnable);
        }
    }

    @EventHandler
    public void removeAmbientTask(final @NotNull EntityRemoveFromWorldEvent entityRemoveFromWorldEvent) {
        if (entityRemoveFromWorldEvent.getEntity() instanceof final @NotNull Giant giant) {
            final @Nullable BukkitRunnable ambientRunnable = this.ambientRunnables.remove(giant);
            if (ambientRunnable != null) {
                ambientRunnable.cancel();
            }
        }
    }

    @EventHandler
    public void makeGiantSilent(final @NotNull EntitySpawnEvent entitySpawnEvent) {
        if (entitySpawnEvent.getEntity() instanceof final @NotNull Giant giant) {
            giant.setSilent(true); // We handle the sounds ourselves.
        }
    }

    @EventHandler
    public void giveGiantPumpkin(final @NotNull EntitySpawnEvent entitySpawnEvent) {
        if (entitySpawnEvent.getEntity() instanceof final @NotNull Giant giant) {
            if (giant.getEquipment().getHelmet() == null) {
                final @NotNull LocalDate localDate = LocalDate.now();
                final int day = localDate.get(ChronoField.DAY_OF_MONTH);
                final int month = localDate.get(ChronoField.MONTH_OF_YEAR);

                if (day == 31 && month == 10 /* Halloween! Spooky! */ && this.random.nextFloat() < 0.55F) {
                    giant.getEquipment().setHelmet(new ItemStack(this.random.nextFloat() < 0.1F ? Material.JACK_O_LANTERN : Material.CARVED_PUMPKIN));
                }
            }
        }
    }

    @EventHandler
    public void burnInSun(final @NotNull ServerTickStartEvent serverTickStartEvent) {
        for (final @NotNull World world : this.getServer().getWorlds()) {
            for (final @NotNull Entity entity : world.getEntities()) {
                if (entity instanceof final @NotNull Giant giant) {
                    if (giant.isInDaylight()) { // No way to read NBT...
                        final @Nullable ItemStack helmet = giant.getEquipment().getHelmet();
                        if (helmet != null && helmet.getType() != Material.AIR) {
                            if (helmet.getItemMeta() instanceof final @NotNull Damageable damageable) {
                                damageable.setDamage(damageable.getDamage() + this.random.nextInt(2));
                                // Item may need to be specially broken if it is over it's max damage.
                            }

                            continue; // This giant is protected from sun fire.
                        }

                        giant.setFireTicks(8 /* default */ * 20);
                    }
                }
            }
        }
    }

    // Giants don't get reinforcements. They are mini-bosses in it of themselves.

    // Giants can't turn villagers into zombie villagers. Seriously, they are 20x the size!

    @EventHandler(ignoreCancelled = true)
    public void setCorrectXp(final @NotNull EntityDeathEvent entityDeathEvent) {
        if (entityDeathEvent.getEntity() instanceof Giant) {
            entityDeathEvent.setDroppedExp(20);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGiantHurt(final @NotNull EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity() instanceof Giant) {
            entityDamageEvent.getEntity().getWorld().playSound(Sound.sound(Key.key("entity.zombie.hurt"), Sound.Source.HOSTILE, 5f, 0.3f), entityDamageEvent.getEntity().getLocation().getX(), entityDamageEvent.getEntity().getLocation().getY(), entityDamageEvent.getEntity().getLocation().getZ());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGiantMove(final @NotNull EntityMoveEvent entityMoveEvent) {
        if (entityMoveEvent.getEntity() instanceof Giant && entityMoveEvent.hasExplicitlyChangedBlock()) {
            if (this.random.nextFloat() > 0.25f) { // 3/4ths chance we get here.
                entityMoveEvent.getTo().getWorld().playSound(Sound.sound(Key.key("entity.zombie.step"), Sound.Source.HOSTILE, 5f, 0.3f), entityMoveEvent.getEntity().getLocation().getX(), entityMoveEvent.getEntity().getLocation().getY(), entityMoveEvent.getEntity().getLocation().getZ());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGiantDie(final @NotNull EntityDeathEvent entityDeathEvent) {
        if (entityDeathEvent.getEntity() instanceof final @NotNull Giant giant) {
            entityDeathEvent.setShouldPlayDeathSound(true);
            entityDeathEvent.setDeathSound(org.bukkit.Sound.ENTITY_ZOMBIE_DEATH);
            entityDeathEvent.setDeathSoundPitch(0.3f);
            entityDeathEvent.setDeathSoundVolume(5f);
            entityDeathEvent.setDeathSoundCategory(SoundCategory.HOSTILE);
        }
    }

    // Giants don't drop zombie heads. They aren't zombies.
}
