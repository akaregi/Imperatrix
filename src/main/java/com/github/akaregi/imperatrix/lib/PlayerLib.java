package com.github.akaregi.imperatrix.lib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Imperatrix で実装されるプレイヤーのプレースホルダ。
 *
 * @author OKOCRAFT
 * @since 1.0.0-SNAPSHOT
 */

public class PlayerLib {

    private final static LoadingCache<String, Score> SCORE_CACHE =
            CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build(new ScoreLoader());

    /**
     * {@code identifier}に指定した文字列を含むLoreを持つアイテムがプレイヤーのインベントリに存在するときtrue
     *
     * @param player     アイテム検証するプレイヤー
     * @param identifier 指定する文字列
     * @return マッチするアイテムがあればtrue、なければfalse
     */
    public static boolean hasItemLorePartialMatch(Player player, String identifier) {
        if (player == null || identifier == null || identifier.isEmpty()) {
            return false;
        }

        String str = identifier.substring(25);

        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .map(ItemStack::getItemMeta)
                .filter(Objects::nonNull)
                .filter(ItemMeta::hasLore)
                .map(ItemMeta::getLore)
                .filter(Objects::nonNull)
                .anyMatch(lore -> anyMatchLore(lore, str));
    }

    /**
     * {@code identifier}に指定した文字列を含むLoreを持つアイテムがプレイヤーのインベントリに存在するときtrue
     *
     * @param player     判定するプレイヤー
     * @param identifier PAPI の識別子
     * @return マッチするアイテムがあればtrue、なければfalse
     */
    public static boolean holdItemLorePartialMatch(Player player, String identifier) {
        if (player == null || identifier == null || identifier.isEmpty()) {
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().equals(Material.AIR)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        return anyMatchLore(meta.getLore(), identifier.substring(26));
    }

    private static boolean anyMatchLore(List<String> lore, String str) {
        if (lore == null) {
            return false;
        }

        String regex = ".*" + str + ".*";

        return lore.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.matches(regex));
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
        if (player == null || identifier == null || identifier.isEmpty()) {
            return false;
        }

        Map<String, String> params = Utilities.parseItemIdentifier(identifier);

        int reqAmount;

        try {
            reqAmount = Integer.parseInt(params.getOrDefault("amount", "1"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        String reqMaterial = params.getOrDefault("id", "");
        String reqName = params.getOrDefault("name", "");
        String reqLores = params.getOrDefault("lore", null);
        String reqEnchants = params.getOrDefault("enchants", "");

        int total = Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> matchItem(item, reqMaterial))
                .filter(item -> matchName(item, reqName))
                .filter(item -> matchLore(item, reqLores))
                .filter(item -> matchEnchants(item, reqEnchants))
                .mapToInt(ItemStack::getAmount).sum();

        return reqAmount <= total;
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
        return request.equals("") || item.getType().name().equalsIgnoreCase(request);
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
        return item.getItemMeta() != null && (name.equals("") || item.getItemMeta().getDisplayName().equals(name));
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
        if (Objects.isNull(lore)) {
            return true;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null && itemMeta.getLore() != null) {
            return itemMeta.getLore().equals(Arrays.asList(lore.split("\\|", -1)));
        } else {
            return false;
        }
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
        if (enchants.isEmpty()) {
            return true;
        }

        try {
            return Utilities.parseEnchantmentIdentifier(enchants).entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> Enchantment.getByName(entry.getKey()),
                            entry -> Integer.parseInt(entry.getValue()),
                            (e1, e2) -> e1, HashMap::new))
                    .equals(item.getEnchantments());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * おこポイントを取得する
     *
     * @param player 取得するプレイヤー
     * @return 問題なく取得できればその値、 {@link Score} が取得できなければ -1, それ以外は -2
     * @author Siroshun09
     * @since 1.2.0-SNAPSHOT
     */
    public static int getOkopoint(Player player) {
        if (player == null) {
            return 0;
        }

        Score score;
        try {
            score = SCORE_CACHE.get(player.getName());
        } catch (Throwable e) {
            e.printStackTrace();
            return -2;
        }

        if (score == null) {
            return -1;
        }

        return score.getScore();
    }

    private static class ScoreLoader extends CacheLoader<String, Score> {

        private static Objective OKOPOINT_OBJ = null;

        private static void setOkopointObj() {
            ScoreboardManager sm = Bukkit.getScoreboardManager();

            if (sm == null) {
                return;
            }

            OKOPOINT_OBJ = sm.getMainScoreboard().getObjective("okopoint2");
        }

        @Override
        public Score load(String s) {
            if (OKOPOINT_OBJ == null) {
                setOkopointObj();
            }

            if (OKOPOINT_OBJ == null) {
                return null;
            }

            return OKOPOINT_OBJ.getScore(s);
        }
    }
}
