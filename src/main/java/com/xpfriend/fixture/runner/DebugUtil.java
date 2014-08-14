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

import com.xpfriend.fixture.staff.Book;
import com.xpfriend.junk.Loggi;

/**
 * デバッグモード切り替えユーティリティ。
 * @author Ototadana
 */
class DebugUtil {
	
	/**
	 * デバッグモードに設定する。
	 */
	public static void setDebugEnabled() {
		Book.setDebugEnabled(true);
		Loggi.setDebugEnabled(true);
	}

	/**
	 * デバッグモードに設定されているかどうかを調べる
	 * @return デバッグモードに設定されている場合は true。
	 */
	public static boolean isDebugEnabled() {
		return Book.isDebugEnabled();
	}	
}
