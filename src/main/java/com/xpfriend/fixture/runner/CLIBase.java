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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.xpfriend.fixture.FixtureBook;
import com.xpfriend.fixture.staff.Book;
import com.xpfriend.fixture.staff.Case;
import com.xpfriend.fixture.staff.Sheet;
import com.xpfriend.junk.ConfigException;
import com.xpfriend.junk.Resi;

/**
 * CLIの基底クラス。
 * @author Ototadana
 */
abstract class CLIBase {

	static String SERVER_OPTION = "-server";
	static String DEBUG_OPTION = "-debug";
	static String CLASS_OPTION = "-class";
	
	static {
		Resi.add(FixtureBook.class.getPackage().getName());
		Resi.add(CLIBase.class.getPackage().getName());
	}
	
	private String className;
	private boolean serverOption;
	private List<String> parameters;
	private Throwable throwable;

	/**
	 * CLIBaseを作成する。
	 * @param args コマンドライン引数。
	 */
	protected CLIBase(String[] args) {
		this.parameters = getParameters(args);
	}

	private List<String> getParameters(String[] args) {
		List<String> parameters = new ArrayList<String>();
		boolean classOption = false;
		for(String arg : args) {
			if(DEBUG_OPTION.equals(arg)) {
				DebugUtil.setDebugEnabled();
			} else if(SERVER_OPTION.equals(arg)) {
				serverOption = true;
			} else if(CLASS_OPTION.equals(arg)) {
				classOption = true;
			} else if(classOption) {
				className = arg;
			} else {
				parameters.add(arg);
			}
		}
		validateParameters(parameters);
		return parameters;
	}
	
	private void validateParameters(List<String> parameters) {
		if(parameters.size() < 1) {
			throw new ConfigException(getUssageMessage());
		}
	}
	
	/**
	 * 使用方法を記述した文字列を取得する。
	 * @return 使用方法を記述した文字列。
	 */
	protected abstract String getUssageMessage();
	
	/**
	 * 処理を実行する。
	 * @param writeReport 処理結果のレポート出力を行う場合は true を指定する。
	 */
	protected void run(boolean writeReport) throws Throwable {
		throwable = null;

		if(parameters.size() > 2) {
			run(writeReport, getFixtureBookRunner(parameters.get(0), parameters.get(1), parameters.get(2)));
		} else {
			String bookPath = parameters.get(0);
			Book book = Book.getInstance(getClass(), bookPath);
			if(parameters.size() == 2) {
				String sheetName = parameters.get(1);
				run(writeReport, bookPath, sheetName, book.getSheet(sheetName));
			} else {
				run(writeReport, bookPath, book);
			}
		}
		
		if(throwable != null) {
			throw throwable;
		}
	}

	private void run(boolean writeReport, String bookPath, Book book) throws Exception {
		Map<String, Sheet> sheets = getSheets(book);
		for(Map.Entry<String, Sheet> sheetEntry : sheets.entrySet()) {
			String sheetName = sheetEntry.getKey();
			Sheet sheet = sheetEntry.getValue();
			run(writeReport, bookPath, sheetName, sheet);
		}
	}

	private void run(boolean writeReport, String bookPath, String sheetName,
			Sheet sheet) throws Exception, IOException {
		Map<String, Case> cases = getCases(sheet);
		for(String caseName : cases.keySet()) {
			run(writeReport, getFixtureBookRunner(bookPath, sheetName, caseName));
		}
	}
	
	private void run(boolean writeReport, FixtureBookRunner runner) throws Exception {
		Report report = new Report(new File(runner.bookPath).getName(),
				runner.sheetName + "__" + runner.testCaseName, className);
		try {
			execute(runner);
			if(writeReport) {
				report.write(null);
			}
		} catch(AssertionError e) {
			report.write(e);
			throwable = e;
		} catch(Exception e) {
			report.write(e);
			throwable = e;
		}
	}

	/**
	 * 指定された FixtureBookRunner を使って処理を実行する。
	 * @param runner FixtureBookRunner。
	 */
	protected abstract void execute(FixtureBookRunner runner) throws Exception, AssertionError;

	@SuppressWarnings("unchecked")
	private Map<String, Case> getCases(Sheet sheet) throws Exception {
		sheet.getCase(Case.ANONYMOUS);
		Map<String, Case> cases = new TreeMap<String, Case>();
		Field field = Sheet.class.getDeclaredField("caseMap");
		field.setAccessible(true);
		cases.putAll((Map<String, Case>)field.get(sheet));
		cases.remove(Case.ANONYMOUS);
		return cases;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Sheet> getSheets(Book book) throws Exception {
		Map<String, Sheet> sheets = new TreeMap<String, Sheet>();
		Field field = Book.class.getDeclaredField("sheetMap");
		field.setAccessible(true);
		sheets.putAll((Map<String, Sheet>) field.get(book));
		return sheets;
	}

	private FixtureBookRunner getFixtureBookRunner(String bookPath,
			String sheetName, String testCaseName) throws IOException {
		return FixtureBookRunner.getInstance(serverOption, bookPath, sheetName, testCaseName);
	}
}
