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
import com.xpfriend.junk.Loggi;

import spock.lang.Specification

/**
 * CommandUtil のテスト。
 * @author Ototadana
 */
class CommandUtilTest extends Specification {

	def setupSpec() {
		TestUtil.setSJISEncoding()
		File dir = new File("target/commandutil")
		if(dir.exists()) {
			assert dir.deleteDir()
		}
		dir.mkdirs()
		Config.put("directory", "target/commandutil")
		Loggi.debugEnabled = true		
	}

	def cleanupSpec() {
		TestUtil.restoreDefaultEncoding()
		Config.put("directory", null)
	}

	def "executeは指定されたコマンドを実行する"() {
		setup:
		Config.put("abc", "あああ")
		
		when:
		CommandResult result01 = CommandUtil.execute(["..\\..\\src\\test\\resources\\scripts\\test01.bat", "\${abc}"], true)
		println result01.stdout
		then:
		result01.exitCode == 0
		result01.stdout.size() > 0
		result01.stderr == ""
		new File("target/commandutil/test01.txt").readLines()[0].trim() == "test01:あああ"

		when:
		CommandResult result02 = CommandUtil.execute(["..\\..\\src\\test\\resources\\scripts\\test02.cmd", "bbb"], true)
		println result02.stdout
		then:
		result02.exitCode == 0
		result02.stdout.size() > 0
		result02.stderr == ""
		new File("target/commandutil/test02.txt").readLines()[0].trim() == "test02:bbb"

		when:
		CommandResult result03 = CommandUtil.execute(["cscript.exe", "..\\..\\src\\test\\resources\\scripts\\test03.js", "ccc"], true)
		println result03.stdout
		then:
		result03.exitCode == 0
		result03.stdout.size() > 0
		result03.stderr == ""
		new File("target/commandutil/test03.txt").readLines()[0].trim() == "test03:ccc"

		cleanup:
		Config.put("abc", null)
	}
	
	def "executeは指定されたコマンドの実行失敗に戻り値に失敗情報を格納する"() {
		when:
		CommandResult result01 = CommandUtil.execute(["testxxx.bat"], true)
		println result01.stderr
		then:
		result01.exitCode == 1
		result01.stdout == ""
		result01.stderr.size() > 0
	}

	def "executeは子プロセスの標準出力および標準エラー出力を呼び出し元の標準出力および標準エラー出力につなげることができる"() {
		setup:
		def err = System.err
		System.err = new PrintStream(new ByteArrayOutputStream(1024))
		def out = System.out
		System.out = new PrintStream(new ByteArrayOutputStream(1024))

		when:
		CommandResult result01 = CommandUtil.execute(["testxxx.bat"], false)
		then:
		result01.exitCode == 1
		result01.stdout == null
		result01.stderr == null
		String errText = System.err.out.toString("Shift_JIS")
		errText.size() > 0

		when:
		CommandResult result02 = CommandUtil.execute(["..\\..\\src\\test\\resources\\scripts\\test01.bat", "aaa"], false)
		then:
		result02.exitCode == 0
		result02.stdout == null
		result02.stderr == null
		String outText = System.out.out.toString("Shift_JIS")
		outText.size() > 0

		cleanup:
		System.err = err
		System.out = out
		println "err:" + errText
		println "out:" + outText
	}
	
	def "replaceVariablesは変数の置換を行う"() {
		setup:
		Config.put("abc", "123")
		Config.put("de", "45")
		Config.put("f", "6")
		assert System.getenv("SYSTEMDRIVE") == "C:"
		
		when:
		def command = ["\${abc}", "x\${abc}", "x\${abc}x", "x\${abc}x\${abc}", 
						"x\${abc}x\${de}x", "x\${abc}\${de}x", "\${abc}\${de}\${f}", 
						"\${x}", "a\${x}b",
						"\${abc}\${abc", "\${}", "\${SYSTEMDRIVE}"]
		CommandUtil.replaceVariables(command)
		then:
		command == ["123", "x123", "x123x", "x123x123",
					"x123x45x", "x12345x", "123456",
					"", "ab",
					"123\${abc", "", "C:"]
		cleanup:
		Config.put("abc", null)
		Config.put("de", null)
		Config.put("f", null)
	}
}
