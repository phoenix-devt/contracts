package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.gui.objects.EditableInventory;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.InventoryItem;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.gui.objects.item.SimpleItem;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class ContractMiddlemanViewer extends EditableInventory {
    public ContractMiddlemanViewer() {
        super("contract-middleman");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("go-back"))
            return new GoBackItem(config);
        if (function.equals("change-view"))
            return new ChangeStateItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return null;
    }

    /**
     * @param ownContracts if it shows the contracts of the middle men or a list of all the different contracts
     *                     waiting for a middle man.
     * @return
     */
    public ContractMiddlemanInventory newInventory(PlayerData playerData, boolean ownContracts) {

        return new ContractMiddlemanInventory(playerData, this, ownContracts);
    }

    public class ChangeStateItem extends InventoryItem<ContractMiddlemanInventory> {
        private final ConfigurationSection config;

        public ChangeStateItem(ConfigurationSection config) {
            super(config);
            this.config = config;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            ContractState viewState = inv.otherContractViews.get(n);
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
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("view-state", ContractsUtils.chatName(inv.otherContractViews.get(n).toString()));
            return holders;
        }
    }

    public class ContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public ContractItem(ConfigurationSection config) {
            super(config);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getId().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            return contract.getContractPlaceholder(inv.getPlayerData());
        }
    }

    public class GoBackItem extends SimpleItem<ContractMiddlemanInventory> {

        public GoBackItem(ConfigurationSection config) {
            super(config);
        }
    }

    public class PreviousPageItem extends SimpleItem<ContractMiddlemanInventory> {

        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractMiddlemanInventory inv) {
            return inv.page > 0;
        }

    }

    public class NextPageItem extends SimpleItem<ContractMiddlemanInventory> {

        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractMiddlemanInventory inv) {
            return inv.page < inv.maxPage;
        }
    }

    public class ContractMiddlemanInventory extends GeneratedInventory {
        private int page = 0;
        private final int contractsPerPage;
        private  List<Contract> displayedContracts;
        private int maxPage;
        private final List<ContractState> allViewedContractStates=Arrays.asList(ContractState.MIDDLEMAN_DISPUTED,ContractState.ADMIN_DISPUTED,ContractState.RESOLVED);
        private ContractState contractState=ContractState.ADMIN_DISPUTED;
        private List<ContractState> otherContractViews=Arrays.asList(ContractState.MIDDLEMAN_DISPUTED,ContractState.RESOLVED);

        public ContractMiddlemanInventory(PlayerData playerData, EditableInventory editable, boolean ownContracts) {
            super(playerData, editable);
            displayedContracts = Contracts.plugin.contractManager.getContractsOfState(ContractState.OPEN /* TODO */).stream()
                    .filter(contract -> contract.getState() == ContractState.WAITING_ACCEPTANCE)
                    .sorted((contract1, contract2) -> (int) (contract1.getEnteringTime(ContractState.WAITING_ACCEPTANCE) - contract2.getEnteringTime(ContractState.WAITING_ACCEPTANCE))).collect(Collectors.toList());
            contractsPerPage = getEditable().getByFunction("contract").getSlots().size();

            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{type}", ContractsUtils.chatName(/* contractType.toString() */ "AHEM")); // TODO
        }

        public void changeState(ContractState contractState) {
            this.contractState= contractState;
            otherContractViews= allViewedContractStates.stream().filter(contractView1 -> contractView1 != contractState).collect(Collectors.toList());
            displayedContracts = playerData.getMiddlemanContracts(contractState);
            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item instanceof GoBackItem) {
                InventoryManager.CONTRACT_TYPE.newInventory(playerData, ContractTypeViewer.InventoryToOpenType.MARKET_VIEWER).open();
            }
            if (item instanceof NextPageItem) {
                page++;
                open();
            }
            if (item instanceof PreviousPageItem) {
                page--;
                open();
            }
            if (item instanceof ChangeStateItem) {
                ContractState newView = ContractState.valueOf(event.getCurrentItem().getItemMeta().getPersistentDataContainer().
                        get(new NamespacedKey(Contracts.plugin, "contract-view"), PersistentDataType.STRING));
                changeState(newView);
                open();
            }
            //TODO
            if (item instanceof ContractItem) {
                Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(Objects.requireNonNull(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING))));
                //If left click, shows the reputation of the player
                if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                    InventoryManager.REPUTATION.newInventory(playerData, PlayerData.getOrLoad(contract.getEmployer()), this).open();
                }
            }

        }


        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

}
