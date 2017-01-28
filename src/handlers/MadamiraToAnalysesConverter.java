package handlers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.StringProcessor;
import constants.Constants;
import constants.Feature;

public class MadamiraToAnalysesConverter {
		
	private String madamiraFile; //Input Madamira file
	private List<List<Map<Feature, String>>> analyses; //Output analyses
	
	//Constructor
	public MadamiraToAnalysesConverter(String madamiraFile){
		this.madamiraFile = madamiraFile;
	}
	
	//Converting a Madamira file into analyses. 
	public List<List<Map<Feature, String>>> execute(){
		
		Logger.getInstance().append("Reading the MADAMIRA file!");
        
		analyses = new ArrayList<List<Map<Feature,String>>>();
		
		try{
			
			//Iteration variables
			String currentWord = "";
			boolean newWordExpected = true;
			Map<Feature, String> currentAnalysis = new HashMap<Feature, String>();
	        List<Map<Feature, String>> currentSentenceAnalyses = new ArrayList<Map<Feature,String>>();
	        
	        //Read the file line by line
			InputStream is = new FileInputStream(madamiraFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String line;
	        while ((line = br.readLine()) != null){
	        	
	        	//Ignore empty lines
	        	if(line.equals(""))
	        		continue;
	        	
	        	//End of word? Record the current analysis.
	        	if(line.equals(Constants.MADAMIRA_WORD_BREAK)){
					if(!newWordExpected){
						currentSentenceAnalyses.add(currentAnalysis);
						currentAnalysis = new HashMap<Feature, String>();
					}					
					newWordExpected = true;
				
				//Word line? Normalize and undiacritize
	        	}else if(line.startsWith(Constants.MADAMIRA_WORD_PREFIX)){
					currentAnalysis = new HashMap<Feature, String>();
					currentWord = line.replace(Constants.MADAMIRA_WORD_PREFIX, "").trim();
					//Normalize and Undiacritize
					currentWord = StringProcessor.normalizeAYP(StringProcessor.undiacritize(currentWord), true);
					newWordExpected = false;
				
				//Analysis line? Read it into features
	        	}else if(line.matches(Constants.MADAMIRA_ANALYSIS_REGEX)){
					currentAnalysis.put(Feature.DIWAN, line);
					for (Feature feature : Feature.values()){
						currentAnalysis.put(feature, getFeatureValue(line, feature));
					}

				//End of sentence? Record the current sentence analyses.
				}else if(line.startsWith(Constants.MADAMIRA_SENTENCE_BREAK)){
					if(currentSentenceAnalyses.size()>0)
						analyses.add(currentSentenceAnalyses);
					currentSentenceAnalyses = new ArrayList<Map<Feature,String>>();
				}
			}

	        //Close the readers
	        is.close();
	        br.close();
	        
	        System.out.println("Madamira analyses have been read successfully!");
	        
		}catch(Exception e){
			System.out.println("Error reading the Madamira analyses!");
			Logger.getInstance().append("Error reading the Madamira analyses!");
		}
		
		return analyses;
	}
	
	//Reading the value of a specific feature from an analysis line
	private String getFeatureValue(String analysisLine, Feature feature){
		
		//Score Feature
		if(feature == Feature.SCORE)
			return analysisLine.split("\\s")[0].substring(1);
		
		//UNDEF Case
		if(!analysisLine.contains(feature.name().toLowerCase()))
			return Constants.UNDEF;
		
		//Read the Feature Value
		String featureValue = analysisLine.replaceAll("^.*"+feature.name().toLowerCase()+":(\\S+).*$", "$1");
		return featureValue;
	}

	//Getters
	
	public List<List<Map<Feature, String>>> getAnalyses() {
		return analyses;
	}
	
}
