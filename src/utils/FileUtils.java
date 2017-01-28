package utils;

import java.io.File;

import constants.Constants;

public class FileUtils {
	
	public static boolean isValidFile(String filePath){
		try{
			File file = new File(filePath);
			return file.isFile();
		}catch(Exception e){
			return false;
		}
	}
	
	public static boolean isValidOutputPath(String outputPath){
		try{
			if(!outputPath.matches("^(.+)[\\/\\\\]([^\\/\\\\]+$)"))
				return false;
			File directory = new File(outputPath.replaceAll("^(.+)[\\/\\\\]([^\\/\\\\]+$)", "$1"));
			String fileName = outputPath.replaceAll("^(.+)[\\/\\\\]([^\\/\\\\]+$)", "$2");
			return directory.isDirectory() && !fileName.matches(Constants.FILE_INVALID_NAME_REGEX);
		}catch(Exception e){
			return false;
		}
	}
	
	public static boolean isValidDirectory(String dirPath){
		try{
			File file = new File(dirPath);
			return file.isDirectory();
		}catch(Exception e){
			return false;
		}
	}

}
