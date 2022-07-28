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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class ContractMiddlemanViewer extends EditableInventory {
    public ContractMiddlemanViewer() {
        super("contract-middleman");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function == null)
            Contracts.log(Level.SEVERE, "Couldn't load the Contract Market GUI there is an item without any function.");
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("switch"))
            return new SwitchItem(config);
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
            container.set(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING, contract.getUuid().toString());
            item.setItemMeta(itemMeta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
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
        private boolean ownContracts;
        private int page = 0;
        private final int contractsPerPage;
        private final List<Contract> displayedContracts;
        private int maxPage;

        public ContractMiddlemanInventory(PlayerData playerData, EditableInventory editable, MiddleManContractType ownContracts) {
            super(playerData, editable);
            this.ownContracts = ownContracts;
            displayedContracts = Contracts.plugin.contractManager.getContractOfState(C).stream()
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


    public enum MiddlemanContractType {
        //Contract with is a dispute but with no middle man engaged
        WAITING_MIDDLEMAN_CONTRACT(playerData -> Contracts.plugin.contractManager.getContractOfState(ContractState.WAITING_MIDDLEMAN)
                .stream().sorted((contract1, contract2) -> (int) (contract1.getCreationTime() - contract2.getCreationTime())).collect(Collectors.toList()))
        ,
        //The contracts of a middle man
        OWN_CONTRACT(playerData -> playerData.)
        //The admin disputed contracts of a middle man
        ,OWN_ADMIN_DISPUTED_CONTRACT;


        private final Function<PlayerData, List<Contract>> contractProvider;

        //enum constructor must be private
        MiddlemanContractType(Function<PlayerData, List<Contract>> contractProvider) {
            this.contractProvider = contractProvider;
        }

        public List<Contract> provideContracts(PlayerData playerData) {
            return contractProvider.apply(playerData);
        }
    }
}
