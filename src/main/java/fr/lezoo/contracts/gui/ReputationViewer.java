package fr.lezoo.contracts.gui;

import fr.lezoo.contracts.gui.objects.EditableInventory;
import fr.lezoo.contracts.gui.objects.GeneratedInventory;
import fr.lezoo.contracts.gui.objects.item.InventoryItem;
import fr.lezoo.contracts.gui.objects.item.Placeholders;
import fr.lezoo.contracts.gui.objects.item.SimpleItem;
import fr.lezoo.contracts.player.PlayerData;
import fr.lezoo.contracts.review.ContractReview;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
        return null;
    }

    public ReputationInventory newInventory(PlayerData playerData) {
        return new ReputationInventory(playerData,this);
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
            if (inv.getReviews().size() >= inv.getPage() * inv.getReviewPerPage() + n)
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
        List<ContractReview> reviews = playerData.getReviews();
        private int page = 0;
        //The getByFunction method of generated inventory will return only if the item has been loaded
        // in it which is not the case here -> editable method
        private final int reviewPerPage =getEditable().getByFunction("review").getSlots().size();
        private final int maxPage = (Math.max(0, playerData.getReviews().size() - 1)) / reviewPerPage;

        public ReputationInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
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
            return str.replace("{player}",player.getDisplayName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("next-page")) {
                page++;
                open();
            }
            if (item.getFunction().equals("previous-page")) {
                page--;
                open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
        //nothing
        }
    }
}
