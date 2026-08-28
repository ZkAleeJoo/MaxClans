package org.zkaleejoo.test;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TestCustomModel {
    public static void test(ItemStack item, int modelData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            org.bukkit.inventory.meta.components.CustomModelDataComponent component = meta
                    .getCustomModelDataComponent();
            component.setFloats(java.util.List.of((float) modelData));
            meta.setCustomModelDataComponent(component);
            item.setItemMeta(meta);
        }
    }
}
