package com.github.akaregi.imperatrix.lib;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Imperatrix で実装されるプレイヤーのプレースホルダ。
 *
 * @since 1.0.0-SNAPSHOT
 * @author OKOCRAFT
 */
public class PlayerLib {

    /**
     * {@code identifier}に指定した文字列を含むLoreを持つアイテムがプレイヤーのインベントリに存在するときtrue
     * 
     * @param player
     * @param identifier
     * @return マッチするアイテムがあればtrue、なければfalse
     */
    public static boolean hasItemLorePartialMatch(Player player, String identifier){
        String str = identifier.substring(25);
        System.out.println(str);
        return Arrays.stream(player.getInventory().getContents())
                .filter(item -> !Objects.isNull(item))
                .map(ItemStack::getItemMeta)
                .filter(ItemMeta::hasLore)
                .map(ItemMeta::getLore)
                .filter(lore -> lore.stream()
                        .filter(loreLine -> !Strings.isNullOrEmpty(loreLine))
                        .filter(loreLine -> loreLine.matches(".*" + str + ".*"))
                        .count() > 0)
                .count() > 0;
    }

    /**
     * プレイヤーが要求されたアイテムを持っているか判定する。
     *
     * <p>
     * <code>identifier: hasitem_id:Id,amount:Number,name:Name,lore:L1|L2|L3,enchants:E1;Lv1|E2;Lv2</code>
     * <p>
     * identifier のデリミタは ","
     *
     * @author LazyGon
     *
     * @param player     インベントリを参照するプレイヤー
     * @param identifier PAPI の識別子
     *
     * @see PlayerLib#matchItem(ItemStack, String)
     * @see PlayerLib#matchName(ItemStack, String)
     * @see PlayerLib#matchLore(ItemStack, String)
     * @see PlayerLib#matchEnchants(ItemStack, String)
     *
     * @return 要求を満たしていれば true 、さもなくば false
     *
     */
    public static boolean hasItem(Player player, String identifier) {
        // expected req: hasitem_id:Id,name:Name,amount:10,lore:L|L|L,enchants:E|E|E
        // expected res: ["id:Id", "amount:10", "name:Name", "lore:L|L|L",
        // "enchants:E|E|E"]
        final Map<String, String> params = Utilities.parseItemIdentifier(identifier);

        try {
            final String reqMaterial = params.getOrDefault("id", "");
            final String reqName = params.getOrDefault("name", "");
            final int reqAmount = Integer.parseInt(params.getOrDefault("amount", "1"));
            final String reqLores = params.getOrDefault("lore", null);
            final String reqEnchants = params.getOrDefault("enchants", "");

            final ItemStack[] inventory = player.getInventory().getContents();

            return Arrays.stream(inventory).filter(item -> Objects.nonNull(item))
                    .filter(item -> matchItem(item, reqMaterial)).filter(item -> matchName(item, reqName))
                    .filter(item -> matchLore(item, reqLores)).filter(item -> matchEnchants(item, reqEnchants))
                    .mapToInt(item -> item.getAmount()).sum() >= reqAmount;

        } catch (NumberFormatException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * アイテムが指定したアイテムか判定する
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param item    任意のアイテム
     * @param request アイテム
     *
     * @return 合致すれば true, さもなくば false
     */
    private static boolean matchItem(ItemStack item, String request) {
        return (request == "") ? true : item.getType().toString().equalsIgnoreCase(request);
    }

    /**
     * アイテムが指定した名前であるか判定する
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param item 任意のアイテム
     * @param name 名前
     *
     * @return 合致すれば true, さもなくば false
     */
    private static boolean matchName(ItemStack item, String name) {
        return (name == "") ? true : item.getItemMeta().getDisplayName().equals(name);
    }

    /**
     * アイテムに指定した説明文があるか判定する
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param item 任意のアイテム
     * @param lore 説明文
     *
     * @return lore の指定がない、または lore がアイテムの説明文と合致すれば true、さもなくば false
     */
    private static boolean matchLore(ItemStack item, String lore) {
        if (Objects.isNull(lore))
            return true;

        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasLore()) return false;

        List<String> reqLores = new ArrayList<>(Arrays.asList(lore.split("\\|", -1)));

        return itemMeta.getLore().equals(reqLores);
    }

    /**
     * アイテムに指定したエンチャントがされているか判定する
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param item     任意のアイテム
     * @param enchants エンチャント
     *
     * @return enchants の指定がない、または enchants がアイテムのエンチャントと一致すれば true, さもなくば false
     */
    @SuppressWarnings("deprecation")
    private static boolean matchEnchants(ItemStack item, String enchants) {
        if (Strings.isNullOrEmpty(enchants))
            return true;
        try {
            return Splitter.on("\\|").trimResults().withKeyValueSeparator(";").split(enchants).entrySet().stream()
                    .collect(Collectors.toMap(entry -> Enchantment.getByName(entry.getKey()),
                            entry -> Integer.parseInt(entry.getValue()), (e1, e2) -> e1, HashMap::new))
                    .equals(item.getEnchantments());
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
