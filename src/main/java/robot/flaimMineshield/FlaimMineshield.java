package robot.flaimMineshield;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
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

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) return;

        Recipe recipe = event.getRecipe();
        if (recipe != null && recipe.getResult().getType() == Material.IRON_AXE) {
            ItemStack itemStack = new ItemStack(Material.BARRIER);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Запрещено", Style.style(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
                itemStack.setItemMeta(meta);
            }
            event.getInventory().setResult(itemStack);

        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getRecipe().getResult().getType() == Material.IRON_AXE) event.setCancelled(true);
    }

}