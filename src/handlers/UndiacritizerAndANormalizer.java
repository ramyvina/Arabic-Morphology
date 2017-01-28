package handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import utils.StringProcessor;
import constants.Feature;
import constants.Status;

public class UndiacritizerAndANormalizer {
	
	//Undiacritization and Normalization
	public static Status execute(Map<Feature, String> analysis){
		try{
			//Read BWHash, POSs and Morphemes
			String morphemicPos = analysis.get(Feature.BWHASH); 
			String diac = analysis.get(Feature.DIAC); 
			morphemicPos = morphemicPos.replace("+#", "^").replace("#+", "@");
			String start = (morphemicPos.charAt(0)+"").replaceAll("[^\\+\\#\\^\\@]", "");
			String end = (morphemicPos.charAt(morphemicPos.length()-1)+"").replaceAll("[^\\+\\#\\^\\@]", "");
			morphemicPos = morphemicPos.replaceAll("^[\\+\\#\\^\\@]", "").replaceAll("[\\+\\#\\^\\@]$", "");
			String splits = morphemicPos.replaceAll("[^\\+\\#\\^\\@]", "");
			List<String>  morphemicPosSplits = Arrays.asList(morphemicPos.split("[\\+\\#\\^\\@]"));
	
			//Undiacritize DIAC
			String undiacDiac = StringProcessor.undiacritizeAndKeepEmpty(diac);
				
			//Undiacritize BWHASH
			String undiacMorphemicPos = "";
			for(int i=0; i<morphemicPosSplits.size(); i++){
				String form = morphemicPosSplits.get(i).split("\\/")[0];
				String originalForm = form;
				String pos = morphemicPosSplits.get(i).split("\\/")[1];
				form = StringProcessor.undiacritizeAndKeepEmpty(form);
				String formPos = form+"/"+pos;
				//Handle the 'null' case.
				if(form.equals("") && pos.matches(".*(PVSUFF|IVSUFF|CVSUFF).*") && originalForm.matches("^[aiuFKNo~`]$")){
					formPos="(null)"+"/"+pos;
				}else if(form.equals("")){
					formPos = "";
				}
				undiacMorphemicPos+=formPos+(i<morphemicPosSplits.size()-1?splits.charAt(i):"");	
			}
			undiacMorphemicPos=start+undiacMorphemicPos+end;
			undiacMorphemicPos = undiacMorphemicPos.replace("^", "+#").replace("@", "#+");
			undiacMorphemicPos = undiacMorphemicPos.replace("++", "+");
	
			//Normalize DIAC
			String splitUndiacDiac = SplitHandler.getBestDiacSplit(undiacMorphemicPos, undiacDiac);
			String normSplitUndiacDiac = splitUndiacDiac.replaceAll("(^|\\+|\\_)[\\{\\<\\>\\|]", "$1A");
			String normUndiacDiac = normSplitUndiacDiac.replace("+", "").replace ("^", "");
				
			//Normalize BWHASH
			String normUndiacMorphemicPos = undiacMorphemicPos.replaceAll("(^|\\+|\\#|\\_)[\\{\\<\\>\\|]", "$1A");
				
			//Update the analysis
			analysis.put(Feature.DIAC, normUndiacDiac);
			analysis.put(Feature.BWHASH, normUndiacMorphemicPos);
			analysis.put(Feature.BW, analysis.get(Feature.BWHASH).replace("#", ""));
			
		}catch(Exception e){
			Logger.getInstance().append(analysis, "An error occurred while processing the entry.");
			return Status.FAIL;
		}
		return Status.SUCCESS;
	}

}
