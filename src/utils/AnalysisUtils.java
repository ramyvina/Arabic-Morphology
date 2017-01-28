package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import constants.Feature;

public class AnalysisUtils {
	
	//Copying analysis
	public static Map<Feature, String> copyAnalysis(Map<Feature, String> inAnalysis){
		if(inAnalysis == null){
			return null;
		}
		Map<Feature, String> outAnalysis = new HashMap<Feature, String>();
		for(Entry<Feature, String> entry : inAnalysis.entrySet()){
			outAnalysis.put(entry.getKey(), entry.getValue());
		}
		return outAnalysis;
	}
	
	//Checking analysis equality
	public static boolean checkExactEqual(Map<Feature, String> originalAnalysis, Map<Feature, String> updatedAnalysis){
		if(originalAnalysis == null || updatedAnalysis == null){
			return true;
		}
		return	originalAnalysis.get(Feature.DIAC).equals(updatedAnalysis.get(Feature.DIAC)) &&
				originalAnalysis.get(Feature.LEX).equals(updatedAnalysis.get(Feature.LEX)) &&
				originalAnalysis.get(Feature.BWHASH).equals(updatedAnalysis.get(Feature.BWHASH));
	}
	
	//Checking analysis equality with undiacritization and A normalization 
	public static boolean checkNormUndiacEqual(Map<Feature, String> originalAnalysis, Map<Feature, String> updatedAnalysis){
		if(originalAnalysis == null || updatedAnalysis == null){
			return true;
		}
		return	StringProcessor.normalizeA(StringProcessor.undiacritize(originalAnalysis.get(Feature.DIAC)),false)
					.equals(StringProcessor.normalizeA(StringProcessor.undiacritize(updatedAnalysis.get(Feature.DIAC)),false)) &&
				StringProcessor.normalizeA(StringProcessor.undiacritize(originalAnalysis.get(Feature.LEX)),false)
					.equals(StringProcessor.normalizeA(StringProcessor.undiacritize(updatedAnalysis.get(Feature.LEX)),false)) &&
				StringProcessor.normalizeA(StringProcessor.undiacritize(originalAnalysis.get(Feature.BWHASH)),false)
					.equals(StringProcessor.normalizeA(StringProcessor.undiacritize(updatedAnalysis.get(Feature.BWHASH)),false));
	}

}
