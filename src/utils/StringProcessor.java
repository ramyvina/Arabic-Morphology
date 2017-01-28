package utils;

import constants.Constants;

public class StringProcessor {
	
	public static String normalizeA(String word, boolean start){
		if(word.matches("^[\\{\\<\\>\\|]+$"))
			return word;
		if(start)
			return word.replaceAll("^[\\<\\>\\|\\{]", "A").replaceAll("_", "");
		else
			return word.replaceAll("[\\<\\>\\|\\{]", "A").replaceAll("_", "");
	}
	
	public static String normalizeAYP(String word, boolean removeUnderScore){
		if(word.matches("^[\\{\\<\\>\\|]+$"))
			return word;
		word = word.replaceAll("[\\<\\>\\|\\{]", "A").replaceAll("Y", "y").replaceAll("p", "h");
		if(removeUnderScore && !word.equals("_"))
			return word.replaceAll("_", "");
		else
			return word;
	}
	
	public static String undiacritize(String word){
		String str = word.replace("null", "XYZ");
		str = str.replaceAll("[aiuFKNo`~]", "");
		str = str.replace("XYZ", "null");
		if(str.length()==0)
			return word;
		return str;
	}
	
	public static String undiacritizeAndKeepEmpty(String word){
		word = word.replace("null", "XYZ");
		word = word.replaceAll("[aiuFKNo`~]", "");
		word = word.replace("XYZ", "null");
		return word;
	}
	
	public static String getRoot(String word){
		word = word.replaceAll("[AwyY\\'\\&\\}p]", "");
		return word;
	}
	
	public static boolean isPunc(String word) {
		return word.matches(Constants.PUNC_REGEX);
	}
	
	public static boolean isNumeric(String word) {
		return word.matches(Constants.DIGIT_REGEX);
	}
	
	public static boolean isForeign(String word) {
		return word.matches(Constants.FOREIGN_REGEX);
	}
	
	public static boolean isArabic(String word) {
		return word.matches(Constants.ARABIC_REGEX);
	}

}
