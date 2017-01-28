package handlers;

import java.util.Map;

import utils.StringProcessor;

import constants.Constants;
import constants.Feature;
import constants.Status;

public class FunctionalFeatureHandler {
	
	public static Status execute(Map<Feature, String> analysis, boolean functionalNumber, boolean functionalGender){

		try{
			//Read the features
			String lemma = analysis.get(Feature.LEX).replaceAll("^(.+)[\\-\\_].*$", "$1");
			String normalizedLemma = StringProcessor.undiacritize(StringProcessor.normalizeA(lemma, false));
			String stemBW = analysis.get(Feature.BWHASH).replaceAll("^.*\\#(.*)\\#.*$", "$1").replaceAll("\\/.*?(\\+|$)", "$1").replace("+", "");
			String normalizedStem = StringProcessor.normalizeA(stemBW, false);
			String pos = analysis.get(Feature.POS);
			
			//Generate the functional number
			if(functionalNumber){
				//Handle the noun/adjective case
				if(pos.matches("noun|adj") && 
					(StringProcessor.getRoot(normalizedLemma).replaceAll("([^\\d])\\1", "$1").equals(StringProcessor.getRoot(normalizedStem).replaceAll("([^\\d])\\1", "$1")))){
					String number = analysis.get(Feature.NUM);
					String fNum = Constants.FEATURE_NUMBER_SINGULAR;
					if(number.equals(Constants.FEATURE_NUMBER_NA))
						fNum = Constants.FEATURE_NUMBER_NA;
					else if(number.equals(Constants.FEATURE_NUMBER_DUAL) || number.equals(Constants.FEATURE_NUMBER_PLURAL))
						fNum = number;
					else if(!normalizedLemma.contains(normalizedStem))
						fNum = Constants.FEATURE_NUMBER_PLURAL;
					analysis.put(Feature.FNUM, fNum);
				//Any other POS type
				}else{
					analysis.put(Feature.FNUM, analysis.get(Feature.NUM));
				}
			}
				
			//Generate the functional gender
			if(functionalGender){
				//Handle the noun/adjective case
				if(pos.matches("noun|adj") && 
					(StringProcessor.getRoot(normalizedLemma).replaceAll("([^\\d])\\1", "$1").equals(StringProcessor.getRoot(normalizedStem).replaceAll("([^\\d])\\1", "$1")))){
					String gender = analysis.get(Feature.GEN);
					String fGen = Constants.FEATURE_GENDER_MASCULIN;
					if(gender.equals(Constants.FEATURE_GENDER_NA))
						fGen = Constants.FEATURE_GENDER_NA;
					else if(gender.equals(Constants.FEATURE_GENDER_FEMININ))
						fGen = gender;
					else if(normalizedLemma.startsWith("A") && !normalizedStem.startsWith("A") && (normalizedStem.endsWith("A'") || normalizedStem.endsWith("A")))
						fGen = Constants.FEATURE_GENDER_FEMININ;
					analysis.put(Feature.FGEN, fGen);
				//Any other POS type
				}else{
					analysis.put(Feature.FGEN, analysis.get(Feature.GEN));
				}
			}
		
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}

}
