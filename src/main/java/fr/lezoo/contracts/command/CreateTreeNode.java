package fr.lezoo.contracts.command;

import fr.lezoo.contracts.command.objects.CommandTreeNode;
import fr.lezoo.contracts.gui.ContractTypeViewer;
import fr.lezoo.contracts.manager.InventoryManager;
import fr.lezoo.contracts.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateTreeNode extends CommandTreeNode {
    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public CreateTreeNode(CommandTreeNode parent) {
        super(parent, "create");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return CommandResult.FAILURE;
        }
        if (args.length != 1)
            return CommandResult.FAILURE;

        Player player = (Player) sender;
        InventoryManager.CONTRACT_TYPE.newInventory(PlayerData.get(player.getUniqueId()), ContractTypeViewer.InventoryToOpenType.CREATION_VIEWER).open();

        return CommandResult.SUCCESS;
    }
}
