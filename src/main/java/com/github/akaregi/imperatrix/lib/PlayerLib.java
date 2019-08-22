package com.github.akaregi.imperatrix.lib;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Imperatrix で実装されるプレイヤーのプレースホルダ。
 *
 * @author OKOCRAFT
 * @since 1.0.0-SNAPSHOT
 */

public class PlayerLib {

    /**
     * {@code identifier}に指定した文字列を含むLoreを持つアイテムがプレイヤーのインベントリに存在するときtrue
     *
     * @param player     アイテム検証するプレイヤー
     * @param identifier 指定する文字列
     * @return マッチするアイテムがあればtrue、なければfalse
     */

    public static boolean hasItemLorePartialMatch(Player player, String identifier) {
        String str = identifier.substring(25);
        System.out.println(str);
        return Arrays.stream(player.getInventory().getContents())
                .filter(item -> !Objects.isNull(item))
                .map(ItemStack::getItemMeta)
                .filter(Objects::nonNull)
                .filter(ItemMeta::hasLore)
                .map(ItemMeta::getLore)
                .filter(Objects::nonNull).anyMatch(lore -> lore.stream()
                        .filter(loreLine -> !Strings.isNullOrEmpty(loreLine)).anyMatch(loreLine -> loreLine.matches(".*" + str + ".*")));
    }

    /**
     * プレイヤーが要求されたアイテムを持っているか判定する。
     *
     * <p>
     * <code>identifier: hasitem_id:Id,amount:Number,name:Name,lore:L1|L2|L3,enchants:E1;Lv1|E2;Lv2</code>
     * <p>
     * identifier のデリミタは ","
     *
     * @param player     インベントリを参照するプレイヤー
     * @param identifier PAPI の識別子
     * @return 要求を満たしていれば true 、さもなくば false
     * @author LazyGon
     * @see PlayerLib#matchItem(ItemStack, String)
     * @see PlayerLib#matchName(ItemStack, String)
     * @see PlayerLib#matchLore(ItemStack, String)
     * @see PlayerLib#matchEnchants(ItemStack, String)
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

            return Arrays.stream(inventory).filter(Objects::nonNull)
                    .filter(item -> matchItem(item, reqMaterial)).filter(item -> matchName(item, reqName))
                    .filter(item -> matchLore(item, reqLores)).filter(item -> matchEnchants(item, reqEnchants))
                    .mapToInt(ItemStack::getAmount).sum() >= reqAmount;

        } catch (NumberFormatException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * {@code identifier}に指定した文字列を含むLoreを持つアイテムがプレイヤーのインベントリに存在するときtrue
     *
     * @param player     判定するプレイヤー
     * @param identifier PAPI の識別子
     * @return マッチするアイテムがあればtrue、なければfalse
     */

    public static boolean holdItemLorePartialMatch(Player player, String identifier) {
        String str = identifier.substring(26);
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        if (mainHandItem.getType().equals(Material.AIR)) return false;

        ItemMeta mainHandItemMeta = mainHandItem.getItemMeta();

        if (mainHandItemMeta == null || !mainHandItemMeta.hasLore()) return false;

        List<String> lore = mainHandItemMeta.getLore();

        if (lore == null) return false;

        return lore.stream()
                .filter(loreLine -> !Strings.isNullOrEmpty(loreLine))
                .anyMatch(loreLine -> loreLine.matches(".*" + str + ".*"));
    }

    /**
     * おこポイントを取得する
     *
     * @param player 取得するプレイヤー
     * @return 問題なく取得できればその値、 ScoreboardManager が null なら -1, okopoint2 が null なら -2
     * @author Siroshun09
     * @since 1.2.0-SNAPSHOT
     */

    public static int getOkopoint(Player player) {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) return -1;

        Objective okopointObj = sm.getMainScoreboard().getObjective("okopoint2");
        if (okopointObj == null) return -2;

        return okopointObj.getScore(player.getName()).getScore();
    }

    /**
     * アイテムが指定したアイテムか判定する
     *
     * @param item    任意のアイテム
     * @param request アイテム
     * @return 合致すれば true, さもなくば false
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     */
    private static boolean matchItem(ItemStack item, String request) {
        return (request.equals("")) || item.getType().toString().equalsIgnoreCase(request);
    }

    /**
     * アイテムが指定した名前であるか判定する
     *
     * @param item 任意のアイテム
     * @param name 名前
     * @return 合致すれば true, さもなくば false
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     */

    private static boolean matchName(ItemStack item, String name) {
        if (item.getItemMeta() == null) return false;
        return (name.equals("")) || item.getItemMeta().getDisplayName().equals(name);
    }

    /**
     * アイテムに指定した説明文があるか判定する
     *
     * @param item 任意のアイテム
     * @param lore 説明文
     * @return lore の指定がない、または lore がアイテムの説明文と合致すれば true、さもなくば false
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     */

    private static boolean matchLore(ItemStack item, String lore) {
        if (Objects.isNull(lore))
            return true;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;
        if (!itemMeta.hasLore()) return false;

        List<String> reqLores = new ArrayList<>(Arrays.asList(lore.split("\\|", -1)));

        if (itemMeta.getLore() == null) return false;
        return itemMeta.getLore().equals(reqLores);
    }

    /**
     * アイテムに指定したエンチャントがされているか判定する
     *
     * @param item     任意のアイテム
     * @param enchants エンチャント
     * @return enchants の指定がない、または enchants がアイテムのエンチャントと一致すれば true, さもなくば false
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
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
