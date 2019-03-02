package com.github.akaregi.imperatrix.lib;

import java.util.Map;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

public class PlayerPlaceholder{

    public static boolean hasItem(Player player, String identifier) {

        // identifier フォーマット
        // hasitem_id:MATERIAL,amount:number,name:itemname,lore:lore1|lore2|lore3...,enchant:enchant1;level|enchant2;level|...
        // エンチャントの名前はBukkitの名前でもminecraftの名前でも可

        // hasitem_~をパーサに掛けてargsに分割
        String[] args = Utilities.getArgs(identifier);

        // 変数を宣言する
        String reqMaterial = null;
        int reqAmount = 1;
        String reqName = null;
        String[] reqLores = null;
        String[] reqEnchants = null;

        // argsの各要素の接頭辞ごとに何を表す引数か判定し、変数に代入する
        try {
            for (String variable : args) {
                if (variable.startsWith("id:"))
                    reqMaterial = variable.substring(3);
                if (variable.startsWith("amount:"))
                    reqAmount = Integer.parseInt(variable.substring(7));
                if (variable.startsWith("name:"))
                    reqName = variable.substring(5);
                if (variable.startsWith("lore:"))
                    reqLores = variable.substring(5).split("\\|");
                if (variable.startsWith("enchant:"))
                    reqEnchants = variable.substring(8).split("\\|");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        // プレイヤーのインベントリーを取得する
        ItemStack[] inventory = player.getInventory().getContents();

        // インベントリの中で見つかったアイテムの数
        int realAmount = 0;

        // それぞれのスロットから条件が一致するアイテムを探して、その数を合計する
        for (ItemStack item : inventory) {

            if (item == null)
                continue;
            if (reqMaterial != null && !equalsMaterial(item, reqMaterial))
                continue;
            if (reqName != null && !equalsName(item, reqName))
                continue;
            if (reqLores != null && !matchLore(item, reqLores, player))
                continue;
            if (reqEnchants != null && !matchEnchantment(item, reqEnchants, player))
                continue;

            realAmount += item.getAmount();
        }
        // 合計数が要求された数以上ならばtrue、そうでなければfalse
        return (realAmount >= reqAmount);
    }

    private static boolean equalsMaterial(ItemStack item, String material) {
        return item.getType().toString().equalsIgnoreCase(material);
    }

    private static boolean equalsName(ItemStack item, String name) {
        return (item.getItemMeta().getDisplayName().equals(name));
    }

    private static boolean matchLore(ItemStack item, String[] lore, Player player) {

        List<String> itemlores = item.getItemMeta().getLore();

        // アイテムがloreを持たない場合
        if (itemlores == null)
            return false;

        // アイテムのloreの最後の行が空白かどうかをチェックしてサイズを調節する
        int itemloresize = 0;
        itemloresize = (itemlores.get(itemlores.size() - 1).equals("")) ? itemlores.size() - 1 : itemlores.size();

        // アイテムのlore数と要求のlore数が違う場合
        if (itemloresize != lore.length)
            return false;
    
        // それぞれの行を比較
        int currentLine = 0;

        for (String reqloreline : lore){
            if(itemlores.get(currentLine).equals(reqloreline)){
                currentLine++;
                continue;
            }
            break;
        }

        return (currentLine == lore.length);
    }

    @SuppressWarnings("deprecation")
    private static boolean matchEnchantment(ItemStack item, String[] reqEnchants, Player player) {
        try {

            // 条件のエンチャントとそのレベルをインデックスで対応させた配列2つを用意
            Integer[] reqEnchantsLevel = new Integer[reqEnchants.length];
            String[] reqEnchantsName = new String[reqEnchants.length];

            for (int i = 0; i < reqEnchants.length; i++) {
                reqEnchantsLevel[i] = Integer.parseInt(reqEnchants[i].replaceAll(".*;", ""));
                reqEnchantsName[i] = reqEnchants[i].replaceAll(";.*", "");
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
                            && reqEnchantsLevel[i] == checkEnchant.getValue()) {
                        // エンチャントがマッチした時1増加
                        matchenchant++;
                        // マッチしたのでiの時の要求エンチャントをマッチさせる処理を終わり、i+1へ移行
                        break;
                    }
                }
            }
            // 要求エンチャントの数とエンチャントがマッチした回数が一致した時trueを返す
            return (matchenchant == reqEnchants.length);

        } catch (Exception e) {
            return false;
        }
    }
}