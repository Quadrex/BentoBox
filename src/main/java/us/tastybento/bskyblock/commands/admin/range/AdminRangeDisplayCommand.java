package us.tastybento.bskyblock.commands.admin.range;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Poslovitch
 */
public class AdminRangeDisplayCommand extends CompositeCommand {

    private Map<User, Integer> display = new HashMap<>();

    public AdminRangeDisplayCommand(CompositeCommand parent) {
        super(parent, "display", "show", "hide");
    }

    @Override
    public void setup() {
        setOnlyPlayer(true);
        setPermission("admin.range.display");
        setParameters("commands.admin.range.display.parameters");
        setDescription("commands.admin.range.display.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // According to the label used to execute the command, there is a different behaviour
        // - display : toggle on/off
        // - show : only set on - and send "error" if already on
        // - hide : only set off - same if already off

        if (!display.containsKey(user)) {
            switch (label) {
                case "display":
                case "show":
                    showZones(user);
                    break;
                case "hide":
                    user.sendMessage("commands.admin.range.display.already-off");
                    break;
            }
        } else {
            switch (label) {
                case "display":
                case "hide":
                    hideZones(user);
                    break;
                case "show":
                    user.sendMessage("commands.admin.range.display.already-on");
                    break;
            }
        }

        return true;
    }

    private void showZones(User user) {
        user.sendMessage("commands.admin.range.display.showing");
        user.sendMessage("commands.admin.range.display.hint");
        display.put(user, Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), () -> {
            if (!user.getPlayer().isOnline()) {
                hideZones(user);
            }

            getIslands().getIslandAt(user.getLocation()).ifPresent(island -> {
                // Draw the island protected area
                drawZone(user.getPlayer(), Particle.BARRIER, island.getCenter(), island.getProtectionRange());

                // Draw the default protected area if island protected zone is different
                if (island.getProtectionRange() != getPlugin().getSettings().getIslandProtectionRange()) {
                    drawZone(user.getPlayer(), Particle.VILLAGER_HAPPY, island.getCenter(), getPlugin().getSettings().getIslandProtectionRange());
                }

                // Draw the island area
                drawZone(user.getPlayer(), Particle.TOWN_AURA, island.getCenter(), island.getRange());
            });
        }, 20, 15));
    }

    private void hideZones(User user) {
        user.sendMessage("commands.admin.range.display.hiding");
        Bukkit.getScheduler().cancelTask(display.get(user));
        display.remove(user);
    }

    private void drawZone(Player player, Particle particle, Location center, int range) {
        // Get player Y coordinate
        int playerY = player.getLocation().getBlockY() + 1;

        // Draw 3 "stages" (one line below, at and above player's y coordinate)
        for (int stage = -1 ; stage <= 1 ; stage++) {
            for (int i = -range ; i <= range ; i++) {
                player.spawnParticle(particle, (double) (center.getBlockX() + i), (double) (playerY + stage), (double) (center.getBlockZ() + range), 1);
                player.spawnParticle(particle, (double) (center.getBlockX() + i), (double) (playerY + stage), (double) (center.getBlockZ() - range), 1);
                player.spawnParticle(particle, (double) (center.getBlockX() + range), (double) (playerY + stage), (double) (center.getBlockZ() + i), 1);
                player.spawnParticle(particle, (double) (center.getBlockX() - range), (double) (playerY + stage), (double) (center.getBlockZ() + i), 1);
            }
        }
    }
}