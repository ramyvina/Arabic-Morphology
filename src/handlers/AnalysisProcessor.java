package handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import utils.StringProcessor;
import constants.Feature;
import constants.Status;

public class AnalysisProcessor {
	
	
	//Process LEX, DIAC, BWHASH and Feature assignment
	public static Status process(Map<Feature, String> analysis, boolean madamira, boolean functionalNumber, boolean functionalGender){
		try{
			Status lemmaStatus = processLemma(analysis, madamira);
			Status diacBWStatus = processDiacAndBW(analysis, madamira);
			if(lemmaStatus == Status.FAIL || diacBWStatus == Status.FAIL){
				return Status.FAIL;
			}else{
				return processFeatures(analysis, functionalNumber, functionalGender);
			}
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
	}
	
	//LEX Normalization
	private static Status processLemma(Map<Feature, String> analysis, boolean madamira){
		try{
			
			String lemma = analysis.get(Feature.LEX);
			
			if(!madamira){
				//Reject UNDEF and empty values
				if(lemma.equals("UNDEF") || lemma.equals("") || StringProcessor.undiacritizeAndKeepEmpty(lemma).replaceAll("[\\-\\_].*", "").equals("")){
					Logger.getInstance().append(analysis, "lex:"+lemma+" is invalid.");
					return Status.FAIL;
				}
			}

			//Fixes
			if(!lemma.matches(".*\\_\\d$"))
				lemma = lemma+"_1";
			lemma = lemma.replaceAll("\\-[uai]+\\_", "_");
			lemma = lemma.replace("-", "_");
			if(!lemma.equals("{"))
				lemma = lemma.replace("{", "A");
			
			if(!lemma.equals(analysis.get(Feature.LEX))){
				Logger.getInstance().append(analysis, "lex:"+analysis.get(Feature.LEX)+" has changed to "+lemma+".");
			}
			analysis.put(Feature.LEX, lemma);
			return Status.SUCCESS;
			
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
	}
	
	//Checking and Correcting DIAC and BWHASH
	private static Status processDiacAndBW(Map<Feature, String> analysis, boolean madamira){
		
		try{
			
			//Reading the morphological features
			String morphemicPos = analysis.get(Feature.BWHASH); 
			String originalBWHash = morphemicPos;
			String diac = analysis.get(Feature.DIAC); 
			String originalDiac = diac;
			
			Status undefAndEmptyStatus = Status.SUCCESS;
			if(!madamira){
				//Reject UNDEF and empty values
				if(diac.equals("UNDEF") || diac.equals("") || StringProcessor.undiacritizeAndKeepEmpty(diac).equals("")){
					Logger.getInstance().append(analysis, "diac:"+diac+" is invalid.");
					undefAndEmptyStatus = Status.FAIL;
				}
				if(morphemicPos.equals("UNDEF") || morphemicPos.equals("")){
					Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+" is invalid.");
					undefAndEmptyStatus = Status.FAIL;
				}
				if(undefAndEmptyStatus == Status.FAIL){
					return Status.FAIL;
				}

				//Update the analysis in the 'null' case
				if(diac.contains("(null)")){
					Logger.getInstance().append(analysis, "diac:"+originalDiac+ " misspelled '(null)' has ben removed");
					diac = diac.replace("(null)", "");
					analysis.put(Feature.DIAC, diac);
				}
				if(diac.contains("(nll)")){
					Logger.getInstance().append(analysis, "diac:"+originalDiac+ " misspelled '(nll)' has ben removed");
					diac = diac.replace("(nll)", "");
					analysis.put(Feature.DIAC, diac);
				}
				if(morphemicPos.contains("(nll)")){
					Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+ " misspelled '(nll)' has changed to '(null)'");
					morphemicPos = morphemicPos.replace("(nll)", "null");
					analysis.put(Feature.BWHASH, morphemicPos);
					analysis.put(Feature.BW, analysis.get(Feature.BWHASH).replace("#", ""));
				}
			
				//Every part of BW should have a form and a POS separated by "/".
				Status wellFormatedMorphemeStatus = Status.SUCCESS;
				morphemicPos = morphemicPos.replaceAll("^\\+", "").replaceAll("\\+$", "");
				List<String>  morphemicPosSplits = Arrays.asList(morphemicPos.split("[\\+]"));
				for(String part : morphemicPosSplits){
					if(part.split("\\/").length!=2){
						Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+ " does not have a (surface, POS) pair for each morpheme.");
						wellFormatedMorphemeStatus = Status.FAIL;
					}
//					else if(StringProcessor.undiacritizeAndKeepEmpty(part.split("\\/")[0]).equals("")){
//						Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+" has a wrong morpheme that has only diacritics.");
//						wellFormatedMorphemeStatus = Status.FAIL;
//					}
				}
				if(wellFormatedMorphemeStatus == Status.FAIL){
					return Status.FAIL;
				}
			}
			
			//Check the stem marking
			Status stemMarkingStatus = Status.SUCCESS;
			if((" "+morphemicPos+" ").split("\\#").length!=3){
				stemMarkingStatus = StemMarker.execute(analysis);
				if(stemMarkingStatus == Status.FAIL){
					Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+ " does not mark the stem boundaries correctly, and the stem boundaries cannot be detected automatically.");
					stemMarkingStatus = Status.FAIL;
				}else {
					if(!madamira)
						Logger.getInstance().append(analysis, "bwhash:"+originalBWHash+ " does not mark the stem boundaries correctly, and has changed to "+analysis.get(Feature.BWHASH)+".");
				}
			}
			if(stemMarkingStatus == Status.FAIL){
				return Status.FAIL;
			}
			

			//Do BW Tagging Filtering
			if(!madamira){
				Status bwFiltering = BWTagFilter.execute(analysis);
				if(bwFiltering == Status.FAIL){
					return Status.FAIL;
				}
			}
			
			//Undiacritize and Normalize
			Status undiacritizeAndNormalizeStatus = UndiacritizerAndANormalizer.execute(analysis);
			if(undiacritizeAndNormalizeStatus == Status.FAIL)
				return Status.FAIL;
			
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}
	
	//Feature Assignment
	private static Status processFeatures(Map<Feature, String> analysis, boolean functionalNumber, boolean functionalGender){
		Status status = FeatureAssignmentHandler.execute(analysis);
		if(status == Status.SUCCESS){
			status = FunctionalFeatureHandler.execute(analysis, functionalNumber, functionalGender);
		}
		return status;
	}

}
