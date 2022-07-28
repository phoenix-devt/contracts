package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


public class ContractMarketViewer extends EditableInventory {
    public ContractMarketViewer() {
        super("contract-market");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("go-back"))
            return new GoBackItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return new SimpleItem(config);
    }

    public ContractMarketInventory newInventory(PlayerData playerData, ContractType contractType) {

        return new ContractMarketInventory(playerData, this, contractType);
    }


    public class ContractItem extends InventoryItem<ContractMarketInventory> {

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
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getId().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMarketInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            Placeholders holders = new Placeholders();
            holders.register("name", contract.getName());
            holders.register("employer", contract.getEmployerName());
            holders.register("payment-amount", contract.getAmount());
            holders.register("created-since", ContractsUtils.timeSinceInHours(contract.getCreationTime()) + " h");

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
        private final int contractsPerPage;
        //TODO Sort the contract by pertinence
        private final List<Contract> displayedContracts;
        private int maxPage;

        public ContractMarketInventory(PlayerData playerData, EditableInventory editable, ContractType contractType) {
            super(playerData, editable);
            this.contractType = contractType;
            displayedContracts = Contracts.plugin.contractManager.getContractsOfType(contractType).stream()
                    .filter(contract -> contract.getState() == ContractState.WAITING_ACCEPTANCE)
                    .sorted((contract1, contract2) -> (int) (contract1.getCreationTime() - contract2.getCreationTime())).collect(Collectors.toList());
            contractsPerPage = getEditable().getByFunction("contract").getSlots().size();

            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{type}", ContractsUtils.chatName(contractType.toString()));
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
                    InventoryManager.REPUTATION.newInventory(playerData, PlayerData.get(contract.getEmployer()), this).open();
                }
                if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                    if (playerData.getUuid().equals(contract.getEmployer())) {
                        Message.CANT_ACCEPT_OWN_CONTRACT.format().send(player);
                        return;
                    }


                    player.getOpenInventory().close();
                    //We accept the contract after a chat input is displayed
                    Message.ARE_YOU_SURE_TO_ACCEPT.format("contract-name", contract.getName()).send(player);
                    new ChatInput(playerData, (playerData, str) -> {
                        if (str.replace(" ", "").equalsIgnoreCase("yes")) {
                            //We must run sync
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Contracts.plugin, () -> {
                                contract.whenAccepted(playerData.getUuid());
                            });

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
