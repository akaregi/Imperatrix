/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPI expansion. Copyright (C) 2019 akaregi <akg.tachibana@gmail.com>
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.val;

class Utilities {
    /**
     * 数値を丸める。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param value もとの値。
     * @param place 残す小数の位。2 なら 20.00 のようになる。
     *
     * @return Rounded value.
     */
    public static double round(double value, Integer place) {
        return BigDecimal.valueOf(value).setScale(place, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * identifierを分割して引数にする
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param identifier placeholderが受け取る引数
     *
     * @return 連想配列、例: { id: "Id", name: "Name", amount: 10, lore: "L|L|L", enchants: "E|E|E" }
     */
    public static Optional<Map<String, String>> parseIdentifier(String identifier) {
        val params = new HashMap<String, String>();

        String[] pairs = identifier.matches("^.*_+*")
            ? identifier.replaceFirst(".*_", "").split(",")
            : new String[0];

        if (pairs.length == 0)
            return Optional.empty();

        for (val pair : pairs) {
            val keyVal = pair.split(":");
            params.put(keyVal[0], keyVal[1]);
        }

        return Optional.ofNullable(params);
    }
}
