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
 * ExecutableFilesDirectory のテスト。
 * @author Ototadana
 */
class ExecutableFilesDirectoryTest extends Specification {
	
	private static final String WINDOWS = "src/main/rt/windows/fixture-book-example/app-test/conf/Config.properties";
	private static final String UNIX = "src/main/rt/unix/fixture-book-example/app-test/conf/Config.properties";

	def cleanup() {
		TestUtil.initializeSetings()
	}

	def "実行可能ファイルは設定ファイルのexecutablesで定義されたものと一致する"() {
		when: "default"
		TestUtil.assertDefaultExecutableSetting()
		List<String> files = getExecutableFiles("src/test/resources/executables/a1/a2")
		then:
		files == ["Test1Spec.cmd", "Test1Test.bat", "Test2Test"]
		
		when: "unix"
		updateConfig(UNIX)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test", "Test.sh"]
		files = getExecutableFiles("src/test/resources/executables/a1/a2")
		then:
		files == ["Test2Test"]
		
		when: "windows"
		updateConfig(WINDOWS)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test.bat", "Test.cmd"]
		files = getExecutableFiles("src/test/resources/executables/a1/a2")
		then:
		files == ["Test1Test.bat"]
	}
	
	def "複数階層にまたがってファイル検索することができる"() {
		when: "default"
		TestUtil.assertDefaultExecutableSetting()
		List<String> files = getExecutableFiles("src/test/resources/executables/a1")
		then:
		files == ["Test3Test.xlsx", "test4Spec.xlsx", "Test1Spec.cmd", "Test1Test.bat", "Test2Test"]

		when: "unix"
		updateConfig(UNIX)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test", "Test.sh"]
		files = getExecutableFiles("src/test/resources/executables/a1")
		then:
		files == ["Test3Test.xlsx", "Test2Test"]
		
		when: "windows"
		updateConfig(WINDOWS)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test.bat", "Test.cmd"]
		files = getExecutableFiles("src/test/resources/executables/a1")
		then:
		files == ["Test3Test.xlsx", "Test1Test.bat"]
	}
	
	def "Excelファイルよりもスクリプトファイルの方が優先される"() {
		when: "default"
		TestUtil.assertDefaultExecutableSetting()
		List<String> files = getExecutableFiles("src/test/resources/executables/")
		then:
		files == ["Test3Test.xlsx", "test4Spec.xlsx", "test4Spec.sh", "test5Test.cmd", "Test1Spec.cmd", "Test1Test.bat", "Test2Test"]
		
		when: "unix"
		updateConfig(UNIX)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test", "Test.sh"]
		files = getExecutableFiles("src/test/resources/executables/")
		then:
		files == ["Test3Test.xlsx", "Test2Test"]
		
		when: "windows"
		updateConfig(WINDOWS)
		assert Settings.getExecutableFiles() == ["Test.xlsx", "Test.bat", "Test.cmd"]
		files = getExecutableFiles("src/test/resources/executables/")
		then:
		files == ["Test3Test.xlsx", "test5Test.cmd", "Test1Test.bat"]
	}
	
	private List<String> getExecutableFiles(String path) {
		println "--"
		return new ExecutableFilesDirectory(path).getExecutableFiles().values().collect {
			println it.getAbsolutePath()
			it.getName()
		}
	}

	private updateConfig(String configFile) {
		TestUtil.initializeSetings()
		FileInputStream is = new FileInputStream(configFile)
		try {
			Properties props = new Properties()
			props.load(is)
			Config.put("executable", props.get("executable"))
		} finally {
			is.close()
		}
	}
}
