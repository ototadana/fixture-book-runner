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

import groovy.sql.Sql;
import spock.lang.Specification

/**
 * ValidateStorage のテスト。
 * @author Ototadana
 */
class ValidateStorageTest extends Specification {

	Sql sql

	def setupSpec() {
		TestUtil.setupSpec()
	}
	
	def cleanupSpec() {
		TestUtil.cleanupSpec()
	}
	
	def setup() {
		sql = TestUtil.createSql()
	}
	
	def cleanup() {
		sql.close()	
	}
	
	def initValidateStorageTestXml() {
		return TestUtil.initReportFile("ValidateStorageTest.xml")
	}
	
	def setupTable() {
		sql.execute("delete from EMPLOYEE")
		sql.execute("insert into EMPLOYEE(ID,NAME,AGE) VALUES(1,'aa',10)")
	}
	
	def "引数指定がないとエラーになる"() {
		when:
		ValidateStorage.main([] as String[])

		then:
		ConfigException e = thrown()
		print e
		e.toString().indexOf("com.xpfriend.fixture.runner.ValidateStorage") > -1
	}

	def "Excelファイル名、シート名、テストケース名を指定して実行できる"() {
		setup:
		File report = initValidateStorageTestXml()
		setupTable()

		when:
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx", "sheet1", "「E更新後データ」でDBデータの検証ができる（1）"] as String[])

		then:
		Testsuite suite = validateSuite(report)
		suite.tests == "1"
		suite.testcase.size() == 1
		suite.testcase[0].name == "sheet1__「E更新後データ」でDBデータの検証ができる（1）"
	}
	
	def "Excelファイル名、シート名を指定して実行できる"() {
		setup:
		File report = initValidateStorageTestXml()
		setupTable()
		
		when:
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx", "sheet1"] as String[])

		then:
		Testsuite suite = validateSuite(report)
		suite.tests == "2"
		suite.testcase.size() == 2
		suite.testcase[0].name == "sheet1__「E更新後データ」でDBデータの検証ができる（1）"
		suite.testcase[1].name == "sheet1__「E更新後データ」でDBデータの検証ができる（2）"
	}
	
	def "Excelファイル名を指定して実行できる"() {
		setup:
		File report = initValidateStorageTestXml()
		setupTable()
		
		when:
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx"] as String[])

		then:
		Testsuite suite = validateSuite(report)
		suite.tests == "3"
		suite.testcase.size() == 3
		suite.testcase[0].name == "sheet1__「E更新後データ」でDBデータの検証ができる（1）"
		suite.testcase[1].name == "sheet1__「E更新後データ」でDBデータの検証ができる（2）"
		suite.testcase[2].name == "sheet2__「E更新後データ」でDBデータの検証ができる（3）"
	}
	
	def validateSuite(File report) {
		assert report.exists() == true
		Testsuite suite = JAXB.unmarshal(report, Testsuite)
		assert suite.name == "ValidateStorageTest.xlsx"
		assert suite.failures == null
		assert suite.errors == null
		assert suite.testcase.every {it.classname == "ValidateStorageTest"} == true
		suite
	}
	
	def "検証エラーレポートを出力できる"() {
		setup:
		File report = initValidateStorageTestXml()
		sql.execute("delete from EMPLOYEE")
		sql.execute("insert into EMPLOYEE(ID,NAME,AGE) VALUES(1,'aa',11)")
		
		when:
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx", "sheet1", "「E更新後データ」でDBデータの検証ができる（1）"] as String[])

		then:
		AssertionError e = thrown()
		report.exists() == true
		Testsuite suite = JAXB.unmarshal(report, Testsuite)
		suite.name == "ValidateStorageTest.xlsx"
		suite.failures == "1"
		suite.errors == null
		suite.tests == "1"
		suite.testcase.every {it.classname == "ValidateStorageTest"} == true
		suite.testcase.size() == 1
		suite.testcase[0].name == "sheet1__「E更新後データ」でDBデータの検証ができる（1）"
		println suite.testcase[0].failure[0].message.startsWith("9行目で列AGEに指定された予想結果は")
	}
	
	def "オプション -server を付けるとサーバー上でFixtureBookを実行する"() {
		setup:
		setupTable()

		when:
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx", "-server"] as String[])
		then:
		ConnectException e = thrown()
		
		when:
		FixtureBookServer.start()
		ValidateStorage.main(["src/test/resources/books/ValidateStorageTest.xlsx", "-server"] as String[])
		then:
		true
		
		cleanup:
		FixtureBookServer.stop()
	}	

}
