package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.Contracts;
import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import fr.lezoo.contracts.gui.objects.item.Placeholders;
import fr.lezoo.contracts.gui.objects.item.SimpleItem;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import fr.lezoo.contracts.utils.ContractsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

public class ReputationViewer extends EditableInventory {


    public ReputationViewer() {
        super("reputation");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equals("next-page"))
            return new NextPageItem(config);
        if (function.equals("previous-page"))
            return new PreviousPageItem(config);
        if (function.equals("review"))
            return new ReviewItem(config);
        if(function.equals("go-back"))
            return new GoBackItem(config);
        return null;
    }

    /**
     * Used when a player tries to check his reputation
     */
    public ReputationInventory newInventory(PlayerData playerData) {
        return new ReputationInventory(playerData,playerData,this,null);
    }
    public ReputationInventory newInventory(PlayerData playerData,PlayerData reputationPlayer) {
        return new ReputationInventory(playerData,reputationPlayer,this,null);
    }
    public ReputationInventory newInventory(PlayerData playerData,PlayerData reputationPlayer,GeneratedInventory invToOpen) {
        return new ReputationInventory(playerData,reputationPlayer,this,invToOpen);
    }



    public class GoBackItem extends SimpleItem<ReputationInventory> {

        public GoBackItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(ReputationInventory inv) {
            return inv.invToOpen!=null;
        }
    }


    public class ReviewItem extends InventoryItem<ReputationInventory> {


        public ReviewItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ReputationInventory inv, int n) {
            if (inv.getReviews().size() <= inv.getPage() * inv.getReviewPerPage() + n)
                return new ItemStack(Material.AIR);

            return super.getDisplayedItem(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(ReputationInventory inv, int n) {
            ContractReview review = inv.getReviews().get(inv.getPage() * inv.getReviewPerPage() + n);
            Placeholders holders = new Placeholders();
            holders.register("reviewer", Bukkit.getOfflinePlayer(review.getReviewer()).getName());
            holders.register("contract-state", review.getContract().getState().toString().toLowerCase().
                    replace("_", " "));
            holders.register("notation", review.getNotation());
            holders.register("comment", review.getComment());
            return holders;
        }
    }

    public class PreviousPageItem extends SimpleItem<ReputationInventory> {

        public PreviousPageItem(ConfigurationSection config) {
            super(config);
            Bukkit.broadcastMessage(getMaterial().toString());
        }

        public boolean isDisplayed(ReputationInventory inv) {
            return inv.getPage() > 0;
        }

    }

    public class NextPageItem extends SimpleItem<ReputationInventory> {

        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        public boolean isDisplayed(ReputationInventory inv) {
            return inv.getPage() < inv.getMaxPage();
        }
    }

    public class ReputationInventory extends GeneratedInventory {
        private final GeneratedInventory invToOpen;
        private final PlayerData reputationPlayer;
        private final List<ContractReview> reviews;
        private int page = 0;
        //The getByFunction method of generated inventory will return only if the item has been loaded
        // in it which is not the case here -> editable method
        private final int reviewPerPage =getEditable().getByFunction("review").getSlots().size();
        private final int maxPage = (Math.max(0, playerData.getReviews().size() - 1)) / reviewPerPage;

        public ReputationInventory(PlayerData playerData,PlayerData reputationPlayer, EditableInventory editable,GeneratedInventory invToOpen) {
            super(playerData, editable);
            this.reputationPlayer=reputationPlayer;
            reviews= reputationPlayer.getReviews();
            this.invToOpen=invToOpen;
        }


        public List<ContractReview> getReviews() {
            return reviews;
        }

        public int getReviewPerPage() {
            return reviewPerPage;
        }

        public int getPage() {
            return page;
        }

        public int getMaxPage() {
            return maxPage;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return ContractsUtils.applyColorCode(str.replace("{player}",player.getDisplayName()));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            event.setCancelled(true);
            if (item.getFunction().equals("next-page")) {
                page++;
                open();
            }
            if (item.getFunction().equals("previous-page")) {
                page--;
                open();
            }
            if(item instanceof GoBackItem) {
                invToOpen.open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
        //nothing
        }
    }
}
