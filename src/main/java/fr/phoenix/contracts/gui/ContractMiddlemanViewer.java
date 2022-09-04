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
        if (function.equals("change-state"))
            return new ChangeStateItem(config);
        if (function.equals("contract"))
            return new ContractItem(config);

        return null;
    }

    public ContractMiddlemanInventory generate(PlayerData playerData) {
        return new ContractMiddlemanInventory(playerData, this);
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
            ContractState contractState = inv.otherContractStates.get(n);
            Material displayMaterial = Material.AIR;
            try {
                displayMaterial = Objects.requireNonNull(Material.valueOf(ContractsUtils.enumName(config.getString("material" + (n + 1)))));
            } catch (Exception e) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Couldn't load material" + (n + 1) + ":" + config.getString("material" + (n + 1)) + " for the change state item of the contracts gui");
            }

            ItemStack item = super.getDisplayedItem(inv, n, displayMaterial);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Contracts.plugin, "contract-state"), PersistentDataType.STRING, contractState.toString());
            item.setItemMeta(meta);
            return item;
        }


        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("contract-state", ContractsUtils.chatName(inv.otherContractStates.get(n).toString()));
            return holders;
        }
    }

    public class ContractItem extends InventoryItem<ContractMiddlemanInventory> {
        private double amount;


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

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Contract contract = inv.displayedContracts.get(inv.page + n);
            return contract.getContractPlaceholder(inv.getPlayerData());
        }
    }

    public class AdminDisputedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public AdminDisputedContractItem(ContractPortfolioViewer.ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }

    public class ResolvedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public ResolvedContractItem(ContractPortfolioViewer.ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            return parent.getPlaceholders(inv, n);
        }
    }



    public class MiddlemanDisputedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public MiddlemanDisputedContractItem(ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
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
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Placeholders holders = parent.getPlaceholders(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            holders.register("can-review", inv.getPlayerData().canLeaveReview(contract));
            return holders;
        }
    }

    public class MiddlemanResolvedContractItem extends InventoryItem<ContractMiddlemanInventory> {

        public MiddlemanResolvedContractItem(ContractPortfolioViewer.ContractItem parent, ConfigurationSection config, Material material) {
            super(parent, config, material);
        }

        @Override
        public ItemStack getDisplayedItem(ContractMiddlemanInventory inv, int n) {
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
        public Placeholders getPlaceholders(ContractMiddlemanInventory inv, int n) {
            Placeholders holders = parent.getPlaceholders(inv, n);
            Contract contract = inv.displayedContracts.get(inv.page + n);
            holders.register("can-review", inv.getPlayerData().canLeaveReview(contract));
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
        private int page = 0;
        private final int contractsPerPage;
        private List<Contract> displayedContracts;
        private int maxPage;
        private final List<ContractState> allViewedContractStates = Arrays.asList(ContractState.MIDDLEMAN_DISPUTED, ContractState.ADMIN_DISPUTED, ContractState.RESOLVED);
        private ContractState contractState = ContractState.MIDDLEMAN_DISPUTED;
        private List<ContractState> otherContractStates = Arrays.asList(ContractState.MIDDLEMAN_RESOLVED, ContractState.ADMIN_DISPUTED, ContractState.RESOLVED);

        public ContractMiddlemanInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
            displayedContracts = playerData.getMiddlemanContracts(contractState);
            contractsPerPage = getEditable().getByFunction("contract").getSlots().size();

            maxPage = Math.max(0, displayedContracts.size() - 1) / contractsPerPage;

        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{type}", ContractsUtils.chatName(/* contractType.toString() */ "AHEM")); // TODO
        }

        public void changeState(ContractState contractState) {
            this.contractState = contractState;
            otherContractStates = allViewedContractStates.stream().filter(contractView1 -> contractView1 != contractState).collect(Collectors.toList());
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
                        get(new NamespacedKey(Contracts.plugin, "contract-state"), PersistentDataType.STRING));
                changeState(newView);
                open();
            }

            if (item instanceof ContractItem contractItem) {
                Contract contract = Contracts.plugin.contractManager.get(UUID.fromString(Objects.requireNonNull(event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(Contracts.plugin, "contract"), PersistentDataType.STRING))));

                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    InventoryManager.REPUTATION.newInventory(playerData, PlayerData.getOrLoad(contract.getEmployer()), this).open();
                    return;
                }

                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    InventoryManager.REPUTATION.newInventory(playerData, PlayerData.getOrLoad(contract.getEmployee()), this).open();
                    return;
                }

                if (event.getClick() == ClickType.RIGHT) {
                    //If the player is already on chat input we block the access employer a new chat input.
                    if (playerData.isOnChatInput()) {
                        Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
                        return;
                    }
                    double max = contract.getAmount();
                    double min = -contract.getGuarantee();
                    Message.RESOLVE_DISPUTE_ASK.format("max", max, "min", min).send(playerData.getPlayer());
                    new ChatInput(playerData, this, (p, val) -> {
                        try {
                            double amount = Double.parseDouble(val);
                            if (amount < min || amount > max) {
                                Message.NOT_IN_LIMIT.format("amount", amount, "max", max, "min", min);
                                return false;
                            }
                            contractItem.setAmount(amount);

                        } catch (NumberFormatException e) {
                            Message.NOT_VALID_DOUBLE.format().send(player);
                            return false;
                        }
                        return true;
                    });
                }

                if (event.getClick() == ClickType.LEFT) {
                    InventoryManager.CONFIRMATION.generate(this, () -> contract.whenDecidedByMiddleman(contractItem.amount)).open();
                }
            }

        }


        @Override
        public void whenClosed(InventoryCloseEvent event) {

        }
    }

}
