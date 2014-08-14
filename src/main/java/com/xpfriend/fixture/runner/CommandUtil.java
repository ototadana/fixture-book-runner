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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.xpfriend.junk.Config;
import com.xpfriend.junk.ExceptionHandler;
import com.xpfriend.junk.Loggi;
import com.xpfriend.junk.Strings;

/**
 * コマンド実行ユーティリティ。
 * @author Ototadana
 */
class CommandUtil {

	/**
	 * 指定されたコマンドを実行する。
	 * @param command コマンド文字列。
	 * @param preserveOutput 標準出力、標準エラー出力を保存するかどうか。
	 * @return 実行したコマンドの結果。
	 */
	public static CommandResult execute(List<String> command, boolean preserveOutput) throws Exception {
		replaceVariables(command);
		command = addExecutableForBatchFile(command);
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		String workingDirectory = Settings.getWorkingDirectory();
		if(!Strings.isEmpty(workingDirectory)) {
			processBuilder.directory(new File(workingDirectory).getAbsoluteFile());
		}

		Loggi.debug("CMD: " + processBuilder.command());
		Loggi.debug("CWD: " + getAbsolutePath(processBuilder.directory()));
		Loggi.debug("ENV: " + processBuilder.environment());
		Process process = processBuilder.start();
		ExecutorService service = Executors.newFixedThreadPool(2);
		try {
			final StreamReader stdoutReader = new StreamReader(process
					.getInputStream(), System.out, preserveOutput);
			final StreamReader stderrReader = new StreamReader(process
					.getErrorStream(), System.err, preserveOutput);
			final Future<String> stdout = service.submit(stdoutReader);
			final Future<String> stderr = service.submit(stderrReader);
			final CommandResult result = new CommandResult();
			result.setExitCode(process.waitFor());
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						ExceptionHandler.ignore(e);
					}
					stderr.cancel(true);
					stdout.cancel(true);
				}
			}).start();
			result.setStderr(getResult(stderr, stderrReader));
			result.setStdout(getResult(stdout, stdoutReader));
			return result;
		} finally {
			service.shutdown();
		}
	}

	private static void replaceVariables(List<String> command) {
		for(int i = 0; i < command.size(); i++) {
			command.set(i, replaceVariable(command.get(i)));
		}
	}

	private static String replaceVariable(String text) {
		if(text == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		int from = 0;
		while((from = replaceVariable(from, text, sb)) < text.length());
		return sb.toString();
	}
	
	private static int replaceVariable(int from, String text, StringBuilder sb) {
		int start = text.indexOf("${", from);
		if(start < 0) {
			sb.append(text.substring(from));
			return text.length();
		}
		int end = text.indexOf("}", start + 2);
		if(end < 0) {
			sb.append(text.substring(from));
			return text.length();
		}
		String variable = text.substring(start + 2, end);
		sb.append(text.substring(from, start));
		sb.append(getValue(variable));
		return end + 1;
	}

	private static String getValue(String variable) {
		String value = Config.get(variable, null);
		if(value == null) {
			value = System.getenv(variable);
		}
		if(value == null) {
			value = "";
		}
		return value;
	}

	private static String getResult(final Future<String> future, StreamReader reader)
			throws InterruptedException, ExecutionException, UnsupportedEncodingException {
		try {
			return future.get();
		} catch(CancellationException e) {
			return reader.getText();
		}
	}

	private static String getAbsolutePath(File directory) {
		if(directory == null) {
			return null;
		}
		return directory.getAbsolutePath();
	}

	private static List<String> addExecutableForBatchFile(List<String> command) {
		String name = command.get(0).toLowerCase();
		if(name.endsWith(".bat") || name.endsWith(".cmd")) {
			List<String> newCommand = new ArrayList<String>(command.size() + 2);
			newCommand.add("cmd.exe");
			newCommand.add("/C");
			newCommand.addAll(command);
			return newCommand;
		}
		return command;
	}

	private static class StreamReader implements Callable<String> {
		private InputStream input;
		private OutputStreamForPreserve output;

		public StreamReader(InputStream is, OutputStream os, boolean preserve) {
			input = is;
			output = new OutputStreamForPreserve(preserve, os);
		}
		
		@Override
		public String call() throws Exception {
			byte[] buffer = new byte[8192];
			while(true) {
				while(input.available() <= 0) {
					Thread.sleep(100);
				}
				int n = 0;
				if((n = input.read(buffer)) > 0) {
					output.write(buffer, 0, n);
				} else if(n < 0) {
					break;
				}
			}
			return getText();
		}
		
		public String getText() throws UnsupportedEncodingException {
			return output.getText();
		}
	}
	
	private static class OutputStreamForPreserve extends FilterOutputStream {
		ByteArrayOutputStream byteArray;
		
		public OutputStreamForPreserve(boolean preserve, OutputStream out) {
			super(out);
			if(preserve) {
				byteArray = new ByteArrayOutputStream(1024);
			}
		}
		
		@Override
		public void write(int b) throws IOException {
			super.write(b);
			if(byteArray != null) {
				byteArray.write(b);
			}
		}
				
		public String getText() throws UnsupportedEncodingException {
			if(byteArray == null) {
				return null;
			} else {
				return byteArray.toString(System.getProperty("file.encoding"));
			}
		}
	}
}
