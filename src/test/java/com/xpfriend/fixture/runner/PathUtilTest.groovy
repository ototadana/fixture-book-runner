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
 * PathUtilのテスト。
 * @author Ototadana
 */
class PathUtilTest extends Specification {

	def "toBaseName[String]は指定されたパスから拡張子なしのファイル名を返す"() {
		expect:
		PathUtil.toBaseName("aa/bb/cc.txt") == "cc"
		PathUtil.toBaseName("aa\\bb\\dd.txt") == "dd"
		PathUtil.toBaseName("aa/bb/ee") == "ee"
	}
	
	def "toBaseName[File]は指定されたファイルから拡張子なしのファイル名を返す"() {
		expect:
		PathUtil.toBaseName(new File("aa/bb/cc.txt")) == "cc"
		PathUtil.toBaseName(new File("aa\\bb\\dd.txt")) == "dd"
		PathUtil.toBaseName(new File("aa/bb/ee")) == "ee"
	}

	def "getReportFileは '指定された文字列'.xml というファイル名でテスト結果出力先フォルダ上のファイルを返す"() {
		setup:
		PathUtil.reportDirectory = null
		Config.put("report", "target/pathutil/report01")
		expect:
		PathUtil.getReportFile("abc") == new File(Settings.getReportPath(), "abc.xml").getAbsoluteFile()
		cleanup:
		Config.put("report", null)
		PathUtil.reportDirectory = null
	}	

	def "getReportFileは テスト結果出力先フォルダが存在しない場合に作成する"() {
		setup:
		PathUtil.reportDirectory = null
		when:
		Config.put("report", "target/pathutil/report02")
		File reportDirectory = new File(Settings.getReportPath())
		if(reportDirectory.exists()) {
			assert reportDirectory.delete() == true
		}
		PathUtil.getReportFile("abc")
		then:
		new File(Settings.getReportPath()).exists() == true
		cleanup:
		Config.put("report", null)
		PathUtil.reportDirectory = null
	}
	
	def "isExcelFileは指定されたファイル名が.xlsxで終わっていればtrueを返す"() {
		expect:
		PathUtil.isExcelFile("abc.xlsx") == true
		PathUtil.isExcelFile("abc.XLSX") == true
		PathUtil.isExcelFile("abc.xls") == false
		PathUtil.isExcelFile("abc") == false
	}
	
	def "isExecutableFileは指定されたファイル名がテスト実行ファイルの場合trueを返す"() {
		when:
		TestUtil.assertDefaultExecutableSetting()
		then:
		PathUtil.isExecutableFile("abcTest") == true
		PathUtil.isExecutableFile("abcTest.sh") == true
		PathUtil.isExecutableFile("abcTest.bat") == true
		PathUtil.isExecutableFile("abcTest.cmd") == true
		PathUtil.isExecutableFile("abcTest.SH") == true
		PathUtil.isExecutableFile("abcTest.Bat") == true
		PathUtil.isExecutableFile("abcTest.CMD") == true
		PathUtil.isExecutableFile("abcTEST.xlsx") == true
		PathUtil.isExecutableFile("abcSpec") == true
		PathUtil.isExecutableFile("abcSpec.sh") == true
		PathUtil.isExecutableFile("abcSpec.bat") == true
		PathUtil.isExecutableFile("abcSpec.cmd") == true
		PathUtil.isExecutableFile("abcSpec.SH") == true
		PathUtil.isExecutableFile("abcSpec.Bat") == true
		PathUtil.isExecutableFile("abcSpec.CMD") == true
		PathUtil.isExecutableFile("abcSpec.xlsx") == true
		PathUtil.isExecutableFile("abc.xlsx") == false
		

	}
}
