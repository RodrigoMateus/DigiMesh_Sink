package com.maykot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogRecord {

	static BufferedWriter bufferedWriter;

	public static void insertLog(String name, String logEntry) {
		File file = new File(name + ".txt");

		try {
			FileWriter fileWriter = new FileWriter(file, true);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bufferedWriter.append(logEntry);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			// bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
