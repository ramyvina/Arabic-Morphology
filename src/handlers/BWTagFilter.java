package handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import constants.Constants;
import constants.Feature;
import constants.Status;

public class BWTagFilter {
	
	//Checking the status of every morph/POS pair and the whole POS combination
	public static Status execute(Map<Feature, String> analysis){
		try{
			//Read morphological information
			String morphemicPos = analysis.get(Feature.BWHASH); 
			String originalBWHash = analysis.get(Feature.BWHASH);
			String morphemicPosNoStem = morphemicPos.replaceAll("\\#(.*\\/)(.*)(\\#)", "#$2$3");
			String stem = morphemicPos.replaceAll(".*\\+\\#(.*?)\\/.*", "$1");
			morphemicPosNoStem = morphemicPosNoStem.replace("+#", "^").replace("#+", "@");
			String start = (morphemicPosNoStem.charAt(0)+"").replaceAll("[^\\+\\#\\^\\@]", "");
			String end = (morphemicPosNoStem.charAt(morphemicPosNoStem.length()-1)+"").replaceAll("[^\\+\\#\\^\\@]", "");
			morphemicPosNoStem = morphemicPosNoStem.replaceAll("^[\\+\\#\\^\\@]", "").replaceAll("[\\+\\#\\^\\@]$", "");
			String splits = morphemicPosNoStem.replaceAll("[^\\+\\#\\^\\@]", "");
			List<String> morphemicPosNoStemSplits = Arrays.asList(morphemicPosNoStem.split("[\\+\\#\\^\\@]"));
			
			//If one of the morphemes is an unrecoverable error, log it and return.
			for(int i=0; i<morphemicPosNoStemSplits.size(); i++){
				String bwTag = morphemicPosNoStemSplits.get(i);
				String value = ResourceHolder.getInstance().getMorphemeDictionaryMap().get(bwTag);
				if(value == null || value.equals(Constants.MORPHEME_DICTIONARY_ERROR)){
					Logger.getInstance().append(analysis, "BWHASH:"+originalBWHash+ " contains an unrecognized morpheme '"+bwTag+"' that seems to be an error.");
					return Status.FAIL;
				}
			}
			
			//Warn the MSA cases
			for(int i=0; i<morphemicPosNoStemSplits.size(); i++){
				String bwTag = morphemicPosNoStemSplits.get(i);
				String value = ResourceHolder.getInstance().getMorphemeDictionaryMap().get(bwTag);
				if(value == null || value.equals(Constants.MORPHEME_DICTIONARY_MSA)){
					Logger.getInstance().append(analysis, "BWHASH:"+originalBWHash+ " contains an MSA morpheme '"+bwTag+"' that might have been meant to be dialectal.");
				}
			}
			
			//Do deletion and modification whenever possible
			String filteredMorphemicPosNoStem = "";
			for(int i=0; i<morphemicPosNoStemSplits.size(); i++){
				String bwTag = morphemicPosNoStemSplits.get(i);
				String value = ResourceHolder.getInstance().getMorphemeDictionaryMap().get(bwTag);
				//Delete the morphemes that are marked as "DELETE"
				if(value.equals(Constants.MORPHEME_DICTIONARY_DELETE)){
					bwTag = "";
				//Replace the morphemes that are marked as "CHANGE"
				}else if(value.contains(Constants.MORPHEME_DICTIONARY_CHANGE)){
					bwTag = value.replace(Constants.MORPHEME_DICTIONARY_CHANGE, "");
				}
				filteredMorphemicPosNoStem+=bwTag+(i<morphemicPosNoStemSplits.size()-1?splits.charAt(i):"");
			}
			
			//Post-processing
			filteredMorphemicPosNoStem=start+filteredMorphemicPosNoStem+end;
			filteredMorphemicPosNoStem = filteredMorphemicPosNoStem.replace("^", "+#").replace("@", "#+");
			filteredMorphemicPosNoStem = filteredMorphemicPosNoStem.replace("++", "+");
			String filteredMorphemicPos = filteredMorphemicPosNoStem.replace("+#", "+#"+stem+"/");
			String completePosTag = filteredMorphemicPos.replace("#", "").replaceAll("^\\+", "").replaceAll("\\+$", "").replaceAll("[^\\+]*?\\/", "");
			
			//Check for the whole POS combination
			if(!ResourceHolder.getInstance().getPosCombinationMap().containsKey(completePosTag)){
				Logger.getInstance().append(analysis, "BWHASH:"+filteredMorphemicPos+ " does not have a recognized POS combination.");
				return Status.FAIL;
			}
			
			//Update the analysis
			analysis.put(Feature.BWHASH, filteredMorphemicPos);
			analysis.put(Feature.BW, analysis.get(Feature.BWHASH).replace("#", ""));
			
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}

}
