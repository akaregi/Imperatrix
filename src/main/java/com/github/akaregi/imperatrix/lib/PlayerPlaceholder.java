package com.github.akaregi.imperatrix.lib;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.google.common.base.Strings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

public class PlayerPlaceholder {

    /**
     * プレイヤーが要求されたアイテムを持っているか判定する。
     *
     * <p>identifier:
     * hasitem_id:Id,amount:Number,name:Name,lore:L1|L2|L3,enchants:E1;Lv1|E2;Lv2
     * <p>identifier のデリミタは ","
     *
     * @author LazyGon
     *
     * @param player     インベントリを参照するプレイヤー
     * @param identifier PAPI の識別子
     *
     * @return 要求を満たしていれば true 、さもなくば false
     *
     */
    public static boolean hasItem(Player player, String identifier) {
        // expected req: hasitem_id:Id,name:Name,amount:10,lore:L|L|L,enchants:E|E|E
        // expected res: ["id:Id", "amount:10", "name:Name", "lore:L|L|L", "enchants:E|E|E"]
        final Map<String, String> params = Utilities.parseItemIdentifier(identifier);

        try {
            final String   reqMaterial = Strings.nullToEmpty(params.get("id"));
            final String   reqName     = Strings.nullToEmpty(params.get("name"));
            final int      reqAmount   = Integer.parseInt(params.get("amount"));
            final String[] reqLores    = Strings.nullToEmpty(params.get("lore")).split("\\|");
            final String[] reqEnchants = Strings.nullToEmpty(params.get("enchants")).split("\\|");

            final ItemStack[] inventory = player.getInventory().getContents();

            return Arrays.stream(inventory)
                    .filter(item -> item != null)
                    .filter(item -> matchItem(item, reqMaterial))
                    .filter(item -> matchName(item, reqName))
                    .filter(item -> matchLore(item, reqLores))
                    .filter(item -> matchEnchants(item, reqEnchants))
                    .collect(Collectors.toList()).size()
                    >= reqAmount;

        } catch (NumberFormatException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * アイテムが指定したアイテムか判定する
     *
     * @author LazyGon
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
     *
     * @param item 任意のアイテム
     * @param lore 説明文
     *
     * @return lore の指定がない、または lore がアイテムの説明文と合致すれば true、さもなくば false
     */
    private static boolean matchLore(ItemStack item, String[] lore) {
        // 条件が指定されなかった場合
        if (lore[0].equalsIgnoreCase("")) return true;

        List<String> itemLores = item.getItemMeta().getLore();

        // アイテムがloreを持たない場合
        if (itemLores == null) return false;

        // アイテムのloreの最後の行が空白かどうかをチェックしてサイズを調節する
        int itemLoreLines = (itemLores.get(itemLores.size() - 1).equals("")) ? itemLores.size() - 1 : itemLores.size();

        // アイテムのlore数と要求のlore数が違う場合
        if (itemLoreLines != lore.length) return false;

        // それぞれの行を比較
        int currentLine = 0;

        for (String reqloreline : lore) {
            if (itemLores.get(currentLine).equals(reqloreline)) {
                currentLine++;
                continue;
            }
            break;
        }

        return (currentLine == lore.length);
    }

    /**
     * アイテムに指定したエンチャントがされているか判定する
     *
     * @author LazyGon
     *
     * @param item     任意のアイテム
     * @param enchants エンチャント
     *
     * @return enchants の指定がない、または enchants がアイテムのエンチャントと一致すれば true, さもなくば false
     */
    @SuppressWarnings("deprecation")
    private static boolean matchEnchants(ItemStack item, String[] enchants) {
        // 条件が指定されなかった場合
        if (enchants[0].equalsIgnoreCase("")) return true;

        // 条件のエンチャントとそのレベルをインデックスで対応させた配列2つを用意
        Integer[] reqEnchantsLevel = new Integer[enchants.length];
        String[] reqEnchantsName = new String[enchants.length];
        try {
            for (int i = 0; i < enchants.length; i++) {
                reqEnchantsLevel[i] = Integer.parseInt(enchants[i].replaceAll(".*;", ""));
                reqEnchantsName[i] = enchants[i].replaceAll(";.*", "");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        // ItemStackのエンチャントとレベルのマップを取得
        Map<Enchantment, Integer> realEnchantsMap = item.getEnchantments();
        // エンチャントがマッチした回数
        int matchenchant = 0;

        // 要求されたエンチャントとItemStackについたエンチャントの全てを比較する
        // それを、要求されたエンチャントの種類(配列数分)だけ繰り返す
        for (int i = 0; i < enchants.length; i++) {
            for (Map.Entry<Enchantment, Integer> checkEnchant : realEnchantsMap.entrySet()) {
                if ((checkEnchant.getKey().getName().equals(reqEnchantsName[i])
                        || checkEnchant.getKey().getKey().getKey().equals(reqEnchantsName[i]))
                        && reqEnchantsLevel[i] == checkEnchant.getValue()) {
                    // エンチャントがマッチした時1増加
                    matchenchant++;
                    // マッチしたのでiの時の要求エンチャントをマッチさせる処理を終わり、i+1へ移行
                    break;
                }
            }
        }
        // 要求エンチャントの数とエンチャントがマッチした回数が一致した時trueを返す
        return (matchenchant == enchants.length);
    }
}
