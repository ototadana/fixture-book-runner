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
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.xpfriend.junk.ConfigException;
import com.xpfriend.junk.Strings;

/**
 * 実行可能なファイルを格納するディレクトリ。
 * @author Ototadana
 */
class ExecutableFilesDirectory {
	private static class FileComparator implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			int pathCountDiff = a.split("\\.").length - b.split("\\.").length;
			if(pathCountDiff != 0) {
				return pathCountDiff;
			}
			return a.compareToIgnoreCase(b);
		}
	}
	
	private static final String[] IGNORABLES = {"~", "."};
	private Map<String, File> executableFiles;

	/**
	 * ExecutableFilesDirectory を作成する。
	 * @param path　実行可能なファイルを格納するディレクトリのパス。
	 */
	public ExecutableFilesDirectory(String path) {
		File directory = new File(path);
		if(!directory.isDirectory()) {
			throw new ConfigException(
					"M_FixtureBookRunner_ExecutableFilesDirectory_Not_Directory",
					directory.getAbsolutePath());
		}
		this.executableFiles = getExecutableFiles(directory.getAbsoluteFile());
	}

	/**
	 * 実行可能ファイルのコレクションを取得する。
	 * @return 実行可能なファイルのコレクション。
	 */
	public Map<String, File> getExecutableFiles() {
		return executableFiles;
	}
	
	private static Map<String, File> getExecutableFiles(File directory) {
		Map<String, File> executableFiles = new TreeMap<String, File>(new FileComparator());
		add("", directory, executableFiles);
		return executableFiles;
	}
	
	private static void add(String prefix, File directory, Map<String, File> executableFiles) {
		File[] files = directory.listFiles();
		for(File file : files) {
			if(isIgnorable(file)) {
				continue;
			}
			file = file.getAbsoluteFile();
			String baseName = toBaseName(prefix, file);
			if(isExecutable(baseName, file)) {
				add(executableFiles, baseName, file);
			} else if(file.isDirectory()) {
				add(concat(prefix, file.getName()), file, executableFiles);
			}
		}
	}
	
	private static boolean isIgnorable(File file) {
		for(String ignorable : IGNORABLES) {
			if(file.getName().startsWith(ignorable)) {
				return true;
			}
		}
		return false;
	}
	
	private static String toBaseName(String prefix, File file) {
		return concat(prefix, PathUtil.toBaseName(file));
	}
	
	private static String concat(String prefix, String suffix) {
		if(Strings.isEmpty(prefix)) {
			return suffix;
		} else {
			return prefix + "." + suffix;
		}
	}

	private static void add(Map<String, File> executableFiles, String baseName,
			File file) {
		File oldFile = executableFiles.get(baseName);
		if(oldFile == null) {
			executableFiles.put(baseName, file);
			return;
		}

		if(PathUtil.isExcelFile(oldFile.getName())) {
			executableFiles.put(baseName, file);
			return;
		}
	}

	private static boolean isExecutable(String baseName, File file) {
		if(!file.isFile()) {
			return false;
		}

		return PathUtil.isExecutableFile(file.getName());
	}
}
