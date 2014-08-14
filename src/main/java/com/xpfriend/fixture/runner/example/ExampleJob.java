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
package com.xpfriend.fixture.runner.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * テスト対象サンプルジョブ。
 * @author Ototadana
 */
class ExampleJob {

	private static final String SQL = "update TEST_FB set NAME = ? where ID = ?";
	
	/**
	 * データベーステーブル TEST_FB の更新処理を行う。
	 * @param args 1:更新対象レコードのID、2:更新対象レコードに設定するNAME項目の値。
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			throw new IllegalArgumentException("args.length == " + args.length);
		}
		int id = Integer.parseInt(args[0]);
		String name = args[1];

		Connection connection = getConnection();
		try {
			updateDatabase(id, name, connection);
			connection.commit();
		} finally {
			connection.close();
		}
	}

	/**
	 * データベーステーブルの更新を行う
	 * @param id 更新対象列のID。
	 * @param name　更新対象列のNAME値。
	 * @param connection データベースコネクション。
	 */
	private static void updateDatabase(int id, String name, Connection connection)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL);
		try {
			statement.setString(1, name);
			statement.setInt(2, id);
			statement.executeUpdate();
		} finally {
			statement.close();
		}
	}

	/**
	 * データベースコネクションを取得する。
	 * @return データベースコネクション。
	 */
	private static Connection getConnection() throws Exception, IOException,
			SQLException {
		DataSource ds = BasicDataSourceFactory.createDataSource(getProperties());
		return ds.getConnection();
	}

	/**
	 * db.properties ファイルからデータベースコネクションの設定を取得する。
	 * @return データベースコネクション設定。
	 */
	private static Properties getProperties() throws IOException {
		Properties props = new Properties();
		InputStream is = ClassLoader.getSystemResourceAsStream("db.properties");
		try {
			props.load(is);
		} finally {
			is.close();
		}
		return props;
	}
}
