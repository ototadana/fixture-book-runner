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

import com.xpfriend.junk.Resi;

/**
 * 文字列フォーマット用ユーティリティ。
 * @author Ototadana
 */
class FormatUtil {

	/**
	 * 指定された文字列をフォーマットする。
	 * @param formatKey 書式取得用のリソースキー。
	 * @param args 書式パラメタ。
	 * @return フォーマットされた文字列。
	 */
	public static String format(String formatKey, Object... args) {
		return String.format(Resi.get(formatKey), args);
	}
	
	/**
	 * AssertionError を作成する。
	 * @param formatKey 書式取得用のリソースキー。
	 * @param args 書式パラメタ。
	 * @return AssertionError。
	 */
	public static AssertionError createAssertionError(String formatKey, Object... args) {
		return new AssertionError(format(formatKey, args));
	}
}
