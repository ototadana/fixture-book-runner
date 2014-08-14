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

import java.io.IOException;

/**
 * データ検証コマンド。
 * @author Ototadana
 */
class ValidateStorage extends CLIBase {
	
	/**
	 * データ検証を実行する。
	 * @param args ブック名、シート名、テストケース名、および -debug, -server, -class オプション。
	 */
	public static void main(String[] args) throws Throwable {
		new ValidateStorage(args).run(true);
	}

	private ValidateStorage(String[] args) {
		super(args);
	}

	@Override
	protected void execute(FixtureBookRunner runner) throws IOException,
			AssertionError {
		runner.validateStorage();
	}

	@Override
	protected String getUssageMessage() {
		return "M_FixtureBookRunner_ValidateStorage_Usage";
	}
}
