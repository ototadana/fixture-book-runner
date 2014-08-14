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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.xml.bind.JAXB;

import com.xpfriend.fixture.runner.junit4.Error;
import com.xpfriend.fixture.runner.junit4.Failure;
import com.xpfriend.fixture.runner.junit4.Testcase;
import com.xpfriend.fixture.runner.junit4.Testsuite;
import com.xpfriend.junk.Strings;

/**
 * テスト結果レポート。
 * @author Ototadana
 */
class Report {
	
	private DecimalFormat timeFormat = new DecimalFormat("0.000");
	private long beginningTime;
	private String suiteName;
	private String classname;
	private String name;
	private File reportFile;
	private boolean summary;

	/**
	 * 指定されたクラス名に該当するレポートファイルを削除する。
	 * @param className クラス名。
	 * @return 該当するファイルが存在しない、もしくは削除できた場合は true。
	 */
	public static boolean delete(String className) {
		File file = getFile(className);
		if(file.exists()) {
			return file.delete();
		} else {
			return true;
		}
	}
	
	/**
	 * 指定されたクラス名に該当するレポートファイルを取得する。
	 * @param className クラス名。
	 * @return レポートファイル。
	 */
	public static File getFile(String className) {
		return PathUtil.getReportFile(className);
	}
	
	/**
	 * Report を作成する。
	 * @param suiteName ブックのパス。
	 * @param testCaseName テストケース名。
	 * @param className レポートファイルの名前。
	 */
	public Report(String suiteName, String testCaseName, String className) {		
		if(className == null) {
			className = PathUtil.toBaseName(suiteName);
		}
		this.suiteName = suiteName;
		this.classname = className;
		this.name = testCaseName;
		this.reportFile = PathUtil.getReportFile(className);
		this.beginningTime = System.currentTimeMillis();
	}
	
	/**
	 * 現在出力対象としているテストケースがサマリーテストケースかどうかを設定する。
	 * @param summary サマリーテストケースかどうか。
	 */
	public void setSummary(boolean summary) {
		this.summary = summary;
	}

	/**
	 * 現在出力対象としているテストケースがサマリーテストケースかどうかを調べる。
	 * @return サマリーテストケースならば true。
	 */
	public boolean isSummary() {
		return summary;
	}
		
	/**
	 * 結果レポートをファイル出力する。
	 * @param e テストで発生した例外。
	 */
	public void write(Throwable e) {
		long currentTime = System.currentTimeMillis();
		long time = currentTime - beginningTime;
		Testsuite suite = readReportFile();
		suite.setTimestamp(new java.sql.Timestamp(currentTime).toString());
		if(summary) {
			time = subtractTime(time, suite.getTime());
		}
		suite.setTime(addTime(suite.getTime(), time));

		List<Testcase> testcases = suite.getTestcase();
		Testcase testcase = createTestcase(e, time);
		testcases.add(testcase);
		suite.setTests(String.valueOf(testcases.size()));
		if(!testcase.getError().isEmpty()) {
			suite.setErrors(add(suite.getErrors(), 1));
		}
		if(!testcase.getFailure().isEmpty()) {
			suite.setFailures(add(suite.getFailures(), 1));
		}
		
		JAXB.marshal(suite, reportFile);
	}

	private Testsuite readReportFile() {
		if(reportFile.exists()) {
			return JAXB.unmarshal(reportFile, Testsuite.class);
		} else {
			Testsuite suite = new Testsuite();
			suite.setName(suiteName);
			return suite;
		}
	}

	private String add(String a, long b) {
		if(Strings.isEmpty(a)) {
			return String.valueOf(b);
		} else {
			long value = 0L;
			try {
				value = Long.parseLong(a);
			} catch(Exception e) {
			}
			return String.valueOf(value + b);
		}
	}
	
	private String addTime(String a, long b) {
		if(Strings.isEmpty(a)) {
			return toTime(b);
		} else {
			return toTime(toLong(a) + b);
		}
	}

	private long subtractTime(long a, String b) {
		if(Strings.isEmpty(b)) {
			return a;
		} else {
			return a - toLong(b);
		}
	}
	
	private long toLong(String a) {
		long value = 0L;
		try {
			value = (long)(Double.parseDouble(a) * 1000);
		} catch(Exception e) {
		}
		return value;
	}

	private Testcase createTestcase(Throwable e, long time) {
		Testcase testcase = new Testcase();
		testcase.setClassname(classname);
		testcase.setName(name);
		testcase.setTime(toTime(time));
		
		if(e instanceof AssertionError) {
			Failure failure = new Failure();
			failure.setType(e.getClass().getName());
			failure.setContent(getStackTrace(e));
			failure.setMessage(getFirstLine(e));
			testcase.getFailure().add(failure);
		} else if(e != null) {
			Error error = new Error();
			error.setType(e.getClass().getName());
			error.setContent(getStackTrace(e));
			error.setMessage(getFirstLine(e));
			testcase.getError().add(error);
		}
		return testcase;
	}
	
	private String getStackTrace(Throwable e) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}

	private String toTime(double time) {
		return timeFormat.format(time / 1000);
	}

	private String getFirstLine(Throwable exception) {
		String message = exception.getMessage();
		if(message == null) {
			message = exception.toString();
		}
		StringBuilder sb = new StringBuilder(80);
		for(int i = 0; i < 77; i++) {
			if(message.length() <= i) {
				return sb.toString();
			}
			char c = message.charAt(i);
			if(c == '\r' || c == '\n') {
				return sb.toString();
			} else {
				sb.append(c);
			}
		}
		sb.append("...");
		return sb.toString();
	}
}
