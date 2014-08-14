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
package com.xpfriend.fixture.runner.example

import com.xpfriend.fixture.runner.Setup;
import com.xpfriend.fixture.runner.TestUtil;
import com.xpfriend.fixture.runner.ValidateStorage;

import spock.lang.Specification;

/**
 * @author Ototadana
 *
 */
class ExampleJobTest extends Specification {

	def setupSpec() {
		TestUtil.setupSpec()
	}
	
	def cleanupSpec() {
		TestUtil.cleanupSpec()
	}
	
	def "実行時引数が二つない場合はエラーになる"() {
		when:
		ExampleJob.main(["x"] as String[])
		then:
		IllegalArgumentException e = thrown()
		println e
	}

	def "指定されたIDのNAME項目の値を更新することができる"() {
		setup:
		Setup.main(["src/test/resources/books/ExampleJobTest.xlsx", "main", "指定されたIDのNAME項目の値を更新することができる"] as String[])
		when:
		ExampleJob.main(["1", "xxx"] as String[])
		then:
		ValidateStorage.main(["src/test/resources/books/ExampleJobTest.xlsx", "main", "指定されたIDのNAME項目の値を更新することができる"] as String[])
	}
}
