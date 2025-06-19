package robot.flaimMineshield;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlaimMineshield extends JavaPlugin implements Listener {
    static JavaPlugin Plugin;

    @Override
    public void onEnable() {
        Plugin = this;
        new BarrierManager();

//        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        BarrierManager.onDisable();
    }

    //    @EventHandler
//    public void onPrepare(PrepareItemCraftEvent event) {
//        if (!(event.getView().getPlayer() instanceof Player)) return;
//
//        Recipe recipe = event.getRecipe();
//        if (recipe != null && recipe.getResult().getType() == Material.IRON_AXE) {
//            ItemStack itemStack = new ItemStack(Material.BARRIER);
//            ItemMeta meta = itemStack.getItemMeta();
//            if (meta != null) {
//                meta.displayName(Component.text("Запрещено", Style.style(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
//                itemStack.setItemMeta(meta);
//            }
//            event.getInventory().setResult(itemStack);
//
//        }
//    }
//
//    @EventHandler
//    public void onCraft(CraftItemEvent event) {
//        if (!(event.getWhoClicked() instanceof Player)) return;
//        if (event.getRecipe().getResult().getType() == Material.IRON_AXE) event.setCancelled(true);
//    }

}