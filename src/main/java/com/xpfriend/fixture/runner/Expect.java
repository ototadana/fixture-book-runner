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

import java.util.List;

/**
 * データセットアップ、テスト対象処理実行、結果検証を一気に行うコマンド。
 * @author Ototadana
 */
class Expect extends CLIBase {
	
	/**
	 * 以下の処理を行う。
	 *  (1)データのセットアップを行い、
	 *  (2)テスト対象処理実行のためのコマンドラインを取得し、
	 *  (3)テスト対象処理を実行し、
	 *  (4)テスト対象処理の終了コードを検証し、
	 *  (5)データ状態の検証を行う。
	 * 
	 * @param args ブック名、シート名、テストケース名、および -debug, -server, -class オプション。
	 */
	public static void main(String[] args) throws Throwable {
		new Expect(args).run(true);
	}

	private Expect(String[] args) {
		super(args);
	}

	@Override
	protected void execute(FixtureBookRunner runner) throws Exception,
			AssertionError {
		List<String> command = runner.setupAndGetCommand();
		CommandResult result = CommandUtil.execute(command, true);
		runner.validateCommandResultAndStorage(result);
	}

	@Override
	protected String getUssageMessage() {
		return "M_FixtureBookRunner_Expect_Usage";
	}
}
