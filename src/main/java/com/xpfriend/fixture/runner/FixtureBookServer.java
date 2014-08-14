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
package com.xpfriend.fixture.runner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.xpfriend.junk.ConfigException;
import com.xpfriend.junk.Strings;

/**
 * FixtureBookRunner のサーバ。
 * @author Ototadana
 */
class FixtureBookServer extends FixtureBookRunner {
	private static final String CHARSET = "UTF-8";
	private static final String BOOK = "book";
	private static final String SHEET = "sheet";
	private static final String CASE = "case";
	private static final String EXIT_CODE = "exit";
	private static final String STDOUT = "stdout";
	private static final String STDERR = "stderr";
	
	private static final String SETUP = "setup";
	private static final String SETUP_AND_GET_COMMAND_OPTIONS = "options";
	private static final String VALIDATE_STORAGE = "validates";
	private static final String VALIDATE_EXIT_CODE_AND_STORAGE = "validatee";
	
	private static Server server;
	
	/**
	 * FixtureBookServer を作成する。
	 * @param bookPath Excelファイルのパス。
	 * @param sheetName シート名。
	 * @param testCaseName テストケース名。
	 */
	FixtureBookServer(String bookPath, String sheetName,
			String testCaseName) {
		super(bookPath, sheetName, testCaseName);
	}

	@Override
	public void setup() throws IOException, AssertionError {
		invoke(SETUP);
	}
	
	@Override
	public List<String> setupAndGetCommand() throws IOException, AssertionError {
		String commandLine = invoke(SETUP_AND_GET_COMMAND_OPTIONS);
		return Arrays.asList(commandLine.trim().split("\t"));
	}
	
	@Override
	public void validateStorage() throws IOException, AssertionError {
		invoke(VALIDATE_STORAGE);
	}
	
	@Override
	public void validateCommandResultAndStorage(CommandResult result) throws IOException,
			AssertionError {
		Connection connection = getConnectionInternal(VALIDATE_EXIT_CODE_AND_STORAGE)
				.data(EXIT_CODE, encode(String.valueOf(result.getExitCode())))
				.data(STDOUT, encode(result.getStdout()))
				.data(STDERR, encode(result.getStderr()));
		execute(connection);
	}

	private String invoke(String method) throws IOException, AssertionError {
		Connection connection = getConnectionInternal(method);
		return execute(connection);
	}
	
	private Connection getConnectionInternal(String method) throws IOException {
		return getConnection(method)
				.data(BOOK, encode(bookPath))
				.data(SHEET, encode(sheetName))
				.data(CASE, encode(testCaseName));
	}
	
	private static String execute(Connection connection) throws IOException, AssertionError {
		Response response = connection.execute();
		if(response.statusCode() == HttpServletResponse.SC_NOT_FOUND) {
			throw new AssertionError(response.body());
		} else if(response.statusCode() != HttpServletResponse.SC_OK) {
			throw new ConfigException(response.body());
		}
		return response.body();
	}
	
	private static Connection getConnection(String method) throws IOException {
		return Jsoup.connect("http://localhost:" + Settings.getPort() + "/" + method)
				.method(Method.POST)
				.ignoreHttpErrors(true)
				.ignoreContentType(true);
	}

	/**
	 * FixtureBookServer を起動する。
	 */
	public static void start() throws Exception {
		server = new Server(Settings.getPort());
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("text/plain;charset=utf-8");
				response.setCharacterEncoding(CHARSET);
				FixtureBookRunner runner = FixtureBookRunner.getInstance(false, 
								decode(baseRequest.getParameter(BOOK)), 
								decode(baseRequest.getParameter(SHEET)), 
								decode(baseRequest.getParameter(CASE)));
				try {
					String text = execute(baseRequest.getPathInfo(), runner, baseRequest);
					if(!Strings.isEmpty(text)) {
						response.getWriter().println(text);
					}
					response.setStatus(HttpServletResponse.SC_OK);
				} catch(AssertionError e) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					e.printStackTrace(response.getWriter());
				} catch(Exception e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					e.printStackTrace(response.getWriter());
				} finally {
					baseRequest.setHandled(true);
				}
			}
			
			private String execute(String path, FixtureBookRunner runner, Request baseRequest) 
					throws IOException, AssertionError {
				if(("/" + SETUP).equals(path)) {
					runner.setup();
				} else if(("/" + SETUP_AND_GET_COMMAND_OPTIONS).equals(path)) {
					return toString(runner.setupAndGetCommand());
				} else if(("/" + VALIDATE_EXIT_CODE_AND_STORAGE).equals(path)) {
					CommandResult result = new CommandResult();
					result.setExitCode(Integer.parseInt(decode(baseRequest.getParameter(EXIT_CODE))));
					result.setStdout(decode(baseRequest.getParameter(STDOUT)));
					result.setStderr(decode(baseRequest.getParameter(STDERR)));
					runner.validateCommandResultAndStorage(result);
				} else { //if(("/" + VALIDATE_STORAGE).equals(path)) {
					runner.validateStorage();
				}
				return null;
			}

			private String toString(List<String> strings) {
				StringBuilder sb = new StringBuilder();
				for(String string : strings) {
					if(sb.length() > 0) {
						sb.append("\t");
					}
					sb.append(string);
				}
				return sb.toString();
			}
		});
		server.start();
	}
	
	private static String encode(String text) throws UnsupportedEncodingException {
		if(Strings.isEmpty(text)) {
			return text;
		} else {
			return URLEncoder.encode(text, CHARSET);
		}
	}
	private static String decode(String text) throws UnsupportedEncodingException {
		if(Strings.isEmpty(text)) {
			return text;
		} else {
			return URLDecoder.decode(text, CHARSET);
		}
	}

	public static void stop() throws Exception {
		server.stop();
	}
}
