package fr.phoenix.contracts.command.admin.middleman;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class AdminMiddlemanTreeNode extends CommandTreeNode {

    public AdminMiddlemanTreeNode(CommandTreeNode parent) {
        super(parent, "middleman");
        addChild(new AddTreeNode(this));
        addChild(new RemoveTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
