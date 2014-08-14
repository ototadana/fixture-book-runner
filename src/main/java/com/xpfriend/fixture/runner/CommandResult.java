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

/**
 * コマンド実行結果。
 * @author Ototadana
 */
public class CommandResult {

	private String stdout;
	private String stderr;
	private int exitCode;
	
	/**
	 * 標準出力の内容を取得する。
	 * @return 標準出力の内容。
	 */
	public String getStdout() {
		return stdout;
	}

	/**
	 * 標準出力の内容をセットする。
	 * @param stdout 標準出力の内容。
	 */
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}
	
	/**
	 * 標準エラー出力の内容を取得する。
	 * @return 標準エラー出力の内容。
	 */
	public String getStderr() {
		return stderr;
	}
	
	/**
	 * 標準エラー出力の内容をセットする。
	 * @param stderr 標準エラー出力の内容。
	 */
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}
	
	/**
	 * 終了コードを取得する。
	 * @return 終了コード。
	 */
	public int getExitCode() {
		return exitCode;
	}
	
	/**
	 * 終了コードの内容をセットする。
	 * @param exitCode 終了コードの内容。
	 */
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	@Override
	public String toString() {
		return "exitCode:" + getExitCode() + ", "
				+ "stdout:" + getStdout() + ", "
				+ "stderr:" + getStderr();
	}
}
