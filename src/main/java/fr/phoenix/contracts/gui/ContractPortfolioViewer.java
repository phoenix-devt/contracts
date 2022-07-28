package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.ReputationViewerCommand;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.review.ContractReview;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ContractPortfolioViewer extends EditableInventory {


    public ContractPortfolioViewer() {
        super("contract-portfolio");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);
        if (function.equals("change-view"))
            return new ChangeViewItem(config);
        return null;
    }

    public ContractPortfolioInventory newInventory(PlayerData playerData) {
        return new ContractPortfolioInventory(playerData, this);
    }


    public class ChangeViewItem extends InventoryItem<ContractPortfolioInventory> {
        private final ConfigurationSection config;

        public ChangeViewItem(ConfigurationSection config) {
            super(config);
            this.config = config;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractPortfolioInventory inv, int n) {
            ViewState viewState = inv.otherViewStates.get(n);
            Material displayMaterial = Material.AIR;
            try {
                displayMaterial = Objects.requireNonNull(Material.valueOf(ContractsUtils.enumName(config.getString("material" + (n + 1)))));
            } catch (Exception e) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Couldn't load material" + (n + 1) + ":" + config.getString("material" + (n + 1)) + " for the change view item of the contracts gui");
            }

            ItemStack item = super.getDisplayedItem(inv, n, displayMaterial);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "view-state"), PersistentDataType.STRING, viewState.toString());
            item.setItemMeta(meta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("view-state", ContractsUtils.chatName(inv.otherViewStates.get(n).toString()));
            return holders;
        }
    }


    public class ContractItem extends InventoryItem<ContractPortfolioInventory> {
        private WaitingAcceptanceContractItem waitingAcceptanceItem;
        private OpenContractItem openContractItem;
        private DisputedContractItem disputedContractItem;
        private EndedContractItem endedContractItem;

        public ContractItem(ConfigurationSection config) {
            super(config);
            Material material = Material.valueOf(Objects.requireNonNull(ContractsUtils.enumName(config.getString("item"))));
            ConfigurationSection waitingAcceptance = config.getConfigurationSection("waiting-acceptance");
            ConfigurationSection open = config.getConfigurationSection("open");
            ConfigurationSection disputed = config.getConfigurationSection("disputed");
            ConfigurationSection ended = config.getConfigurationSection("ended");
            Validate.notNull(waitingAcceptance, "Could not load 'waiting-acceptance' config");
            Validate.notNull(open, "Could not load 'open' config");
            Validate.notNull(disputed, "Could not load 'disputed' config");
            Validate.notNull(ended, "Could not load 'ended' config");
            waitingAcceptanceItem = new WaitingAcceptanceContractItem(this, waitingAcceptance, material);
            openContractItem = new OpenContractItem(this, waitingAcceptance, material);
            disputedContractItem = new DisputedContractItem(this, waitingAcceptance, material);
            endedContractItem = new EndedContractItem(this, waitingAcceptance, material);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }


        @Override
        public ItemStack getDisplayedItem(ContractPortfolioInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = null;
            Contract contract = inv.displayedContracts.get(inv.page + n);
            switch (inv.viewState) {
                case WAITING_ACCEPTANCE:
                    item = waitingAcceptanceItem.getDisplayedItem(inv, n);
                    break;
                case OPEN:
                    item = openContractItem.getDisplayedItem(inv, n);
                    break;
                case DISPUTED:
                    item = disputedContractItem.getDisplayedItem(inv, n);
                    break;
                case ENDED:
                    item = endedContractItem.getDisplayedItem(inv, n);
                    break;
            }
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUuid().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = new Placeholders();
            holders.register("name", contract.getName());
            holders.register("employee", contract.getEmployee() != null ? contract.getEmployeeName() : "");
            holders.register("employer", contract.getEmployerName());
            holders.register("payment-amount", contract.getAmount());
            holders.register("created-since", ContractsUtils.timeSinceInHours(contract.getCreationTime()) + " h");
            holders.register("accepted-since", contract.getAcceptanceTime() != 0 ? ContractsUtils.timeSinceInHours(contract.getAcceptanceTime()) + " h" : "Not Accepted");
            holders.register("ended-since", contract.isEnded() ? ContractsUtils.timeSinceInHours(contract.getEndTime()) + " h" : "Not Finished");
            return holders;
        }
    }

    public class WaitingAcceptanceContractItem extends InventoryItem<ContractPortfolioInventory> {

        public WaitingAcceptanceContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class OpenContractItem extends InventoryItem<ContractPortfolioInventory> {

        public OpenContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class DisputedContractItem extends InventoryItem<ContractPortfolioInventory> {
        public DisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class EndedContractItem extends InventoryItem<ContractPortfolioInventory> {

        public EndedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public ItemStack getDisplayedItem(ContractPortfolioInventory inv, int n) {
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);

            //For a ended contract item: 2 containers, if it can be reviewed and the uuid of the contract
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "can-review"), PersistentDataType.INTEGER,
                    inv.getPlayerData().canLeaveReview(contract) ? 1 : 0);
            item.setItemMeta(itemMeta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            Placeholders holders = parent.getPlaceholders(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            holders.register("can-review", inv.getPlayerData().canLeaveReview(contract));
            return holders;
        }
    }


    public class PreviousPageItem extends SimpleItem<ContractPortfolioInventory> {

        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractPortfolioInventory inv) {
            return inv.page > 0;
        }

    }

    public class NextPageItem extends SimpleItem<ContractPortfolioInventory> {

        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override

        public boolean isDisplayed(ContractPortfolioInventory inv) {
            return inv.page < inv.maxPage;
        }
    }


    public class ContractPortfolioInventory extends GeneratedInventory {
        //The type of contracts displayed in the GUI
        private ViewState viewState = ViewState.WAITING_ACCEPTANCE;
        private List<ViewState> otherViewStates = Arrays.stream(ViewState.values()).filter(viewState1 -> viewState1 != viewState).collect(Collectors.toList());
        private List<Contract> displayedContracts = viewState.provide(playerData);
        private int page = 0;
        private final int contractsPerPage = getEditable().getByFunction("contract").getSlots().size();
        private int maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        public ContractPortfolioInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
            Bukkit.broadcastMessage("NAME:"+getName());
        }


        public void changeState(ViewState viewState) {
            this.viewState = viewState;
            otherViewStates = Arrays.stream(ViewState.values()).filter(viewState1 -> viewState1 != viewState).collect(Collectors.toList());
            displayedContracts = viewState.provide(playerData);
            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(str.replace("{view-state}", ContractsUtils.chatName(viewState.toString())));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof NextPageItem) {
                page++;
                open();
            }
            if (item instanceof PreviousPageItem) {
                page--;
                open();
            }
            if (item instanceof ChangeViewItem) {
                ViewState newView = ViewState.valueOf(event.getCurrentItem().getItemMeta().getPersistentDataContainer().
                        get(new NamespacedKey(Contracts.plugin, "view-state"), PersistentDataType.STRING));
                changeState(newView);
                open();
            }
            if (item instanceof ContractItem) {
                if (viewState == ViewState.ENDED) {
                    //If there can be a review from the item (stored in a persistant data container)
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Contracts.plugin, "can-review"), PersistentDataType.INTEGER) == 1) {

                        Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(event.getCurrentItem().getItemMeta().getPersistentDataContainer().
                                get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING)));
                        UUID reviewer = playerData.getUuid();
                        UUID reviewed = contract.getEmployee() != null && contract.getEmployee().equals(playerData.getUuid()) ? contract.getEmployer() : contract.getEmployee();
                        int notation = Contracts.plugin.configManager.defaultNotation;

                        ContractReview review = new ContractReview(reviewed, reviewer, contract, notation, new ArrayList<>());
                        //If the reviewed is not in playerData we load it (even if he is offline)
                        if (!PlayerData.has(reviewed))
                            Contracts.plugin.playerManager.setup(reviewed);
                        PlayerData.get(reviewed).addReview(review);


                        //We close the gui and create the clickable chat message to set comment and notation
                        getPlayer().closeInventory();
                        displayChoices(playerData, review);
                    }
                }
            }
        }


        @Override
        public void whenClosed(InventoryCloseEvent event) {
            //nothing
        }
    }


    /**
     * Sends a clickable message to the player corresponding to the review he wants to post.
     */
    public static void displayChoices(PlayerData playerData, ContractReview review) {
        TextComponent textComponent = new TextComponent(Message.SET_NOTATION_INFO.format("notation", "" + review.getNotation()).getAsString());
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review " + ReputationViewerCommand.NOTATION_ASK + " " + review.getUuid().toString()));
        playerData.getPlayer().spigot().sendMessage(textComponent);


        StringBuilder comment = new StringBuilder();
        for (String str : review.getComment()) {
            comment.append("\n");
            comment.append(str);
        }
        textComponent = new TextComponent(Message.SET_NOTATION_INFO.format("comment", "" + comment.toString()).getAsString());
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review " + ReputationViewerCommand.COMMENT_ASK + " " + review.getUuid().toString()));
        playerData.getPlayer().spigot().sendMessage(textComponent);

    }

}
