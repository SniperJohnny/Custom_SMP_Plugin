package io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.gui_pvp;

import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.menu_Pvp_important.SimpleMenu_pvp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Revive_beacon_recipe_shower extends SimpleMenu_pvp {

    public Revive_beacon_recipe_shower() {
        super(Rows.FIVE, "This is the recipe for the items you need to revive someone");
    }

    @Override
    public void onSetItems() {
        final ItemStack revive_beacon = new ItemStack(Material.BEACON);
        final ItemMeta revive_beacon_meta = revive_beacon.getItemMeta();
        revive_beacon_meta.displayName(Component.text("Right Click in the air to revive an teammate of yours or add yourself an Live"
                , NamedTextColor.GOLD));
        revive_beacon_meta.addEnchant(Enchantment.BINDING_CURSE, 255, true);
        revive_beacon.setItemMeta(revive_beacon_meta);

        final ItemStack gray_outline = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        final ItemMeta grayoutlineMeta = gray_outline.getItemMeta();
        grayoutlineMeta.displayName(Component.text(" "));
        gray_outline.setItemMeta(grayoutlineMeta);

        final ItemStack resultArrow = new ItemStack(Material.ARROW);
        final ItemMeta resultArrowMeta = resultArrow.getItemMeta();
        resultArrowMeta.displayName(Component.text(" "));
        resultArrow.setItemMeta(resultArrowMeta);



        //Gray Outline
        setItem(0, gray_outline);
        setItem(1, gray_outline);
        setItem(2, gray_outline);
        setItem(3, gray_outline);
        setItem(4, gray_outline);
        setItem(5, gray_outline);
        setItem(6, gray_outline);
        setItem(7, gray_outline);
        setItem(8, gray_outline);
        setItem(9, gray_outline);
        setItem(10, new ItemStack(Material.NETHERITE_BLOCK));
        setItem(11, new ItemStack(Material.TOTEM_OF_UNDYING));
        setItem(12, new ItemStack(Material.DIAMOND_BLOCK));
        setItem(13, gray_outline);
        setItem(14, gray_outline);
        setItem(15, gray_outline);
        setItem(16, gray_outline);
        setItem(17, gray_outline);
        setItem(18, gray_outline);
        setItem(19, new ItemStack(Material.TOTEM_OF_UNDYING));
        setItem(20, new ItemStack(Material.BEACON));
        setItem(21, new ItemStack(Material.TOTEM_OF_UNDYING));
        setItem(22, gray_outline);
        setItem(23, resultArrow);
        setItem(24, gray_outline);
        setItem(25, revive_beacon);
        setItem(26, gray_outline);
        setItem(27, gray_outline);
        setItem(28, new ItemStack(Material.DIAMOND_BLOCK));
        setItem(29, new ItemStack(Material.TOTEM_OF_UNDYING));
        setItem(30, new ItemStack(Material.NETHERITE_BLOCK));
        setItem(31, gray_outline);
        setItem(32, gray_outline);
        setItem(33, gray_outline);
        setItem(34, gray_outline);
        setItem(35, gray_outline);
        setItem(36, gray_outline);
        setItem(37, gray_outline);
        setItem(38, gray_outline);
        setItem(39, gray_outline);
        setItem(40, gray_outline);
        setItem(41, gray_outline);
        setItem(42, gray_outline);
        setItem(43, gray_outline);
        setItem(44, gray_outline);
    }
}
