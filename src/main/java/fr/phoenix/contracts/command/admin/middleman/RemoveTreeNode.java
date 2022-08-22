package fr.phoenix.contracts.command.admin.middleman;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.command.objects.parameter.Parameter;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class RemoveTreeNode extends CommandTreeNode {
    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public RemoveTreeNode(CommandTreeNode parent) {
        super(parent,"remove");
        addParameter( new Parameter("middleman-player",
                (explorer,list)-> Contracts.plugin.playerManager.getAllPlayerData()
                        .stream()
                        .filter(player-> player.isMiddleman())
                        .forEach(player->list.add(player.getPlayerName()))));

    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length != 4)
            return CommandResult.THROW_USAGE;
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[3]);
        if (!Contracts.plugin.playerManager.has(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid player.");
            return CommandResult.FAILURE;
        }
        PlayerData playerData = PlayerData.getOrLoad(player.getUniqueId());
        if (!playerData.isMiddleman()) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a middleman.");
            return CommandResult.FAILURE;
        }
        playerData.setMiddleman(false);
        Contracts.plugin.middlemenManager.removeMiddlemen(playerData);
        return CommandResult.SUCCESS;
    }
}
