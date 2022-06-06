package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.contract.Contract;
import fr.lezoo.contracts.contract.ContractState;
import fr.lezoo.contracts.contract.ContractType;
import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import fr.lezoo.contracts.gui.objects.item.Placeholders;
import fr.lezoo.contracts.gui.objects.item.SimpleItem;
import fr.lezoo.contracts.manager.InventoryManager;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.utils.ChatInput;
import fr.lezoo.contracts.utils.ContractsUtils;
import fr.lezoo.contracts.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class ContractMarketViewer extends EditableInventory {
    public ContractMarketViewer() {
        super("contract-market");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function == null)
            Contracts.log(Level.SEVERE, "Couldn't load the Contract Market GUI there is an item without any function.");
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("go-back"))
            return new GoBackItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return null;
    }


    public ContractMarketInventory newInventory(PlayerData playerData, ContractType contractType) {
        return new ContractMarketInventory(playerData, this, contractType);
    }


    public class ContractItem extends InventoryItem<ContractMarketInventory> {
        private ContractPortfolioViewer.WaitingAcceptanceContractItem waitingAcceptanceItem;
        private ContractPortfolioViewer.OpenContractItem openContractItem;
        private ContractPortfolioViewer.DisputedContractItem disputedContractItem;
        private ContractPortfolioViewer.EndedContractItem endedContractItem;

        public ContractItem(ConfigurationSection config) {
            super(config);
        }


        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }


        @Override
        public ItemStack getDisplayedItem(ContractMarketInventory inv, int n) {
            if (inv.page + n >= inv.displayedContracts.size())
                return new ItemStack(Material.AIR);
            ItemStack item = super.getDisplayedItem(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUuid().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMarketInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = new Placeholders();
            holders.register("name", contract.getName());
            holders.register("employer", contract.getEmployerName());
            holders.register("payment-amount", contract.getPaymentInfo().getAmount());
            holders.register("payment-type", ContractsUtils.chatName(contract.getPaymentInfo().getType().toString()));
            holders.register("create-since", ContractsUtils.timeSinceInHours(contract.getCreationTime()) + " h");

            //TODO: Un item par type de contrat
            return holders;
        }
    }


    public class GoBackItem extends SimpleItem<ContractMarketInventory> {

        public GoBackItem(ConfigurationSection config) {
            super(config);
        }
    }


    public class PreviousPageItem extends SimpleItem<ContractMarketInventory> {

        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractMarketInventory inv) {
            return inv.page > 0;
        }

    }

    public class NextPageItem extends SimpleItem<ContractMarketInventory> {

        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ContractMarketInventory inv) {
            return inv.page < inv.maxPage;
        }
    }


    public class ContractMarketInventory extends GeneratedInventory {
        private ContractType contractType;
        private int page = 0;
        private final int contractsPerPage = getEditable().getByFunction("contract").getSlots().size();
        //TODO Sort the contract by pertinence
        private final List<Contract> displayedContracts = Contracts.plugin.contractManager.getContractOfType(contractType).stream()
                .filter(contract -> contract.getState() == ContractState.WAITING_ACCEPTANCE)
                .sorted((contract1, contract2) -> (int) (contract1.getCreationTime() - contract2.getCreationTime())).collect(Collectors.toList());
        private int maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;


        public ContractMarketInventory(PlayerData playerData, EditableInventory editable, ContractType contractType) {
            super(playerData, editable);
            this.contractType = contractType;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return null;
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
            if (item instanceof ContractItem) {
                Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(Objects.requireNonNull(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING))));

                //If left click, shows the reputation of the player
                if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                    InventoryManager.REPUTATION.newInventory(playerData, PlayerData.get(contract.getEmployee()),this).open();
                }
                if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                    //We accept the contract after a chat input is displayed
                    Message.ARE_YOU_SURE_TO_ACCEPT.format("contract-name", contract.getName()).send(player);
                    new ChatInput(playerData, (playerData, str) -> {
                        if (str.replace(" ", "").equalsIgnoreCase("yes")) {
                            contract.whenAccepted(playerData.getUuid());
                        } else
                            Message.CONTRACT_REFUSED.format("contract-name", contract.getName()).send(player);
                        return true;
                    });

                }
            }

        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }
}
