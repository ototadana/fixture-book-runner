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

import java.io.File;

/**
 * ファイルおよびファイルパス関連のユーティリティ。
 * @author Ototadana
 */
class PathUtil {

	private static File reportDirectory;
	private static String[] executableFiles;
	
	/**
	 * 指定されたパスの中から拡張子なしのファイル名を取り出す。
	 * @param path 対象となるパス文字列。
	 * @return 拡張子なしのファイル名。
	 */
	public static String toBaseName(String path) {
		return toBaseName(new File(path));
	}

	/**
	 * 指定されたファイルの中から拡張子なしのファイル名を取り出す。
	 * @param path 対象となるファイル。
	 * @return 拡張子なしのファイル名。
	 */
	public static String toBaseName(File file) {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if(dotIndex > -1) {
			name = name.substring(0, dotIndex);
		}
		return name;
	}
	
	/**
	 * 指定された実行可能ファイル名に対応するレポートファイルを取得する。
	 * @param baseName 実行可能ファイル名（拡張子なし）。
	 * @return レポートファイル。
	 */
	public static File getReportFile(String baseName) {
		if(reportDirectory == null) {
			reportDirectory = new File(Settings.getReportPath());
			if(!reportDirectory.exists()) {
				reportDirectory.mkdirs();
			}
		}
		return new File(reportDirectory, baseName + ".xml").getAbsoluteFile();
	}

	/**
	 * 指定されたファイルが Excel ファイルかどうかを調べる。
	 * @param fileName ファイル名。
	 * @return Excelファイルならば true。
	 */
	public static boolean isExcelFile(String fileName) {
		return endsWith(fileName, ".xlsx");
	}

	/**
	 * 指定されたファイルがスクリプトファイルかどうかを調べる。
	 * @param fileName ファイル名。
	 * @return スクリプトファイルならば true。
	 */
	public static boolean isExecutableFile(String fileName) {
		if(executableFiles == null) {
			executableFiles = Settings.getExecutableFiles();
		}
		for(String suffix : executableFiles) {
			if(endsWith(fileName, suffix)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定されたファイル名が指定された拡張子を持つかどうかを調べる
	 * @param fileName ファイル名。
	 * @param ext 拡張子。
	 * @return 指定されたファイル名が指定された拡張子を持つならば true。
	 */
	private static boolean endsWith(String fileName, String ext) {
		return fileName.regionMatches(true, fileName.length() - ext.length(), ext, 0, ext.length());
	}
}
