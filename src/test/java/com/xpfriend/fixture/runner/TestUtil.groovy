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

import javax.sql.DataSource;

import groovy.sql.Sql;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.h2.tools.Server;

import com.xpfriend.fixture.cast.temp.DatabaseConnection;
import com.xpfriend.junk.Config;

/**
 * テスト用ユーティリティ。
 * @author Ototadana
 */
class TestUtil {
	
	private static String encoding
	private static Server dbServer
	private static Properties dbProperties
	
	static setupSpec() {
		setSJISEncoding()
		setReportDirectory()
		startH2Server()
		createTable()
	}
	
	static cleanupSpec() {
		restoreDefaultEncoding()
		restoreReportDirectory()
		stopH2Server()
		initializeSetings()
	}
	
	static initializeSetings() {
		System.setProperty("com.xpfriend.fixture.runner.port", "")
		System.setProperty("com.xpfriend.fixture.runner.report", "")
		System.setProperty("com.xpfriend.fixture.runner.directory", "")
		System.setProperty("com.xpfriend.fixture.runner.executable", "")
		System.setProperty("com.xpfriend.fixture.runner.output-validation", "")
		Config.put("port", null)
		Config.put("report", null)
		Config.put("directory", null)
		Config.put("executable", null)
		Config.put("output-validation", null)
		PathUtil.executableFiles = null
	}
	
	static setReportDirectory() {
		PathUtil.reportDirectory = null
		Config.put("report", "target/reporttest")
	}
	
	static restoreReportDirectory() {
		PathUtil.reportDirectory = null
		Config.put("report", null)
	}

	static setSJISEncoding() {
		encoding = System.getProperty("file.encoding")
		System.setProperty("file.encoding", "Shift_JIS")
	}
	
	static restoreDefaultEncoding() {
		System.setProperty("file.encoding", encoding)
	}

	static Sql createSql() {
		if(dbProperties == null) {
			dbProperties = new Properties()
			def is = ClassLoader.getSystemResourceAsStream("db.properties")
			try {
				dbProperties.load(is)
			} finally {
				is.close()
			}
	
		}
		DataSource ds = BasicDataSourceFactory.createDataSource(dbProperties)
		return new Sql(ds)
	}
	
	static startH2Server() {
		DatabaseConnection.dataSourceMap.clear()
		dbServer = Server.createTcpServer().start()
	}
	
	static stopH2Server() {
		dbServer.stop()
	}
	
	static createTable() {
		Sql sql = createSql()
		execute(sql, """
			create table EMPLOYEE(
			    ID identity primary key,
			    NAME varchar,
			    AGE int,
			    RETIRE int,
			    LAST_UPDATED timestamp
			)""")
		execute(sql, """
			create table TEST_FB(
			    ID number(4) primary key,
			    NAME varchar(20)
			)""")
	}
	
	static execute(Sql sql, String text) {
		try {
			sql.execute(text)
		} catch(Exception e) {
		}
	}
	
	static assertDefaultExecutableSetting() {
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test", "Test.sh", "Test.bat", "Test.cmd", "Spec.xlsx", "Spec", "Spec.sh", "Spec.bat", "Spec.cmd"]
		true
	}
	
	static initReportFile(String fileName) {
		File report = new File(Settings.getReportPath(), fileName)
		if(report.exists()) {
			assert report.delete()
		}
		return report
	}
}
