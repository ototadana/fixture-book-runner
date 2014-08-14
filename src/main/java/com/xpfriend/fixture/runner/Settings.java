/*
 * Copyright 2014 XPFriend Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xpfriend.fixture.runner;

import com.xpfriend.junk.Config;
import com.xpfriend.junk.Strings;

/**
 * 設定。
 * @author Ototadana
 */
class Settings {

	/**
	 * サーバのポート番号を取得する。
	 * @return 設定ファイルの "port" もしくは "com.xpfriend.fixture.runner.port" のいずれかで設定された値。
	 *         デフォルトは 7878。
	 */
	public static int getPort() {
		return Integer.parseInt(get("port", "7878"));
	}

	/**
	 * テスト結果レポート出力先フォルダのパスを取得する。
	 * @return 設定ファイルの "report" もしくは "com.xpfriend.fixture.runner.report" のいずれかで設定された値。
	 *         デフォルトは "./reports"。
	 */
	public static String getReportPath() {
		return get("report", "./reports");
	}
	
	/**
	 * 別プロセスでテストを呼び出す際のワーキングディレクトリを取得する。
	 * @return 設定ファイルの "directory" もしくは "com.xpfriend.fixture.runner.directory" のいずれかで設定された値。
	 *         デフォルトは null。
	 */
	public static String getWorkingDirectory() {
		return get("directory", null);
	}
	
	/**
	 * テスト実行ファイルを示すファイル拡張子を取得する。
	 * @return テスト実行ファイルを示すファイル拡張子。設定ファイルの "executable" もしくは 
	 *         "com.xpfriend.fixture.runner.executable" のいずれかで設定された値。
	 *         セミコロンを区切り文字とする。
	 *         デフォルトの設定は "Test.xlsx;Test;Test.sh;Test.bat;Test.cmd;Spec.xlsx;Spec;Spec.sh;Spec.bat;Spec.cmd"。
	 */
	public static String[] getExecutableFiles() {
		return get("executable", "Test.xlsx;Test;Test.sh;Test.bat;Test.cmd;Spec.xlsx;Spec;Spec.sh;Spec.bat;Spec.cmd").split(";");
	}
	
	private static String get(String key, String defaultValue) {
		String systemProperyValue = System.getProperty("com.xpfriend.fixture.runner." + key);
		if(!Strings.isEmpty(systemProperyValue)) {
			defaultValue = systemProperyValue;
		}
		return Config.get(key, defaultValue);
	}
}
