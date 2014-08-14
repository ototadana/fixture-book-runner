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
package com.xpfriend.fixture.runner

import java.text.SimpleDateFormat;

import javax.xml.bind.JAXB;

import com.xpfriend.fixture.runner.junit4.Testsuite;
import com.xpfriend.junk.Config;

import spock.lang.Specification

/**
 * Reportのテスト。
 * @author Ototadana
 */
class ReportTest extends Specification {

	def setupSpec() {
		TestUtil.setReportDirectory()
	}
	
	def cleanupSpec() {
		TestUtil.restoreReportDirectory()
	}
	
	def "writeは結果レポートファイルが存在しないときJUnit形式のXMLファイルを新規作成する"() {
		when:
		File reportFile = PathUtil.getReportFile("test01")
		if(reportFile.exists()) {
			assert reportFile.delete() == true
		}
		Report report = new Report("test01.xlsx", "sheet01__case01", null)
		Thread.sleep(100)
		report.write(null)
		then:
		reportFile.exists() == true
		Testsuite suite = JAXB.unmarshal(reportFile, Testsuite)
		suite.tests == "1"
		suite.name == "test01.xlsx"
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(suite.timestamp) instanceof Date
		suite.time.length() == 5
		new BigDecimal(suite.time) > 0.090
		suite.testcase.size() == 1
		suite.testcase[0].classname == "test01"
		suite.testcase[0].name == "sheet01__case01"
		suite.testcase[0].time.length() == 5
		new BigDecimal(suite.testcase[0].time) > 0.090
		suite.time == suite.testcase[0].time
	}
	
	def "writeは結果レポートファイルが存在するとき既存のファイルにtestcaseを追記する"() {
		setup:
		File reportFile = PathUtil.getReportFile("test02")
		if(reportFile.exists()) {
			assert reportFile.delete() == true
		}
		when:
		new Report("test02.xlsx", "sheet01__case01", null).write(null)
		new Report("test02.xlsx", "sheet01__case02", null).write(new Exception("ERR1"))
		new Report("test02.xlsx", "sheet01__case03", null).write(new Exception("ERR2\rLINE2"))
		new Report("test02.xlsx", "sheet02__case01", null).write(null)
		new Report("test02.xlsx", "sheet03__case01", null).write(new AssertionError("FAIL1\nLINE2"))
		new Report("test02.xlsx", "sheet03__case02", null).write(new AssertionError("FAIL2"))
		new Report("test02.xlsx", "sheet04__case01", null).write(
			new AssertionError("12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
		then:
		reportFile.exists() == true
		Testsuite suite = JAXB.unmarshal(reportFile, Testsuite)
		suite.tests == "7"
		suite.errors == "2"
		suite.failures == "3"
		suite.name == "test02.xlsx"
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(suite.timestamp) instanceof Date
		suite.time.length() == 5
		toLong(suite.time) == suite.testcase.sum {toLong(it.time)}
		suite.testcase.size() == 7

		suite.testcase[0].classname == "test02"
		suite.testcase[0].name == "sheet01__case01"
		suite.testcase[0].error == []
		suite.testcase[0].failure == []

		suite.testcase[1].classname == "test02"
		suite.testcase[1].name == "sheet01__case02"
		suite.testcase[1].error.collect {it.message} == ["ERR1"]
		suite.testcase[1].error[0].content.startsWith("java.lang.Exception: ERR1")
		suite.testcase[1].failure == []

		suite.testcase[2].classname == "test02"
		suite.testcase[2].name == "sheet01__case03"
		suite.testcase[2].error.collect {it.message} == ["ERR2"]
		suite.testcase[2].error[0].content.startsWith("java.lang.Exception: ERR2")
		suite.testcase[2].failure == []

		suite.testcase[3].classname == "test02"
		suite.testcase[3].name == "sheet02__case01"
		suite.testcase[3].error == []
		suite.testcase[3].failure == []

		suite.testcase[4].classname == "test02"
		suite.testcase[4].name == "sheet03__case01"
		suite.testcase[4].error == []
		suite.testcase[4].failure.collect {it.message} == ["FAIL1"]
		suite.testcase[4].failure[0].content.startsWith("java.lang.AssertionError: FAIL1")

		suite.testcase[5].classname == "test02"
		suite.testcase[5].name == "sheet03__case02"
		suite.testcase[5].error == []
		suite.testcase[5].failure.collect {it.message} == ["FAIL2"]
		suite.testcase[5].failure[0].content.startsWith("java.lang.AssertionError: FAIL2")

		suite.testcase[6].classname == "test02"
		suite.testcase[6].name == "sheet04__case01"
		suite.testcase[6].error == []
		suite.testcase[6].failure.collect {it.message} == [
			"12345678901234567890123456789012345678901234567890123456789012345678901234567..."]
		suite.testcase[6].failure[0].content.startsWith(
			"java.lang.AssertionError: 12345678901234567890123456789012345678901234567890123456789012345678901234567890")
	}

	def "コンストラクタのクラス名引数でファイル名を指定することが可能"() {
		when:
		File reportFile = PathUtil.getReportFile("aa.bb.test03")
		if(reportFile.exists()) {
			assert reportFile.delete() == true
		}
		Report report = new Report("test01.xlsx", "sheet01__case01", "aa.bb.test03")
		report.write(null)
		then:
		reportFile.exists() == true
		Testsuite suite = JAXB.unmarshal(reportFile, Testsuite)
		suite.tests == "1"
		suite.name == "test01.xlsx"
		suite.testcase.size() == 1
		suite.testcase[0].classname == "aa.bb.test03"
		suite.testcase[0].name == "sheet01__case01"
	}
	
	def "deleteは指定されたクラス名に該当するファイルを削除する"() {
		when:
		File reportFile = PathUtil.getReportFile("aa.bb.test04")
		if(!reportFile.exists()) {
			assert reportFile.createNewFile() == true
		}
		Report.delete("aa.bb.test04") == true
		then:
		reportFile.exists() == false
	}
	
	def "getFirstLineはException.getMessageがnullを返す際にtoStringで代替する"() {
		expect:
		new Report("a","a","a").getFirstLine(new Exception()) == "java.lang.Exception"
	}
	
	private long toLong(String time) {
		return (long)(Double.parseDouble(time) * 1000);
	}
}
