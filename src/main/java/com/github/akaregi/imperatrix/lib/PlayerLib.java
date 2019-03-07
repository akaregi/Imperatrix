/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPI expansion.
 * Copyright (C) 2019 akaregi <akg.tachibana@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package com.github.akaregi.imperatrix.lib;

import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import lombok.val;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Imperatrix で実装されるプレイヤーのプレースホルダ。
 *
 * @since  1.0.0-SNAPSHOT
 * @author OKOCRAFT
 */
public class PlayerLib {

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
            final String reqName     = params.getOrDefault("name", "");
            final int    reqAmount   = Integer.parseInt(params.getOrDefault("amount", "1"));
            final String reqLores    = params.getOrDefault("lore", null);
            final String reqEnchants = params.getOrDefault("enchants", "");

            final ItemStack[] inventory = player.getInventory().getContents();

            return Arrays.stream(inventory).filter(item -> Objects.nonNull(item))
                    .filter(item -> matchItem(item, reqMaterial))
                    .filter(item -> matchName(item, reqName))
                    .filter(item -> matchLore(item, reqLores))
                    .filter(item -> matchEnchants(item, reqEnchants))
                    .mapToInt(item -> item.getAmount())
                    .sum() >= reqAmount;

        } catch (NumberFormatException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * アイテムが指定したアイテムか判定する
     *
     * @author LazyGon
     * @since  1.0.0-SNAPSHOT
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
        if (Objects.isNull(lore)) return true;

        List<String> reqLores = Arrays.asList(lore.split("\\|", -1));

        val itemLores = item.getItemMeta().getLore();
        if (Objects.isNull(itemLores)) return false;

        if (itemLores.size() != reqLores.size()) return false;

        return reqLores.stream().filter( line ->
            itemLores.get(reqLores.indexOf(line)).equals(line)
        ).collect(Collectors.toList()).size() == reqLores.size();
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

        val requestEnchants =
                Splitter.on("\\|").trimResults().withKeyValueSeparator(";").split(enchants);

        return item.getEnchantments().entrySet().stream().filter(enchant -> {

            val enchantName = enchant.getKey().getName();
            val enchantLevel = enchant.getValue();

            if (requestEnchants.containsKey(enchantName))
                return Integer.parseInt(requestEnchants.get(enchantName)) == enchantLevel;

            // ns = Namespaced
            val nsEnchantName = enchant.getKey().getKey().getKey();

            if (requestEnchants.containsKey(nsEnchantName))
                return Integer.parseInt(requestEnchants.get(nsEnchantName)) == enchantLevel;

            return false;

        }).collect(Collectors.toSet()).size() == requestEnchants.entrySet().size();
    }
}
