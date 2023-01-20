package cf.grcq.priveapi.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Button {

    abstract public String getName(Player player);

    abstract public List<String> getLore(Player player);

    abstract public Material getMaterial(Player player);

    abstract public boolean cancelClick(Player player);

    public byte getDamageValue(Player player) {
        return 0;
    }

    public int getAmount(Player player) {
        return 1;
    }

    public void onClick(Player player, ClickType clickType, int slot) {}

    public ItemMeta getItemMeta(Player player) {
        return new ItemStack(getMaterial(player)).getItemMeta();
    }

    public ItemFlag[] addItemFlags(Player player) {
        return new ItemFlag[0];
    }

    public Map<Enchantment, Integer> getEnchantments(Player player) {
        return new HashMap<>();
    }

    public ItemStack createItem(Player player) {
        ItemStack itemStack = new ItemStack(getMaterial(player), getAmount(player), getDamageValue(player));

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(getName(player));
        meta.setLore(getLore(player));
        meta.addItemFlags(addItemFlags(player));
        for (Map.Entry<Enchantment, Integer> entry : getEnchantments(player).entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            meta.addEnchant(enchantment, level, (enchantment.getMaxLevel() > level));
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }

}
