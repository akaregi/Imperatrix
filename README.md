# Imperatrix

Imperatrix, a PlaceholderAPI expansion.

## 依存

* JRE / JDK 8 以降
* PlaceholderAPI

## 導入

Imperatrix を `plugins/PlaceholderAPI/expansions` に配置し `/papi register <JAR's full name>` を実行する。

## ライセンス

GNU General Public License v3.0

## プレースホルダ

### `%imperatrix_tps%`

サーバーの TPS(_Tick Per Second_)を取得する。

### `%imperatrix_hasItem_<NBT expr>`

プレイヤーが `<NBT expr>` というアイテムを持っているか判定する。

`<NBT expr>` は `id:Id,amount:Number,name:Name,lore:L1|L2|L3,enchants:E1;Lv1|E2;Lv2` という形式である。
