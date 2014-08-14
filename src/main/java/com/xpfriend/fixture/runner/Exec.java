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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xpfriend.junk.ConfigException;
import com.xpfriend.junk.Loggi;

/**
 * 一括テスト実行コマンド。
 * @author Ototadana
 */
class Exec {
	
	/**
	 * 指定したディレクトリの配下にあるテスト実行ファイルをすべて実行する。
	 * @param args コマンド引数。 -debug および実行ディレクトリが指定できる。
	 */
	public static void main(String[] args) throws Throwable {
		String directory = ".";
		for(String arg : args) {
			if(CLIBase.DEBUG_OPTION.equals(arg)) {
				DebugUtil.setDebugEnabled();
			} else {
				directory = arg;
			}
		}
		new Exec(directory).run();
	}
	
	private Throwable throwable;
	private ExecutableFilesDirectory directory;
	
	private Exec(String directory) {
		this.directory = new ExecutableFilesDirectory(directory);
	}

	private void run() throws Throwable {
		throwable = null;
		FixtureBookServer.start();
		try {
			for (Map.Entry<String, File> executableFile : directory
					.getExecutableFiles().entrySet()) {
				try {
					execute(executableFile.getKey(), executableFile.getValue());
				} catch(Throwable t) {
					throwable = t;
				}
			}
			if(throwable != null) {
				throw throwable;
			}
		} finally {
			FixtureBookServer.stop();
		}
	}

	private void execute(String className, File executableFile) throws Throwable {
		Loggi.debug("execute: " + executableFile.getAbsolutePath());
		deleteReportFile(className);
		if(PathUtil.isExcelFile(executableFile.getName())) {
			executeByFixtureBookRunner(executableFile, className);
		} else {
			executeAsExecutable(executableFile, className);
		}
	}

	private void deleteReportFile(String className) {
		if(!Report.delete(className)) {
			throw new ConfigException(
					"M_FixtureBookRunner_Exec_CannotDeleteFile",
					Report.getFile(className).getAbsolutePath());
		}
	}

	private void executeByFixtureBookRunner(File executableFile, String className) throws Throwable {
		Expect.main(new String[]{executableFile.getAbsolutePath(), CLIBase.CLASS_OPTION, className});
	}
	
	private void executeAsExecutable(File executableFile, String className) throws Exception {
		String suiteName = executableFile.getName();
		Report report = new Report(suiteName, getTestCaseName(suiteName), className);
		report.setSummary(true);
		try {
			String executable = executableFile.getAbsolutePath();
			CommandResult result = CommandUtil.execute(createCommand(executable, className), false);
			if(result.getExitCode() != 0) {
				throw FormatUtil.createAssertionError(
						"M_FixtureBookRunner_Exec_TestFailed",
						executable, result.getExitCode());
			}
			report.write(null);
		} catch(Exception e) {
			report.write(e);
			throw e;
		} catch(Error e) {
			report.write(e);
			throw e;
		}
	}

	private String getTestCaseName(String suiteName) {
		return FormatUtil.format("M_FixtureBookRunner_Exec_TestCaseName", suiteName);
	}

	private List<String> createCommand(String executable, String className) {
		List<String> command = new ArrayList<String>(2);
		command.add(executable);
		if(DebugUtil.isDebugEnabled()) {
			command.add(CLIBase.DEBUG_OPTION);
		}
		command.add(CLIBase.SERVER_OPTION);
		command.add(CLIBase.CLASS_OPTION);
		command.add(className);
		return command;
	}
}
