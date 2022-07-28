package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.objects.CommandTreeRoot;

public class ContractTreeRoot extends CommandTreeRoot {
    /**
     * First class called when creating a command tree
     *
     * @param id         The command tree root id
     * @param permission The eventual permission the player needs to have in order to
     */
    public ContractTreeRoot(String id, String permission) {
        super(id, permission);
        addChild(new CreateTreeNode(this));
        addChild(new MarketTreeNode(this));
        addChild(new PortfolioTreeNode(this));
        addChild(new ReputationTreeNode(this));
    }
}
