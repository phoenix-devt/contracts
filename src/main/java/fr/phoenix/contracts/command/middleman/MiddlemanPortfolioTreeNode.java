package fr.phoenix.contracts.command.middleman;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiddlemanPortfolioTreeNode extends CommandTreeNode {

    public MiddlemanPortfolioTreeNode(CommandTreeNode parent) {
        super(parent, "portfolio");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if(sender instanceof Player player) {
            if(args.length!=2)
                return CommandResult.THROW_USAGE;
            PlayerData playerData=PlayerData.getOrLoad(player);
            if(!playerData.isMiddleman()) {
                player.sendMessage(ChatColor.RED+"This command is only for middlemen.");
                return CommandResult.FAILURE;
            }
            InventoryManager.CONTRACT_MIDDLEMAN.generate(playerData).open();
        }
        return CommandResult.FAILURE;
    }
}
