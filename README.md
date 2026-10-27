# open-agriculture-diary

オープンソース農業日誌管理ツール

## 地図で農場を管理

ログイン後のダッシュボードでは、登録した圃場をすべて地図に表示し、作物数・圃場数・作業記録数と最近の作業を確認できます。圃場一覧と作業履歴では、検索結果の**現在のページ**に対応する圃場を表示します。地図上の圃場から編集画面を開けます。

圃場の登録・編集では「範囲を選択」を押し、対角となる2点をクリックして長方形の境界を指定します。同じボタンで選択を取り消せます。座標の手入力と地図は連動し、保存はフォームの保存ボタンで行います。緯度はWeb Mercator対応範囲（±85.051129度）、経度は±180度以内で、北東が南西より北・東になるよう指定してください。日付変更線をまたぐ圃場には対応していません。

地図には **MapLibre GL JS 5.6.1** を使用しています。背景地図は国土地理院の標準・淡色・航空写真とOpenStreetMapの4種類から切り替えられます（APIキー不要）。地図の出典は右下に表示されます。外部CDN（unpkg.com）とタイル配信元へのネットワーク接続およびWebGL対応ブラウザーが必要です。利用時には[地理院タイルの利用規約](https://maps.gsi.go.jp/development/ichiran.html)・[OpenStreetMapのタイル利用ポリシー](https://operations.osmfoundation.org/policies/tiles/)を確認してください。大規模利用時は契約タイルサービス等への差し替えを検討してください。外部地図を読み込めない場合も座標入力は利用できます。

画面構成は[アグリノート](https://www.agri-note.jp/)や[farmOS](https://farmos.org/)に見られる「圃場を地図で把握し、記録につなげる」考え方を参考に、独自のUIで実装しています。

地図の座標処理とタイル設定のテストは `node --test test/farm-map.test.cjs`、Java側の検証はH2ドライバーをテスト用クラスパスに含めて `sbt test` で実行します。

## Running

Run this using [sbt](http://www.scala-sbt.org/).  If you downloaded this project from http://www.playframework.com/download then you'll find a prepackaged version of sbt in the project directory:

```
sbt run
```

And then go to http://localhost:9000 to see the running web application.