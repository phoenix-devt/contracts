package fr.phoenix.contracts.command.middleman;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MiddlemanTreeNode  extends CommandTreeNode {

    public MiddlemanTreeNode(CommandTreeNode parent) {
        super(parent, "middleman");
        addChild(new MiddlemanPortfolioTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
