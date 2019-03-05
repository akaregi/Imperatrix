package com.github.akaregi.imperatrix.lib;

import java.util.Map;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

public class PlayerPlaceholder {

    /**
     * プレイヤーのインベントリ内のすべてのアイテムを調べ、条件に合致したアイテムの合計数が指定数以上かどうかを判定する
     * identifier フォーマット: hasitem_id:MATERIAL,amount:number,name:itemname,lore:lore1|lore2|lore3...,enchant:enchant1;level|enchant2;level|...
     * identifierのデリミタはカンマ(,)を使用
     * 
     * @author LazyGon
     * 
     * @param player
     * @param identifier
     * 
     * @return boolean
     * 
     */
    public static boolean hasItem(Player player, String identifier) {

        // identifierの先頭のhasitem_を削って、その文字列をカンマで分割する
        String[] args = Utilities.parseIdentifier(identifier).orElse(new String[0]);

        String reqMaterial = "";
        int reqAmount = 1;
        String reqName = "";
        String[] reqLores = new String[0];
        String[] reqEnchants = new String[0];
        ItemStack[] inventory = player.getInventory().getContents();
        int realAmount = 0;

        // argsの各要素の接頭辞ごとに何を表す引数か判定し、変数に代入する
        // 接頭辞がなく、取得できない引数があった場合は検索処理を飛ばす
        try {
            for (String arg : args) {
                if(args.length == 0) break;
                if (arg.startsWith("id:"))
                    reqMaterial = arg.substring(3);
                if (arg.startsWith("amount:"))
                    reqAmount = Integer.parseInt(arg.substring(7));
                if (arg.startsWith("name:"))
                    reqName = arg.substring(5);
                if (arg.startsWith("lore:"))
                    reqLores = arg.substring(5).split("\\|");
                if (arg.startsWith("enchant:"))
                    reqEnchants = arg.substring(8).split("\\|");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        // それぞれのスロットから条件が一致するアイテムを検索して、その数を合計する
        for (ItemStack item : inventory) {

            if (item == null)
                continue;
            if (reqMaterial != "" && !isRequiredMaterial(item, reqMaterial))
                continue;
            if (reqName != "" && !hasRequiredName(item, reqName))
                continue;
            if (reqLores.length != 0 && !matchLore(item, reqLores))
                continue;
            if (reqEnchants.length != 0 && !matchEnchantment(item, reqEnchants))
                continue;

            realAmount += item.getAmount();
        }
        // 合計数が要求された数以上ならばtrue、そうでなければfalse
        return (realAmount >= reqAmount);
    }
    /**
     * 指定したアイテムかどうかを調べる
     * 
     * @author LazyGon
     * 
     * @param item
     * @param material
     * 
     * @return boolean
     */
    private static boolean isRequiredMaterial(ItemStack item, String material) {
        return item.getType().toString().equalsIgnoreCase(material);
    }

    /**
     * アイテムが指定した名前かどうかを調べる
     * 
     * @author LazyGon
     * 
     * @param item
     * @param name
     * 
     * @return boolean
     */
    private static boolean hasRequiredName(ItemStack item, String name) {
        return (item.getItemMeta().getDisplayName().equals(name));
    }

    /**
     * アイテムが指定したloreを持つかどうかを調べる
     * 
     * @author LazyGon
     * 
     * @param item
     * @param lore
     * 
     * @return boolean
     */
    private static boolean matchLore(ItemStack item, String[] lore) {

        List<String> itemLores = item.getItemMeta().getLore();

        // アイテムがloreを持たない場合
        if (itemLores == null)
            return false;

        // アイテムのloreの最後の行が空白かどうかをチェックしてサイズを調節する
        int itemLoreLines = (itemLores.get(itemLores.size() - 1).equals("")) ? itemLores.size() - 1 : itemLores.size();

        // アイテムのlore数と要求のlore数が違う場合
        if (itemLoreLines != lore.length)
            return false;

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
     * アイテムが指定したエンチャントを持つかどうかを調べる
     * 
     * @author LazyGon
     * 
     * @param item
     * @param reqEnchants
     * 
     * @return boolean
     */
    @SuppressWarnings("deprecation")
    private static boolean matchEnchantment(ItemStack item, String[] reqEnchants) {

            // 条件のエンチャントとそのレベルをインデックスで対応させた配列2つを用意
            Integer[] reqEnchantsLevel = new Integer[reqEnchants.length];
            String[] reqEnchantsName = new String[reqEnchants.length];
        try{
            for (int i = 0; i < reqEnchants.length; i++) {
                reqEnchantsLevel[i] = Integer.parseInt(reqEnchants[i].replaceAll(".*;", ""));
                reqEnchantsName[i] = reqEnchants[i].replaceAll(";.*", "");
            }
        }catch(NumberFormatException e){
            e.printStackTrace();
            return false;
        }

            // ItemStackのエンチャントとレベルのマップを取得
            Map<Enchantment, Integer> realEnchantsMap = item.getEnchantments();
            // エンチャントがマッチした回数
            int matchenchant = 0;

            // 要求されたエンチャントとItemStackについたエンチャントの全てを比較する
            // それを、要求されたエンチャントの種類(配列数分)だけ繰り返す
            for (int i = 0; i < reqEnchants.length; i++) {
                for (Map.Entry<Enchantment, Integer> checkEnchant : realEnchantsMap.entrySet()) {
                    if ((checkEnchant.getKey().getName().equals(reqEnchantsName[i])
                        || checkEnchant.getKey().getKey().getKey().equals(reqEnchantsName[i]))
                        || ("minecraft:" + checkEnchant.getKey().getKey().getKey()).equals(reqEnchantsName[i])
                        && reqEnchantsLevel[i] == checkEnchant.getValue())
                        {
                        // エンチャントがマッチした時1増加
                        matchenchant++;
                        // マッチしたのでiの時の要求エンチャントをマッチさせる処理を終わり、i+1へ移行
                        break;
                    }
                }
            }
            // 要求エンチャントの数とエンチャントがマッチした回数が一致した時trueを返す
            return (matchenchant == reqEnchants.length);

    }
}