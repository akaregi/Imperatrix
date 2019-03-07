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

import java.util.Arrays;

public class ServerTPS {
    /**
     * サーバーから生の値ではない TPS を取得する。小数点第三位以下は削られる。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param server サーバーインスタンス。net.minecraft.server を要求する。
     *
     * @return TPS 配列、ただし各値の最大値は 20 。例: [double, double, double]
     */
    public static double[] getRationalTPS(Object server) throws IllegalAccessException, NoSuchFieldException {
        return Arrays.stream(getTPS(server)).map(it ->
            Math.min(Utilities.round(it, 2), 20)
        ).toArray();
    }

    /**
     * サーバーインスタンスから TPS を取得する。値は 20 以上になる可能性がある。
     * 最大値 20 を望むなら {@link ServerTPS#getRationalTPS(Object) } を使う。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @see ServerTPS#getRationalTPS(Object)
     *
     * @param server サーバーインスタンス。net.minecraft.server を要求する。
     *
     * @return TPS 配列。例: [double, double, double]
     */
    public static double[] getTPS(Object server) throws IllegalAccessException, NoSuchFieldException {
        return (double[]) server.getClass().getField("recentTps").get(server);
    }
}
