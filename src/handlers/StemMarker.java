package handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import constants.Constants;
import constants.Feature;
import constants.Status;

public class StemMarker {

	public static Status execute(Map<Feature, String> analysis){
		try{
			
			String morphemicPos = analysis.get(Feature.BWHASH); 

			morphemicPos = morphemicPos.replace("#", "");
			
			boolean plusStart = morphemicPos.startsWith("+");
			boolean plusEnd = morphemicPos.endsWith("+");
			morphemicPos = morphemicPos.replaceAll("^\\+", "").replaceAll("\\+$", "");
			List<String>  morphemicPosSplits = Arrays.asList(morphemicPos.split("[\\+]"));
	    	
			//Read the BW value
			int[] counted = new int[morphemicPosSplits.size()];
	    	boolean stemFound = false;
	    	String additionalInterrogPart = "";
	    	int interrog = 0;
	    	String additionalProclitic = "";
	    	int wf = 0;
	    	
	    	//One-Morpheme Words
	    	if(morphemicPosSplits.size()==1){
	    		counted[0]=1;
	    		stemFound = true;
	    	}
	    	
	    	//Words with known open-class POS
	    	//POS Group # 1
	    	if(!stemFound){
		    	for(int i=0; i<morphemicPosSplits.size(); i++){
		    		if(morphemicPosSplits.get(i).matches(".*("+Constants.STEM_DETECTION_CORE_POS_REGEX1+")$")){
		    			counted[i]=1;
		    			stemFound = true;
		    			break;
		    		}
		    	}
	    	}
	    	
	    	//POS Group # 2
	    	if(!stemFound){
		    	for(int i=0; i<morphemicPosSplits.size(); i++){
		    		if(morphemicPosSplits.get(i).matches(".*("+Constants.STEM_DETECTION_CORE_POS_REGEX2+")$")){
		    			counted[i]=1;
		    			stemFound = true;
		    			break;
		    		}
		    	}
	    	}
	    	
	    	//POS Group # 3
	    	if(!stemFound){
		    	for(int i=0; i<morphemicPosSplits.size(); i++){
		    		if(morphemicPosSplits.get(i).matches(".*("+Constants.STEM_DETECTION_CORE_POS_REGEX3+")$")){
		    			counted[i]=1;
		    			stemFound = true;
		    			break;
		    		}
		    	}
	    	}
	    	
	    	//Checking for the cases of common clitics
	    	if(!stemFound){
	    		//strip >a interrogative parts
		    	if(morphemicPosSplits.get(0).matches("^[\\>A\\$][ai]?\\/(INTERROG_PART)$")){
		    		additionalInterrogPart = morphemicPosSplits.get(0);
		    		interrog = 1;
		    		morphemicPosSplits = morphemicPosSplits.subList(1, morphemicPosSplits.size());
		    	}
		    	//strip f/w affixes
		    	if(morphemicPosSplits.get(0).matches("^[wf\\$][ai]?\\/(CONJ|SUB_CONJ|PREP|PART|CONNEC_PART)$")){
		    		additionalProclitic = morphemicPosSplits.get(0);
		    		wf = 1;
		    		morphemicPosSplits = morphemicPosSplits.subList(1, morphemicPosSplits.size());
		    	}
	    	}
	    	
	    	//words of one part after stripping
	    	if(!stemFound){
		    	if(morphemicPosSplits.size()==1){
		    		counted[wf+interrog]=1;
		    		stemFound=true;
		    	}
	    	}
	    	
	    	//special cases
	    	if(!stemFound && morphemicPosSplits.size()>=2 && morphemicPosSplits.get(0).matches("^ha?l\\/INTERROG_PART$") && morphemicPosSplits.get(1).matches("^lA\\/NEG_PART$")){
	    		counted[0+wf+interrog]=1;
	    		counted[1+wf+interrog]=1;
	    		stemFound=true;
	    	}
	    	if(!stemFound && morphemicPosSplits.size()>=2 && morphemicPosSplits.get(0).matches("^[Ahdm\\>][aA]?\\/.*") && morphemicPosSplits.get(1).matches(".*(\\/PRON|\\/POSS_PRON).*")){
	    		counted[1+wf+interrog]=1;
	    		stemFound=true;
	    	}
	    	if(!stemFound && morphemicPosSplits.size()>=2 && morphemicPosSplits.get(1).matches(".*(\\/PRON|\\/POSS_PRON).*")){
	    		counted[0+wf+interrog]=1;
	    		stemFound=true;
	    	}
	    	if(!stemFound &&  morphemicPosSplits.size()>=2 && morphemicPosSplits.get(0).matches(".*(INTERROG_PRON).*") && morphemicPosSplits.get(1).matches(".*(PREP).*")){
	    		counted[1+wf+interrog]=1;
	    		stemFound=true;
	    	}
	    	
	    	//Checking for the Al/DET cases
	    	if(!stemFound){
	        	for(int i=0; i<morphemicPosSplits.size(); i++){
	        		if(morphemicPosSplits.get(i).matches(".*\\/DET.*") && i<morphemicPosSplits.size()-1){
	        			counted[i+1+wf+interrog]=1;
	        			stemFound=true;
	        			break;
	        		}
	        	}
	    	}
	    	
	    	//POS Group # 4
	    	if(!stemFound){
	    		outer: for(int j=0; j<Constants.STEM_DETECTION_CORE_POS_REGEX4.length; j++){
	    			for(int i=morphemicPosSplits.size()-1; i>=0; i--){
	    				if(morphemicPosSplits.get(i).contains(Constants.STEM_DETECTION_CORE_POS_REGEX4[j])){
	    					counted[i+wf+interrog]=1;
	    					stemFound=true;
	    					break outer;
	    				}
	        		}
	        	}
	    	}
	    	
	    	//Constructing BWHASH with stem markers.
	    	
	    	//Check for the cases with common clitics first
	    	if(interrog == 1){
	    		List<String> temp = new ArrayList<String>();
	    		temp.add(additionalInterrogPart);
	    		temp.addAll(morphemicPosSplits);
	    		morphemicPosSplits = temp;
	    	}
	    	if(wf == 1){
	    		List<String> temp = new ArrayList<String>();
	    		temp.add(additionalProclitic);
	    		temp.addAll(morphemicPosSplits);
	    		morphemicPosSplits = temp;
	    	}
	  
	    	//Detect what the stem index is.
	    	String stemMarkedMorphemicPos="";
	    	int stemIndex = -1;
	    	for(int i=0; i<counted.length; i++){
	    		if(counted[i]==0){
	    			stemMarkedMorphemicPos+=morphemicPosSplits.get(i)+(i<counted.length-1?"+":"");
	    		}else{
	    			stemMarkedMorphemicPos+="#"+morphemicPosSplits.get(i)+"#"+(i<counted.length-1?"+":"");
	    			stemIndex = i;
	    		}
	    	}
	    	
	    	//Post-processing
	    	stemMarkedMorphemicPos=(plusStart?"+":"")+stemMarkedMorphemicPos+(plusEnd?"+":"");
	    	if(stemMarkedMorphemicPos.startsWith("#"))
	    		stemMarkedMorphemicPos="+"+stemMarkedMorphemicPos;
	    	if(stemMarkedMorphemicPos.endsWith("#"))
	    		stemMarkedMorphemicPos=stemMarkedMorphemicPos+"+";
	    	
	    	//BWHASH Assignment
	    	analysis.put(Feature.BWHASH, stemMarkedMorphemicPos);
	    	analysis.put(Feature.BW, analysis.get(Feature.BWHASH).replace("#", ""));
	    	
	    	if(stemIndex == -1){
	    		System.out.println("Invalid index: "+analysis.get(Feature.BWHASH));
	    		return Status.FAIL;
	    	}else{
	    		return Status.SUCCESS;
	    	}
			
		}catch(Exception e){
			return Status.FAIL;
		}
	}

}
