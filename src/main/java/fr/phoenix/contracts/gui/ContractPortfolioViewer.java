package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.ReputationViewerCommand;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
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
        private final Map<ContractState, InventoryItem> inventoryItems = new HashMap<>();

        public ContractItem(ConfigurationSection config) {
            super(config);
            Material material = Material.valueOf(Objects.requireNonNull(ContractsUtils.enumName(config.getString("item"))));
            for (ContractState contractState : ContractState.values()) {
                ConfigurationSection section = Objects.requireNonNull(config.getConfigurationSection(ContractsUtils.ymlName(contractState.toString()))
                        , "Could not load " + ContractsUtils.ymlName(contractState.toString()) + " config");
                inventoryItems.put(contractState, getItem(section, contractState, material));
            }
        }

        public InventoryItem getItem(ConfigurationSection section, ContractState state, Material material) {
            switch (state) {
                case WAITING_ACCEPTANCE:
                    return new WaitingAcceptanceContractItem(this, section, material);
                case OPEN:
                    return new OpenContractItem(this, section, material);
                case FULFILLED:
                    return new FulfilledContractItem(this, section, material);
                case MIDDLEMAN_DISPUTED:
                    return new MiddlemanDisputedContractItem(this, section, material);
                case MIDDLEMAN_RESOLVED:
                    return new MiddlemanResolvedContractItem(this, section, material);
                case ADMIN_DISPUTED:
                    return new AdminDisputedContractItem(this, section, material);
                case RESOLVED:
                    return new ResolvedContractItem(this, section, material);

            }
            return null;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }


        @Override
        public ItemStack getDisplayedItem(ContractPortfolioInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            Contract contract = inv.displayedContracts.get(inv.page + n);

            ItemStack item = inventoryItems.get(contract.getState()).getDisplayedItem(inv, n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getId().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = contract.getContractPlaceholder(inv.getPlayerData());
            if (contract.getEmployee()!=null&&contract.getEmployee().equals(inv.getPlayerData().getUuid()))
                holders.register("employee","You");
            else
                holders.register("employer","You");

            return holders;
        }
    }

    public class WaitingAcceptanceContractItem extends InventoryItem<ContractPortfolioInventory> {

        public WaitingAcceptanceContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            Placeholders holders = parent.getPlaceholders(inv, n);
            return holders;
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

    public class AdminDisputedContractItem extends InventoryItem<ContractPortfolioInventory> {

        public AdminDisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class ResolvedContractItem extends InventoryItem<ContractPortfolioInventory> {

        public ResolvedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class FulfilledContractItem extends InventoryItem<ContractPortfolioInventory> {
        public FulfilledContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractPortfolioInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class MiddlemanDisputedContractItem extends InventoryItem<ContractPortfolioInventory> {

        public MiddlemanDisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
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

    public class MiddlemanResolvedContractItem extends InventoryItem<ContractPortfolioInventory> {

        public MiddlemanResolvedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
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
        private List<Contract> displayedContracts = playerData.getContracts(viewState.corresponding);
        private int page = 0;
        private final int contractsPerPage = getEditable().getByFunction("contract").getSlots().size();
        private int maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        public ContractPortfolioInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        public void changeState(ViewState viewState) {
            this.viewState = viewState;
            otherViewStates = Arrays.stream(ViewState.values()).filter(viewState1 -> viewState1 != viewState).collect(Collectors.toList());
            displayedContracts = playerData.getContracts(viewState.corresponding);
            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(str.replace("{view-state}", viewState.toString()));
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
                if (!event.getCurrentItem().hasItemMeta())
                    return;
                Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(
                        Objects.requireNonNull(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                                .get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING))));


                if (contract.getState() == ContractState.WAITING_ACCEPTANCE && event.getClick() == ClickType.LEFT) {
                    InventoryManager.PROPOSAL.generate(playerData, contract, this).open();
                }

               if(contract.hasBeenIn(ContractState.OPEN)&&event.getClick()==ClickType.LEFT) {
                   InventoryManager.ACTION.generate(playerData,contract,this).open();

               }
                if (event.getClick() == ClickType.SHIFT_RIGHT&&contract.getState()!=ContractState.WAITING_ACCEPTANCE) {
                    InventoryManager.REPUTATION.newInventory(playerData, contract.getOther(playerData), this).open();
                }

            }
        }


        @Override
        public void whenClosed(InventoryCloseEvent event) {
            //nothing
        }
    }


    /**
     * Sends a clickable message employer the player corresponding employer the review he wants employer post.
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
