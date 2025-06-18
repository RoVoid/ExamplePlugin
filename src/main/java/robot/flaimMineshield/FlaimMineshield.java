package robot.flaimMineshield;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlaimMineshield extends JavaPlugin implements Listener {
    static JavaPlugin Plugin;

    static BarrierManager BarrierManager;

    @Override
    public void onEnable() {
        Plugin = this;

        getServer().getPluginManager().registerEvents(this, this);
        BarrierManager = new BarrierManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) return;

        var recipe = event.getRecipe();
        if (recipe != null && recipe.getResult().getType() == Material.IRON_AXE) {
            event.getInventory().setResult(new ItemStack(Material.BARRIER));
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getRecipe().getResult().getType() == Material.IRON_AXE) event.setCancelled(true);
    }

}