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

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class ServerLib {

    private final static Object SERVER;
    private final static Field TPS_FIELD;

    static {
        try {
            String serverVer = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            SERVER = Class.forName("net.minecraft.server." + serverVer + ".MinecraftServer").getMethod("getServer").invoke(null);
            TPS_FIELD = SERVER.getClass().getField("recentTps");
        } catch (Throwable e) {
            throw new IllegalStateException("Could not get server or recentTps", e);
        }
    }

    /**
     * サーバーから生の値ではない TPS を取得する。小数点第三位以下は削られる。
     *
     * @return TPS 配列、ただし各値の最大値は 20 。例: [double, double, double]
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     */
    public static double[] getRationalTPS() throws IllegalAccessException {
        double[] recentTps = (double[]) TPS_FIELD.get(SERVER);

        return Arrays.stream(recentTps)
                .map(ServerLib::round)
                .map(tps -> Math.min(tps, 20.00))
                .toArray();
    }

    /**
     * 数値を丸める。
     *
     * @param value もとの値。
     * @return 小数の位を削られた数値。
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     */
    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
