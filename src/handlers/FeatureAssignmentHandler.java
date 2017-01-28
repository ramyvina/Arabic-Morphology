package handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import constants.Feature;
import constants.Status;

public class FeatureAssignmentHandler {
	
	//Feature assignment using Form-to-Func analysiss
	public static Status execute(Map<Feature, String> analysis){
		try{
			//Read morphological information
			String originalMorphemicPos = analysis.get(Feature.BWHASH);
			String morphemicPos = originalMorphemicPos.replace("#", "");
			String prefixMorphemicPos = originalMorphemicPos.replaceAll("\\#.*\\#.*$", "");
			String stemMorphemicPos = originalMorphemicPos.replaceAll("^.*\\#(.*)\\#.*$", "$1");
			String suffixMorphemicPos = originalMorphemicPos.replaceAll("^.*\\#.*\\#", "");
			String pos = morphemicPos.replaceAll("[^\\+]*?\\/", "");
			String prefixPos = prefixMorphemicPos.replaceAll("[^\\+]*?\\/", "");
			String stemPos = stemMorphemicPos.replaceAll("[^\\+]*?\\/", "");
			String suffixPos = suffixMorphemicPos.replaceAll("[^\\+]*?\\/", "");
			
			boolean successful = false;
	
			//STEM
			successful = successful || setFeatures(analysis, ResourceHolder.getInstance().getStemFormToFuncMap(), stemMorphemicPos, stemPos);
			
			//DEFAULT POS
			if(ResourceHolder.getInstance().getDefaultPosFuncMap().containsKey("pos:"+analysis.get(Feature.POS))){
				setFeatures(analysis, ResourceHolder.getInstance().getDefaultPosFuncMap().get("pos:"+analysis.get(Feature.POS)));	
			} else {
				setFeatures(analysis, ResourceHolder.getInstance().getDefaultPosFuncMap().get("pos:*"));	
			}
			
			//PREFIX
			successful = successful || setFeatures(analysis, ResourceHolder.getInstance().getPrefixFormToFuncMap(), prefixMorphemicPos, prefixPos);
			//SUFFIX
			successful = successful || setFeatures(analysis, ResourceHolder.getInstance().getSuffixFormToFuncMap(), suffixMorphemicPos, suffixPos);
			//SPECIAL
			successful = successful || setFeatures(analysis, ResourceHolder.getInstance().getSpecialFormToFuncMap(), morphemicPos, pos);
			
			if(!successful){
				Logger.getInstance().append(analysis, "Could not find corresponding features in the Form-to-Func files.");
				return Status.FAIL;
			}
			
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}

	//Setting the features given A Form-to-Func analysis
	private static boolean setFeatures(Map<Feature, String> analysis, Map<String, String> featureMap, String morphemicPos, String pos){
		Map<String, String> selectedEntries = new HashMap<>();
		for(Entry<String, String> entry1 : featureMap.entrySet()){
			if(morphemicPos.contains(entry1.getKey()) || pos.contains(entry1.getKey())){
				boolean valid = true;
				for(Entry<String, String> entry2 : featureMap.entrySet()){
					if(	!entry2.getKey().equals(entry1.getKey()) && 
						entry2.getKey().contains(entry1.getKey()) && 
						(morphemicPos.contains(entry2.getKey()) || pos.contains(entry2.getKey()))){
						valid = false;
						break;
					}
				}
				if(valid){ //a match found
					selectedEntries.put(entry1.getKey(), entry1.getValue());
				}
			}
		}
		for(Entry<String, String> entry : selectedEntries.entrySet()){
			setFeatures(analysis, entry.getValue());	
		}
		return selectedEntries.size() > 0;
	}
	
	//Setting a feature value given the feature analysis and feature string
	private static void setFeatures(Map<Feature, String> analysis, String featureLine){
		String[] features = featureLine.trim().split("\\s+");
		for(int i=0; i<features.length; i++){
			String featureName = features[i].split("\\:")[0];
			String featureValue = features[i].split("\\:")[1];
			analysis.put(Feature.valueOf(featureName.toUpperCase()), featureValue);
		}
	}

}
