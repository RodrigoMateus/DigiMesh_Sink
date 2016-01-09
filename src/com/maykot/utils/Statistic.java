package com.maykot.utils;

public class Statistic {

	static int countOK = 0;
	static int countBadPack = 0;
	static int countNoModem = 0;

	public static int getCountOK() {
		return countOK;
	}

	public static void incrementCountOK() {
		countOK++;
	}

	public static int getCountBadPack() {
		return countBadPack;
	}

	public static void incrementCountBadPack() {
		countBadPack++;
	}

	public static int getCountNoModem() {
		return countNoModem;
	}

	public static void incrementCountNoModem() {
		countNoModem++;
	}

}
