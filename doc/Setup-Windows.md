FixtureBookRunner - Windows 環境でのセットアップ
================================================

必須ソフトウェア
----------------
*   Java SE 6.0 以上。


セットアップ
------------
1.  ダウンロードした zip ファイルを任意の場所に展開する。
2.  展開すると **fixture-book** という名前のフォルダができるので、
    その中にある **testexec.bat** を任意のフォルダから実行できるようにするため、
    環境変数 **PATH** に **fixture-book** を追加する。

### (参考) fixture-book フォルダ構成

      ─fixture-book/
        │  testexec.bat            ... テスト実行用バッチファイル(Windows用)
        │  testexec                ... テスト実行用スクリプト(Unix用)
        │  *-windows-example.zip   ... Windows環境用サンプル
        │  *-unix-example.zip      ... Unix環境用サンプル
        │
        └─lib/                    ... Javaライブラリ(.jar)


サンプルの実行
--------------
fixture-book\*-window-example.zip は、Window環境用テストサンプルです。
このサンプルは実際に実行して動作確認できます。

### テストサンプル実行方法
1.  *-windows-example.zip  ファイルを展開する。

    展開すると以下のような **fixture-book-example** フォルダが作成される。

        ─ fixture-book-example/
            ├── app/             ... テスト対象アプリケーション
            │  │  ExampleJob.bat      ... テスト対象となるバッチファイル
            │  ├─classes/            ... 上記バッチファイルが呼び出すクラス
            │  └─lib/                ... 上記クラスが利用するライブラリ
            │
            └── app-test/        ... テスト実行環境
                ├── conf/            ... テスト実行時設定
                ├── lib/             ... テスト実行時に必要となるライブラリ
                ├── reports/         ... テスト結果レポート
                └── tests/           ... テスト実行用ファイル（バッチファイル、Excelファイル）

2.  テスト実行環境に移動して、`testexec` コマンドを実行する。

    ```bat
    cd fixture-book-example\app-test
    testexec
    ```

3.  実行終了すると **reports** フォルダにテスト結果レポート(XMLファイル)
    が格納される。


使ってみよう
------------
実際の使い方については、以下のドキュメントを参照してください。
*   [FixtureBookRunner の利用方法](./Tutorial.md)

