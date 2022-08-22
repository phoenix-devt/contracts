package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.Proposal;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.gui.*;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import org.bukkit.Bukkit;
import org.checkerframework.checker.units.qual.A;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class InventoryManager {
    public static final ContractCreationViewer CONTRACT_CREATION = new ContractCreationViewer();
    public static final ContractMarketViewer CONTRACT_MARKET = new ContractMarketViewer();
    public static final ContractPortfolioViewer CONTRACT_PORTFOLIO = new ContractPortfolioViewer();
    public static final ContractTypeViewer CONTRACT_TYPE = new ContractTypeViewer();
    public static final ConfirmationViewer CONFIRMATION = new ConfirmationViewer();
    public static final ProposalViewer PROPOSAL = new ProposalViewer();
    public static final ReputationViewer REPUTATION = new ReputationViewer();
    public static ActionViewer ACTION = new ActionViewer();
    public static List<EditableInventory> list = Arrays.asList(ACTION,PROPOSAL
            , CONFIRMATION, REPUTATION, CONTRACT_TYPE, CONTRACT_PORTFOLIO, CONTRACT_MARKET, CONTRACT_CREATION);


    public static void load() {
        list.forEach(inv -> {
            Contracts.plugin.configManager.loadDefaultFile("gui", inv.getId() + ".yml");
            try {
                inv.reload(new ConfigFile("/gui", inv.getId()).getConfig());
            } catch (IllegalArgumentException exception) {
                Contracts.log(Level.WARNING, "Could not load inventory " + inv.getId() + ": " + exception.getMessage());
            }
        });
    }
}
