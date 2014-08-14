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
import com.xpfriend.junk.Loggi;

import groovy.sql.Sql;
import spock.lang.Specification

/**
 * Expectのテスト。
 * @author Ototadana
 */
class ExpectTest extends Specification {
	
	def setupSpec() {
		TestUtil.setupSpec()
	}
	
	def cleanupSpec() {
		TestUtil.cleanupSpec()
	}
	
	def "引数指定がないとエラーになる"() {
		when:
		Expect.main([] as String[])

		then:
		ConfigException e = thrown()
		print e
		e.toString().indexOf("com.xpfriend.fixture.runner.Expect") > -1
	}

	def "「E取得データ」が定義されていなくても、コマンドの終了コードが0ならばエラーにならない"() {
		setup:
		File report = new File(Settings.getReportPath(), "ExpectTest.xml")
		if(report.exists()) {
			assert report.delete()
		}
		expect:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"] as String[])
		report.exists()
	}
	
	def "「E取得データ」が定義されていない場合、コマンドの終了コードが0以外ならばエラーとなる"() {
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」が定義されておらず、かつ、コマンド実行が失敗する例"] as String[])
		then:
		AssertionError e = thrown()
		println e
	}
	
	def "「E取得データ」でコマンドの実行結果を判定できる（正常終了のケース）"() {
		expect:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "正常終了を想定した「E取得データ」が定義されている例"] as String[])
		true
	}
	
	def "「E取得データ」ではstdout,stderrの定義を省略できる"() {
		expect:
		Expect.main(["src/test/resources/books/ExpectTestEx.xlsx", "sheet1", "正常終了を想定した「E取得データ」が定義されている例"] as String[])
		true
	}
	
	def "「E取得データ」でコマンドの実行結果を判定できる（異常終了のケース）"() {
		expect:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "異常終了を想定した「E取得データ」が定義されている例"] as String[])
		true
	}
	
	def "「E取得データ」でコマンドの実行結果を判定できる（終了コード不一致のケース）"() {
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」でコマンドの実行結果を判定できる（終了コード不一致のケース）"] as String[])
		then:
		AssertionError e = thrown()
		println e
		e.getMessage().indexOf("10") > 0
	}
	
	def "「E取得データ」でコマンドの実行結果を判定できる（stdout不一致のケース）"() {
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」でコマンドの実行結果を判定できる（stdout不一致のケース）"] as String[])
		then:
		AssertionError e = thrown()
		println e
		e.getMessage().indexOf("OK@systemout1") > 0
	}
	
	def "「E取得データ」でコマンドの実行結果を判定できる（stderr不一致のケース）"() {
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」でコマンドの実行結果を判定できる（stderr不一致のケース）"] as String[])
		then:
		AssertionError e = thrown()
		println e
		e.getMessage().indexOf("OK@systemerr1") > 0
	}
	
	def "「E更新後データ」でDBデータの検証ができる（不一致のケース）"() {
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet2", "「E更新後データ」でDBデータの検証ができる（不一致のケース）"] as String[])
		then:
		AssertionError e = thrown()
		println e
		e.getMessage().indexOf("EMPLOYEE") > 0
	}
	
	def "オプション -server を付けるとサーバー上でFixtureBookを実行する"() {
		when:
		Expect.main(["-server", "src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"] as String[])
		then:
		ConnectException e = thrown()
		
		when:
		FixtureBookServer.start()
		Expect.main(["-server", "src/test/resources/books/ExpectTest.xlsx", "sheet1", "「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"] as String[])
		then:
		true
		
		when:
		Expect.main(["-server", "src/test/resources/books/ExpectTest.xlsx", "sheet2", "「E更新後データ」でDBデータの検証ができる（不一致のケース）"] as String[])
		then:
		AssertionError ae = thrown()
		println ae
		ae.getMessage().indexOf("EMPLOYEE") > 0
		
		when:
		Expect.main(["-server", "src/test/resources/books/ExpectTest.xxx", "sheet2", "「E更新後データ」でDBデータの検証ができる（不一致のケース）"] as String[])
		then:
		ConfigException ce = thrown()
		println ce
		ce.getMessage().indexOf("ExpectTest.xxx") > 0
		
		cleanup:
		FixtureBookServer.stop()
	}	
	
	def "Excelファイル名のみを指定するとそのファイルに含まれるすべてのテストが実行される"() {
		setup:
		File report = new File(Settings.getReportPath(), "all.ExpectTest.xml")
		if(report.exists()) {
			assert report.delete()
		}
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "-class", "all.ExpectTest"] as String[])
		then:
		AssertionError e = thrown()
		println e
		Testsuite suite = JAXB.unmarshal(report, Testsuite)
		suite.tests == "8"
		suite.failures == "5"
		suite.errors == null
		suite.testcase.every {it.classname == "all.ExpectTest"} == true
		suite.testcase.size() == 8
		suite.testcase[0].name == "sheet1__「E取得データ」が定義されておらず、かつ、コマンド実行が失敗する例"
		suite.testcase[1].name == "sheet1__「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"
		suite.testcase[2].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（stderr不一致のケース）"
		suite.testcase[3].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（stdout不一致のケース）"
		suite.testcase[4].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（終了コード不一致のケース）"
		suite.testcase[5].name == "sheet1__正常終了を想定した「E取得データ」が定義されている例"
		suite.testcase[6].name == "sheet1__異常終了を想定した「E取得データ」が定義されている例"
		suite.testcase[7].name == "sheet2__「E更新後データ」でDBデータの検証ができる（不一致のケース）"
	}
	
	def "Excelファイル名とシート名を指定するとそのシートに含まれるすべてのテストが実行される"() {
		setup:
		File report1 = new File(Settings.getReportPath(), "sheet1.ExpectTest.xml")
		File report2 = new File(Settings.getReportPath(), "sheet2.ExpectTest.xml")
		if(report1.exists()) {
			assert report1.delete()
		}
		if(report2.exists()) {
			assert report2.delete()
		}
		
		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet1", "-class", "sheet1.ExpectTest"] as String[])
		then:
		AssertionError e1 = thrown()
		println e1
		Testsuite suite1 = JAXB.unmarshal(report1, Testsuite)
		suite1.tests == "7"
		suite1.failures == "4"
		suite1.errors == null
		suite1.testcase.every {it.classname == "sheet1.ExpectTest"} == true
		suite1.testcase.size() == 7
		suite1.testcase[0].name == "sheet1__「E取得データ」が定義されておらず、かつ、コマンド実行が失敗する例"
		suite1.testcase[1].name == "sheet1__「E取得データ」が定義されておらず、かつ、コマンド実行が成功する例"
		suite1.testcase[2].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（stderr不一致のケース）"
		suite1.testcase[3].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（stdout不一致のケース）"
		suite1.testcase[4].name == "sheet1__「E取得データ」でコマンドの実行結果を判定できる（終了コード不一致のケース）"
		suite1.testcase[5].name == "sheet1__正常終了を想定した「E取得データ」が定義されている例"
		suite1.testcase[6].name == "sheet1__異常終了を想定した「E取得データ」が定義されている例"

		when:
		Expect.main(["src/test/resources/books/ExpectTest.xlsx", "sheet2", "-class", "sheet2.ExpectTest"] as String[])
		then:
		AssertionError e2 = thrown()
		println e2
		Testsuite suite2 = JAXB.unmarshal(report2, Testsuite)
		suite2.tests == "1"
		suite2.failures == "1"
		suite2.errors == null
		suite2.testcase.every {it.classname == "sheet2.ExpectTest"} == true
		suite2.testcase.size() == 1
		suite2.testcase[0].name == "sheet2__「E更新後データ」でDBデータの検証ができる（不一致のケース）"
	}
}
