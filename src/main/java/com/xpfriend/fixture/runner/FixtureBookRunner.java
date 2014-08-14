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
import java.util.List;

import com.xpfriend.fixture.staff.Book;
import com.xpfriend.fixture.staff.Case;
import com.xpfriend.fixture.staff.Column;
import com.xpfriend.fixture.staff.Section;
import com.xpfriend.fixture.staff.Section.SectionType;
import com.xpfriend.fixture.staff.Sheet;
import com.xpfriend.fixture.staff.Table;
import com.xpfriend.junk.Loggi;
import com.xpfriend.junk.Strings;

/**
 * FixtureBook の実行処理。
 * @author Ototadana
 */
class FixtureBookRunner {

	/**
	 * FixtureBookRunner のインスタンスを取得する。
	 * @param server サーバー上で実行するかどうか。
	 * @param bookPath Excelファイルのパス。
	 * @param sheetName シート名。
	 * @param testCaseName テストケース名。
	 * @return FixtureBookRunner のインスタンス。
	 */
	public static FixtureBookRunner getInstance(boolean server, String bookPath, String sheetName, String testCaseName) {
		if(server) {
			return new FixtureBookServer(bookPath, sheetName, testCaseName);
		} else {
			return new FixtureBookRunner(bookPath, sheetName, testCaseName);
		}
	}
	
	protected String bookPath;
	protected String sheetName;
	protected String testCaseName;
	
	/**
	 * FixtureBookRunner を作成する。
	 * @param bookPath Excelファイルのパス。
	 * @param sheetName シート名。
	 * @param testCaseName テストケース名。
	 */
	protected FixtureBookRunner(String bookPath, String sheetName, String testCaseName) {
		this.bookPath = bookPath;
		this.sheetName = sheetName;
		this.testCaseName = testCaseName;
	}

	/**
	 * FixtureBook の setup メソッド呼び出しを行う。
	 */
	public void setup() throws IOException, AssertionError {
		Case testCase = getTestCase(bookPath, sheetName, testCaseName);
		testCase.setup();
	}

	/**
	 * FixtureBook の setup メソッド呼び出しを行い、コマンドラインの取得を行う。
	 * @return コマンドライン。
	 */
	public List<String> setupAndGetCommand() throws IOException, AssertionError {
		Case testCase = getTestCase(bookPath, sheetName, testCaseName);
		testCase.setup();
		return testCase.getList(String.class, null);
	}

	/**
	 * FixtureBook の validateStorage メソッドの呼び出しを行う。
	 */
	public void validateStorage() throws IOException, AssertionError {
		Case testCase = getTestCase(bookPath, sheetName, testCaseName);
		testCase.validateStorage();
	}

	/**
	 * FixtureBook の validate でコマンド実行結果検証を行い、validateStorage メソッドの呼び出しを行う。
	 * @param result コマンド実行結果。
	 */
	public void validateCommandResultAndStorage(CommandResult result) throws IOException, AssertionError {
		Case testCase = getTestCase(bookPath, sheetName, testCaseName);
		validateCommandResult(testCase, result);
		testCase.validateStorageInternal();
	}
	
	private void validateCommandResult(Case testCase, CommandResult result)
			throws IOException {
		Section section = testCase.getSection(SectionType.EXPECTED_RESULT);
		if(section != null) {
			List<String> tableNames = section.getTableNames();
			if(!tableNames.isEmpty()) {
				convertLineSeparator(result);
				testCase.validate(result, tableNames.get(0));
				return;
			}
		}
		if(result.getExitCode() != 0) {
			throw FormatUtil.createAssertionError(
					"M_FixtureBookRunner_Expect_Command_Failed",
					result.getExitCode());
		}
	}
	
	private void convertLineSeparator(CommandResult result) {
		result.setStderr(convertLineSeparator(result.getStderr()));
		result.setStdout(convertLineSeparator(result.getStdout()));
	}

	private String convertLineSeparator(String text) {
		if(Strings.isEmpty(text)) {
			return text;
		}
		return text.replace("\r\n", "\n");
	}

	private static Case getTestCase(String bookPath, String sheetName, String testCaseName) throws IOException {
		Book book = Book.getInstance(FixtureBookRunner.class, bookPath);
		Sheet sheet = book.getSheet(sheetName);
		Case testCase = sheet.getCase(testCaseName);
		Loggi.debug("FixtureBook : Case : " + testCase);
		return testCase;
	}

	public boolean isRequiredOutputValidation() throws IOException {
		Case testCase = getTestCase(bookPath, sheetName, testCaseName);
		Section section = testCase.getSection(SectionType.EXPECTED_RESULT);
		if(section == null) {
			return false;
		}
		List<String> tableNames = section.getTableNames();
		if(tableNames.isEmpty()) {
			return false;
		}
		Table table = section.getTable(tableNames.get(0));
		for(Column column : table.getColumns()) {
			if(column != null) {
				String name = column.getName();
				if("stdout".equalsIgnoreCase(name) || "stderr".equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		return false;
	}
}
