package us.tastybento.bskyblock.commands.admin.teams;

import java.util.List;
import java.util.UUID;

import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

public class AdminTeamKickCommand extends CompositeCommand {

    public AdminTeamKickCommand(CompositeCommand parent) {
        super(parent, "kick");

    }

    @Override
    public void setup() {
        setPermission("admin.team");
        setParameters("commands.admin.team.kick.parameters");
        setDescription("commands.admin.team.kick.description");
    }

    @Override
    public boolean execute(User user, List<String> args) {
        // If args are not right, show help
        if (args.size() != 1) {
            showHelp(this, user);
            return false;
        }
        // Get target
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player");
            return false;
        }
        if (!getIslands().hasIsland(getWorld(), targetUUID)) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        if (!getIslands().inTeam(getWorld(), targetUUID)) {
            user.sendMessage("general.errors.not-in-team");
            return false;
        }
        if (getIslands().getTeamLeader(getWorld(), targetUUID).equals(targetUUID)) {
            user.sendMessage("commands.admin.team.kick.cannot-kick-leader");
            getIslands().getIsland(getWorld(), targetUUID).showMembers(getPlugin(), user);
            return false;
        }
        User.getInstance(targetUUID).sendMessage("commands.admin.team.kick.admin-kicked");
        getIslands().removePlayer(getWorld(), targetUUID);
        user.sendMessage("general.success");
        return true;

    }


}
