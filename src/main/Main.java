package main;

import handlers.Logger;
import handlers.MadamiraToAnalysesConverter;
import handlers.MadiwanToMagoldConverter;
import handlers.ResourceHolder;

import java.util.List;
import java.util.Map;

import utils.FileUtils;
import constants.Constants;
import constants.Feature;
import constants.Status;

public class Main {
	
	//File variables
	public static String madiwanFile;						//Madiwan Annotations
	public static String madamiraFile;						//Madamira Output
	public static String morphemeDictionaryFile;			//Morpheme Dictionary
	public static String posCombinationFile;				//POS Combinations
	public static String prefixFormToFuncFile;				//Prefix Form-to-Func Map
	public static String stemFormToFuncFile;				//Stem Form-to-Func Map
	public static String suffixFormToFuncFile;				//Suffix Form-to-Func Map
	public static String specialFormToFuncFile;				//Special Form-to-Func Map
	public static String defaultPosFuncFile;				//Default POS-Func Map
	public static String outputPath;						//Output Path
	private static String generateOrthographicMappingsStr;	//Generate Orthographic Mappings?"?
	private static String generateMLETextStr;					//Generate MLE Text
	
	public static List<List<Map<Feature, String>>> madiwanAnalyses; //Final output analysis
	
	public static void main(String[] args){
		try{
			
			//Check the parameters
			if(args.length != 12){
				System.out.println("Number of arguments should be 11:");
				System.out.println("1) Madiwan Annotations");
				System.out.println("2) Madamira Output");
				System.out.println("3) Morpheme Dictionary");
				System.out.println("4) POS Combinations");
				System.out.println("5) Prefix Form-to-Func Map");
				System.out.println("6) Stem Form-to-Func Map");
				System.out.println("7) Suffix Form-to-Func Map");
				System.out.println("8) Special Form-to-Func Map");
				System.out.println("9) Default POS-Func Map");
				System.out.println("10) Output Path");
				System.out.println("11) Generate Orthographic Mappings?");
				System.out.println("12) Generate MLE Text?");
				return;
			}

			//Read the parameters
			madiwanFile = args[0];
			madamiraFile = args[1];
			morphemeDictionaryFile = args[2];
			posCombinationFile = args[3];
			prefixFormToFuncFile = args[4];
			stemFormToFuncFile = args[5];
			suffixFormToFuncFile = args[6];
			specialFormToFuncFile = args[7];
			defaultPosFuncFile = args[8];
			outputPath = args[9];
			generateOrthographicMappingsStr = args[10];
			generateMLETextStr = args[11];
			
			Status checkStatus = Status.SUCCESS;
			
			//Check the input files
			if(	!FileUtils.isValidFile(madiwanFile) ||
				!FileUtils.isValidFile(madamiraFile) ||
				!FileUtils.isValidFile(posCombinationFile) ||
				!FileUtils.isValidFile(morphemeDictionaryFile) ||
				!FileUtils.isValidFile(prefixFormToFuncFile) ||
				!FileUtils.isValidFile(stemFormToFuncFile) ||
				!FileUtils.isValidFile(suffixFormToFuncFile) ||
				!FileUtils.isValidFile(specialFormToFuncFile) ||
				!FileUtils.isValidFile(defaultPosFuncFile)){
				System.out.println("Error checking the input Files!");
				checkStatus = Status.FAIL;
			}
			
			//Check the output file name
			if(!FileUtils.isValidOutputPath(outputPath)){
				System.out.println("Invalid output name!");
				checkStatus = Status.FAIL;
			}
			
			//Check the boolean parameters
			if(!generateOrthographicMappingsStr.toUpperCase().matches(Constants.YES_NO_REGEX) || !generateMLETextStr.toUpperCase().matches(Constants.YES_NO_REGEX)){
				System.out.println("Invalid Yes/No values!");
				checkStatus = Status.FAIL;
			}
			
			//check failed
			if(checkStatus == Status.FAIL){
				return;
			}
			
			//Initialize the resources
			ResourceHolder resourceHolderInstance = ResourceHolder.getInstance();
			if(resourceHolderInstance == null){
				System.out.println("Error reading the input Files!");
				return;
			}
			
			//Reading the Madamira analyses
			System.out.println("Reading the Madamira analyses.................");
			List<List<Map<Feature, String>>> madamiraAnalyses = new MadamiraToAnalysesConverter(madamiraFile).execute();
			
			//Reading and processing the Madiwan analyses
			System.out.println("Reading and processing the Madiwan analyses.................");
			madiwanAnalyses = new MadiwanToMagoldConverter(madiwanFile, madamiraAnalyses, outputPath, 
					generateOrthographicMappingsStr.toUpperCase().equals(Constants.YES), generateMLETextStr.toUpperCase().equals(Constants.YES)).execute();

			//Print the LOG!
			Logger.getInstance().print(outputPath+Constants.FILE_LOG_EXTENSION);
			
			System.out.println("Done!");
			
		}catch(Exception e){
			System.out.println("An unexpected error has occurred!");
		}
	}

}
