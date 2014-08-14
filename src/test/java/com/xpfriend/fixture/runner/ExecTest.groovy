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

import javax.xml.bind.JAXB;

import com.xpfriend.fixture.runner.junit4.Testsuite;
import com.xpfriend.junk.ConfigException;

import spock.lang.Specification

/**
 * Exec のテスト。
 * @author Ototadana
 */
class ExecTest extends Specification {

	def setupSpec() {
		TestUtil.setupSpec()
	}
	
	def cleanupSpec() {
		TestUtil.cleanupSpec()
	}

	def "指定されたフォルダにある実行可能ファイルを実行する（正常終了のケース）"() {
		setup:
		File report1 = new File(Settings.getReportPath(), "Test01Test.xml")
		File report2 = new File(Settings.getReportPath(), "Test02Spec.xml")
		File report3 = new File(Settings.getReportPath(), "sub1.Test03Test.xml")

		when:
		Exec.main(["src/test/resources/exec/noerror", "-debug"] as String[])

		then:
		report1.exists() == true
		Testsuite suite1 = JAXB.unmarshal(report1, Testsuite)
		suite1.tests == "4"
		suite1.errors == null
		suite1.failures == null
		suite1.name == "Test01Test.xlsx"
		suite1.testcase.size() == 4
		suite1.testcase[0].name == "sheet01__1.「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"
		suite1.testcase[1].name == "sheet01__2.正常終了を想定した「E取得データ」が定義されている例"
		suite1.testcase[2].name == "sheet02__case-a"
		suite1.testcase[3].name == "sheet02__case-b"
		suite1.testcase.every {it.classname == "Test01Test"} == true

		report2.exists() == true
		Testsuite suite2 = JAXB.unmarshal(report2, Testsuite)
		suite2.tests == "1"
		suite2.errors == null
		suite2.failures == null
		suite2.name == "Test02Spec.bat"
		suite2.testcase.size() == 1
		suite2.testcase[0].classname == "Test02Spec"
		suite2.testcase[0].name == "Test02Spec.batの実行が正常終了すること"

		report3.exists() == true
		Testsuite suite3 = JAXB.unmarshal(report3, Testsuite)
		suite3.tests == "2"
		suite3.errors == null
		suite3.failures == null
		suite3.name == "Test03Test.xlsx"
		suite3.testcase.size() == 2
		suite3.testcase[0].classname == "sub1.Test03Test"
		suite3.testcase[0].name == "sheet01__case-a"
		suite3.testcase[1].classname == "sub1.Test03Test"
		suite3.testcase[1].name == "Test03Test.batの実行が正常終了すること"
	}
	
	def "指定されたフォルダにある実行可能ファイルを実行する（エラーのケース）"() {
		setup:
		File report1 = new File(Settings.getReportPath(), "Test04Spec.xml")
		File report2 = new File(Settings.getReportPath(), "Test05Test.xml")
		File report3 = new File(Settings.getReportPath(), "Test06Test.xml")
		
		when:
		Exec.main(["src/test/resources/exec/error"] as String[])

		then:
		AssertionError e = thrown()

		report1.exists() == true
		Testsuite suite1 = JAXB.unmarshal(report1, Testsuite)
		suite1.tests == "1"
		suite1.errors == null
		suite1.failures == "1"
		suite1.name == "Test04Spec.cmd"
		suite1.testcase[0].classname == "Test04Spec"
		suite1.testcase[0].name == "Test04Spec.cmdの実行が正常終了すること"
		suite1.testcase[0].failure[0].type == "java.lang.AssertionError"
		
		report2.exists() == true
		Testsuite suite2 = JAXB.unmarshal(report2, Testsuite)
		suite2.tests == "5"
		suite2.errors == "1"
		suite2.failures == "2"
		suite2.name == "Test05Test.xlsx"
		suite2.testcase[0].name == "sheet01__1.「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"
		suite2.testcase[1].name == "sheet01__2.正常終了を想定した「E取得データ」が定義されている例（予想結果間違い）"
		suite2.testcase[1].failure[0].message.startsWith("org.junit.ComparisonFailure: 67行目で列stdoutに指定された予想結果は")
		suite2.testcase[2].name == "sheet02__case-a"
		suite2.testcase[2].error[0].message.startsWith("com.xpfriend.junk.ConfigException: 44行目のデータを用いたDBテーブル「xEMPLOYEE」")
		suite2.testcase[3].name == "sheet02__case-b"
		suite2.testcase[4].name == "Test05Test.batの実行が正常終了すること"
		suite2.testcase[4].failure[0].type == "java.lang.AssertionError"
		suite2.testcase.every {it.classname == "Test05Test"} == true
	}
}
