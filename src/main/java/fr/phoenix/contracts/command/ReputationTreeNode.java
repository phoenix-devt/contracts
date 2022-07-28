package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.command.objects.parameter.Parameter;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReputationTreeNode extends CommandTreeNode {


    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public ReputationTreeNode(CommandTreeNode parent) {
        super(parent, "rep");
        addParameter(Parameter.PLAYER_OPTIONAL);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return CommandResult.FAILURE;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            InventoryManager.REPUTATION.newInventory(PlayerData.get(player.getUniqueId())).open();
            return CommandResult.SUCCESS;
        }
        if (args.length == 2) {
            PlayerData reputationPlayer = PlayerData.get(args[1]);
            if (reputationPlayer == null) {
                Message.NOT_VALID_PLAYER.format("input", args[2]).send(player);
                return CommandResult.FAILURE;
            }
            InventoryManager.REPUTATION.newInventory(PlayerData.get(player.getUniqueId()), reputationPlayer).open();
            return CommandResult.SUCCESS;
        }
        //If length !=2 and !=3
        return CommandResult.FAILURE;

    }
}
