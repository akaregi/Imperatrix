# Imperatrix

Imperatrix, a PlaceholderAPI expansion.

## 依存

* JRE / JDK 8 以降
* Spigot
* PlaceholderAPI

## 導入

Imperatrix を `plugins/PlaceholderAPI/expansions` に配置し `/papi register <JAR's full name>` を実行する。

## ライセンス

GNU General Public License v3.0

## プレースホルダ

### `%imperatrix_tps%`

サーバーの TPS(_Tick Per Second_)を取得する。

### `%imperatrix_hasItem_<NBT expr>%`

プレイヤーが `<NBT expr>` というアイテムを持っているか判定する。

`<NBT expr>` は `id:Id,amount:Number,name:Name,lore:L1|L2|L3,enchants:E1;Lv1|E2;Lv2` という形式である。

### `%imperatrix_hasitem_lorepartialmatch_<String>%`

プレイヤーのインベントリ内に指定した文字列が説明文 (lore) に含まれているアイテムを持っているか判定する。

### `%imperatrix_holditem_lorepartialmatch_<String>%`

プレイヤーが持っているアイテムの説明文 (lore) に指定した文字列が含まれているか判定する。

### `%imperatrix_okopoint%`

プレイヤーが持っているおこポイントを返す。

これの使用には、スコア `okopoint2` が作成されている必要があり、未作成などで値を取得できない場合は -1 を、他の要因で取得できない場合は -2 を返す。
