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

import groovy.sql.GroovyResultSet;
import groovy.sql.Sql;

import javax.xml.bind.JAXB;

import com.xpfriend.fixture.runner.junit4.Testsuite;
import com.xpfriend.junk.ConfigException;

import spock.lang.Specification

/**
 * Setup のテスト。
 * @author Ototadana
 */
class SetupTest extends Specification {
	
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
	
	def initSetupTestXml() {
		return TestUtil.initReportFile("SetupTest.xml")
	}
	
	def "実行時のエラーはレポートファイルに出力される"() {
		setup:
		File report = initSetupTestXml()
		
		when:
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "sheet2x", "xxx"] as String[])
		
		then:
		ConfigException e = thrown()
		e.printStackTrace()
		report.exists() == true
		Testsuite suite = JAXB.unmarshal(report, Testsuite)
		suite.name == "SetupTest.xlsx"
		suite.tests == "1"
		suite.failures == null
		suite.errors == "1"
		suite.testcase.every {it.classname == "SetupTest"} == true
		suite.testcase.size() == 1
		suite.testcase[0].name == "sheet2x__xxx"
	}

	def "Excelファイル名、シート名、テストケース名を指定して実行できる"() {
		setup:
		File report = initSetupTestXml()
		sql.execute("delete from EMPLOYEE")
		
		when:
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "sheet2", "Bテストデータクリア条件とCテストデータの両方を定義"] as String[])

		then:
		report.exists() == false // 実行時にエラーがない場合はレポートファイルは出力されない
		findAll() == [[id:3, name:"cc"], [id:4, name:"dd"]]
	}
	
	def "Excelファイル名とシート名を指定して実行するとそのシートにあるテストケースがすべて実行される"() {
		setup:
		File report = initSetupTestXml()
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "sheet2", "Bテストデータクリア条件とCテストデータの両方を定義"] as String[])
		
		when:
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "sheet1"] as String[])

		then:
		report.exists() == false // 実行時にエラーがない場合はレポートファイルは出力されない
		findAll() == [[id:1, name:"aa"], [id:2, name:"bb"]]
	}
	
	def "Excelファイル名のみを指定して実行するとそのファイルに含まれるテストケースがすべて実行される"() {
		setup:
		File report = initSetupTestXml()
		
		when:
		Setup.main(["src/test/resources/books/SetupTest.xlsx"] as String[])

		then:
		report.exists() == false // 実行時にエラーがない場合はレポートファイルは出力されない
		findAll() == [[id:1, name:"aa"], [id:2, name:"bb"], [id:3, name:"cc"], [id:4, name:"dd"]]
	}
	
	def "オプション -server を付けるとサーバー上でFixtureBookを実行する"() {
		when:
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "-server"] as String[])
		then:
		ConnectException e = thrown()
		
		when:
		FixtureBookServer.start()
		Setup.main(["src/test/resources/books/SetupTest.xlsx", "-server"] as String[])
		then:
		true
		
		cleanup:
		FixtureBookServer.stop()
	}	

	private findAll() {
		def list = []
		sql.eachRow("select * from EMPLOYEE order by ID") {
			list.add(["id":it["id"], "name":it["name"]])
		}
		return list
	}
}
