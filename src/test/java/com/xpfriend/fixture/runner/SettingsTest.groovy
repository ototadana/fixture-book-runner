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

import com.xpfriend.junk.Config;

import spock.lang.Specification

/**
 * Settings のテスト。
 * @author Ototadana
 */
class SettingsTest extends Specification {

	def cleanup() {
		TestUtil.initializeSetings()
	}
	
	def "getPort はデフォルトのポート番号として7878を返す"() {
		expect: 
		Settings.getPort() == 7878
	}
	
	def "getPort はcom.xpfriend.fixture.runner.portに設定された値を返す"() {
		when:
		System.setProperty("com.xpfriend.fixture.runner.port", "8888")
		then: 
		Settings.getPort() == 8888
	}

	def "getPort は設定ファイルのportに設定された値を返す"() {
		when:
		System.setProperty("com.xpfriend.fixture.runner.port", "8888")
		Config.put("port", "9999")
		then: 
		Settings.getPort() == 9999
	}

	def "getReportPath はデフォルトのレポート出力先として ./reports を返す"() {
		expect:
		Settings.getReportPath() == "./reports"
	}
	
	def "getReportPath はcom.xpfriend.fixture.runner.reportに設定された値 を返す"() {
		when:
		System.setProperty("com.xpfriend.fixture.runner.report", "./")
		then:
		Settings.getReportPath() == "./"
	}
	
	def "getReportPath は設定ファイルのreportに設定された値 を返す"() {
		when:
		Config.put("report", "..")
		then:
		Settings.getReportPath() == ".."
	}
	
	def "getWorkingDirectory はデフォルトのワーキングディレクトリとして null を返す"() {
		expect:
		Settings.getWorkingDirectory() == null
	}

	def "getWorkingDirectory はcom.xpfriend.fixture.runner.direcotryに設定された値 を返す"() {
		when:
		System.setProperty("com.xpfriend.fixture.runner.directory", "../")
		then:
		Settings.getWorkingDirectory() == "../"
	}

	def "getWorkingDirectory は設定ファイルのdirectoryに設定された値 を返す"() {
		when:
		Config.put("directory", ".")
		then:
		Settings.getWorkingDirectory() == "."
	}
	
	def "getExecutableFilesはデフォルト値としてファイル名がTestまたはSpecでおわり、拡張子なしもしくは.xlsx, .sh, .bat, .cmd を返す"() {
		expect:
		TestUtil.assertDefaultExecutableSetting()
	}

	def "getExecutableFilesはcom.xpfriend.fixture.runner.executableに設定された値を返す"() {
		when:
		System.setProperty("com.xpfriend.fixture.runner.executable", ".aaa;.bbb")
		then:
		Settings.getExecutableFiles() == [".aaa", ".bbb"]
	}

	def "getExecutableFilesは設定ファイルのexecutableに設定された値 を返す"() {
		when:
		Config.put("executable", ".bat;.cmd")
		then:
		Settings.getExecutableFiles() == [".bat", ".cmd"]
	}
}
