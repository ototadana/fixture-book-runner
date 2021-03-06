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
package com.xpfriend.fixture.runner.command

import com.xpfriend.fixture.runner.TestUtil;

import groovy.sql.Sql;

/**
 * テスト対象プログラム　その1。
 * 
 * @author Ototadana
 */
class Test01 {
	public static void main(String[] args) {
		def sql = TestUtil.createSql()
		if(args.length == 0) {
			throw new IllegalArgumentException("Invalid ID")
		}
		sql.execute("DELETE FROM EMPLOYEE WHERE ID = ?", [args[0]])
		System.out.println("OK@systemout1")
		System.out.println("OK@systemout2")
		System.err.println("OK@systemerr1")
		System.err.println("OK@systemerr2")
	}
}
