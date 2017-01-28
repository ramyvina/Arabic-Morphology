package handlers;

import java.util.Map;

import utils.StringProcessor;
import constants.Constants;
import constants.Feature;
import constants.Status;

public class TrivialCaseHandler {
	
	//Adjusting the values inside a trivial-case analysis
	public static Status execute(Map<Feature, String> analysis){
		try{

			String inputString = analysis.get(Feature.INPUT_STRING);
			
			//Handle foreign words marked as "@@LAT@@"
			if(inputString.startsWith(Constants.LATIN_MARKER)){
				assignFeatures(analysis, Constants.POS_BW_FOREIGN, "noun", "foreign");
			//Handle foreign words
			} else if(StringProcessor.isForeign(inputString)){
				assignFeatures(analysis, Constants.POS_BW_FOREIGN, "noun", "foreign");
			//Handle punctuation marks
			} else if(StringProcessor.isPunc(inputString)){
				assignFeatures(analysis, Constants.POS_BW_PUNC, "punc", "punc");
			//Handle numerical words	
			} else if(StringProcessor.isNumeric(inputString)){
				assignFeatures(analysis, Constants.POS_BW_DIGIT, "digit", "digit");
			}
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}
	
	//Feature assignment
	private static void assignFeatures(Map<Feature, String> analysis, String bwPosTag, String posValue, String source){
		String inputString = analysis.get(Feature.INPUT_STRING);
		
		String diac = "";
		String lemma = "";
		String bwHash = "";
		String gloss = "";
		String pos = "";
		
		//DIAC
		diac = inputString;
		if(!diac.equals(analysis.get(Feature.DIAC))){
			Logger.getInstance().append(analysis, "diac:"+analysis.get(Feature.DIAC)+ " is incorrect and changed to "+diac+".");
			analysis.put(Feature.DIAC, diac);
		}
		
		//LEX
		lemma = inputString+"_0";
		if(!lemma.equals(analysis.get(Feature.LEX))){
			Logger.getInstance().append(analysis, "lex:"+analysis.get(Feature.LEX)+ " is incorrect and changed to "+lemma+".");
			analysis.put(Feature.LEX, lemma);
		}
		
		//BWHASH
		bwHash = "+#"+inputString+"/"+bwPosTag+"#+";
		if(!bwHash.equals(analysis.get(Feature.BWHASH))){
			Logger.getInstance().append(analysis, "bwhash:"+analysis.get(Feature.BWHASH)+ " is incorrect and changed to "+bwHash+".");
			analysis.put(Feature.BWHASH, bwHash);
		}
		
		//GLOSS
		gloss = inputString;
		if(!gloss.equals(analysis.get(Feature.GLOSS))){
			Logger.getInstance().append(analysis, "gloss:"+analysis.get(Feature.GLOSS)+ " is incorrect and changed to "+gloss+".");
			analysis.put(Feature.GLOSS, gloss);
		}
		
		//POS
		if(!pos.equals(analysis.get(Feature.POS))){
			Logger.getInstance().append(analysis, "pos:"+analysis.get(Feature.POS)+ " is incorrect and changed to "+posValue+".");
			analysis.put(Feature.POS, pos);
		}
		
		//All other features
		analysis.put(Feature.STEM, inputString);
		analysis.put(Feature.SOURCE, source);
		analysis.put(Feature.MSA, inputString);
		assignEmptyFeatures(analysis);
	}
	
	//Assigning empty features
	public static void assignEmptyFeatures(Map<Feature, String> analysis){
		analysis.put(Feature.PRC3, "na");
		analysis.put(Feature.PRC2, "na");
		analysis.put(Feature.PRC1, "na");
		analysis.put(Feature.PRC0, "na");
		analysis.put(Feature.PER, "na");
		analysis.put(Feature.ASP, "na");
		analysis.put(Feature.VOX, "na");
		analysis.put(Feature.MOD, "no");
		analysis.put(Feature.GEN, "na");
		analysis.put(Feature.FGEN, "na");
		analysis.put(Feature.NUM, "na");
		analysis.put(Feature.FNUM, "na");
		analysis.put(Feature.STT, "no");
		analysis.put(Feature.CAS, "no");
		analysis.put(Feature.ENC0, "na");
		analysis.put(Feature.ENC1, "na");
		analysis.put(Feature.ENC2, "na");
		analysis.put(Feature.RAT, "na");
		analysis.put(Feature.WORD_TYPE, "DIALECT");
		analysis.put(Feature.REGION, "ALL");
		analysis.put(Feature.SCORE, "1.000100");
	}

}
