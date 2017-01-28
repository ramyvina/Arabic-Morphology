package handlers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.AnalysisUtils;
import utils.StringProcessor;
import constants.Constants;
import constants.Feature;
import constants.Status;

public class MadiwanToMagoldConverter {
	
	private String madiwanFile; //Input Madiwan file
	private List<List<Map<Feature, String>>> MadamiraAnalyses; //Input Madamira analyses
	private String outputPath; //Output file path
	private boolean generateOrthographicMappings; //Orthographic mapping generation
	private boolean generateMLEText; //MLE text generation
	
	private List<List<Map<Feature, String>>> madiwanAnalyses = new ArrayList<List<Map<Feature, String>>>(); //Output MADIWAN analyses
	private List<String> sentences = new ArrayList<String>(); //Sentences
	private Map<String, Map<String, Integer>> wordMappings = new HashMap<String, Map<String, Integer>>(); //Output word mappings
	private Map<String, String> mleWordMappings = new HashMap<String, String>(); //Output MLE word mappings
	private Map<String, Map<String, Integer>> prefixMappings = new HashMap<String, Map<String, Integer>>(); //Output word mappings
    private Map<String, Map<String, Integer>> stemMappings = new HashMap<String, Map<String, Integer>>(); //Output word mappings
    private Map<String, Map<String, Integer>> suffixMappings = new HashMap<String, Map<String, Integer>>(); //Output word mappings
	
    private Map<Feature, String> originalAnalysis = null;
    private int totalAnalyses = 0; //Number of analyses
    private int correctAnalyses = 0; //Number of analyses that are completely correct 
    private int normUndiacAnalyses = 0; //Number of analyses that only received Undiacritization/Normalziation 
    private int changedAnalyses = 0; //Number of analyses that only received major changes
    private int rejectedAnalyses = 0; //Number of rejected analyses
    
	//Constructor
	public MadiwanToMagoldConverter(String madiwanFile, List<List<Map<Feature, String>>> MadamiraAnalyses, String outputPath, boolean generateOrthographicMappings, boolean generateMLEText){
		this.madiwanFile = madiwanFile;
		this.MadamiraAnalyses = MadamiraAnalyses;
		this.outputPath = outputPath;
		this.generateOrthographicMappings = generateOrthographicMappings;
		this.generateMLEText = generateMLEText;
	}
	
	//Converting a MADIWAN file to a MAGOLD file. 
	public List<List<Map<Feature, String>>> execute(){
        
		Logger.getInstance().append("Reading the MADIWAN file!");
				
		try{			
			//Iteration variables
	        int wordIndex = 0;
	        int sentenceIndex = 0;
	        String sentenceId = "0";
			boolean newWordExpected = true;
			Map<Feature, String> currentAnalysis = new HashMap<Feature, String>();
			List<Map<Feature, String>> currentSentenceAnalyses = new ArrayList<Map<Feature,String>>();
			

	        //Reading the MADIWAN file line by line
			InputStream is = new FileInputStream(madiwanFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String line;
	        while ((line = br.readLine()) != null){
	        	
	        	//Ignore empty lines
	        	if(line.equals("")) //Empty line
	        		continue;
	        	
	        	//Trim the line
	        	line = line.trim();
	        	
	        	//Sentence ID line? Record the ID (mainly for logging)
	        	if(line.contains(Constants.MADIWAN_SENTENCE_ID)){
	        		sentenceId = line.replace(Constants.MADIWAN_SENTENCE_ID, "").trim();
	        	
	        	//End of word? Record the current analysis.
	        	} else if(line.equals(Constants.MADIWAN_WORD_BREAK)){
					if(!newWordExpected){
						currentSentenceAnalyses.add(currentAnalysis);
						currentAnalysis = new HashMap<Feature, String>();
					}					
					newWordExpected = true;
					wordIndex++;
				
				//Word line? Read the word and record it
				}else if(line.startsWith(Constants.MADIWAN_INPUT_STRING_PREFIX)){
					currentAnalysis = new HashMap<Feature, String>();
					currentAnalysis.put(Feature.INPUT_STRING, line.replace(Constants.MADIWAN_INPUT_STRING_PREFIX, "").trim());
					newWordExpected = false;
					//Normalize and diacritize the current word
					String processedWord = StringProcessor.normalizeAYP(StringProcessor.undiacritize(currentAnalysis.get(Feature.INPUT_STRING)), true);
					//Remove speech effects
					processedWord = SpeechEffectHandler.execute(processedWord).replace(" ", "");
					currentAnalysis.put(Feature.PROCESSED, processedWord);
					
				//Analysis line? Read it into features
				}else if(line.matches(Constants.MADIWAN_ANALYSIS_REGEX)){
					currentAnalysis.put(Feature.SENTENCE_ID, sentenceId);
					currentAnalysis.put(Feature.DIWAN, line);
					for (Feature feature : Feature.values()){
						//Only read the following features
						if(	feature == Feature.SCORE || feature == Feature.DIAC || feature == Feature.LEX || feature == Feature.BWHASH || 
							feature == Feature.GLOSS || feature == Feature.FNUM || feature == Feature.GEN || feature == Feature.ANNO || feature == Feature.DIWAN_SOURCE){
							currentAnalysis.put(feature, getFeatureValue(line, feature));
						}
					}
					
					//Keep a copy of the analysis at this stage (before any edits).
					originalAnalysis = AnalysisUtils.copyAnalysis(currentAnalysis);
					totalAnalyses++;
					
					//Auto-filled Features
					currentAnalysis.put(Feature.BW, currentAnalysis.get(Feature.BWHASH).replace("#", ""));
					if(currentAnalysis.get(Feature.ANNO).equals("diwan_approved")){
						currentAnalysis.put(Feature.SOURCE, "annotator");
					}else{
						currentAnalysis.put(Feature.SOURCE, "lex");
					}
					
					//Detect if the word represents a trivial case
					boolean trivialCase = 	currentAnalysis.get(Feature.INPUT_STRING).startsWith(Constants.LATIN_MARKER) || 
											StringProcessor.isForeign(currentAnalysis.get(Feature.INPUT_STRING)) || 
											StringProcessor.isPunc(currentAnalysis.get(Feature.INPUT_STRING)) || 
											StringProcessor.isNumeric(currentAnalysis.get(Feature.INPUT_STRING));

					
					Status status = Status.SUCCESS;
					if(trivialCase){
						//Repair the feature case
						status = TrivialCaseHandler.execute(currentAnalysis);
					}else{
						//Process LEX, DIAC, BWHASH and Feature assignment
						status = AnalysisProcessor.process(currentAnalysis, false, false, false);
					}

					boolean backoff = false;
					
					//If the analysis is incorrect, replace it by its Madamira analysis.
					if(status == Status.FAIL){
						Map<Feature, String> madaMap = MadamiraAnalyses.get(sentenceIndex).get(wordIndex);
						
						//If the Madamira analysis is not empty, copy and process it.
						if(madaMap.get(Feature.POS) != null){
							currentAnalysis.put(Feature.SCORE, madaMap.get(Feature.SCORE));
							currentAnalysis.put(Feature.DIAC, madaMap.get(Feature.DIAC));
							currentAnalysis.put(Feature.LEX, madaMap.get(Feature.LEX));
							currentAnalysis.put(Feature.BW, madaMap.get(Feature.BW));
							currentAnalysis.put(Feature.BWHASH, madaMap.get(Feature.BW));
							currentAnalysis.put(Feature.GLOSS, madaMap.get(Feature.GLOSS));
							currentAnalysis.put(Feature.SOURCE, "lex");
							if(currentAnalysis.get(Feature.ANNO).equals("diwan_approved"))
								currentAnalysis.put(Feature.GLOSS, currentAnalysis.get(Feature.GLOSS)+Constants.OVERWRITTEN);
							
							Logger.getInstance().append(currentAnalysis, "rroneous analysis! The analysis has been replaced by its Madamira counterpart. It has to be manually checked in order to reflect dialectal aspects.");

							//Process LEX, DIAC, BWHASH and Feature assignment
							status = AnalysisProcessor.process(currentAnalysis, true, true, true);
						
						//If the Madamira analysis is empty, back-off to NOUN_PROP
						}else{
							String noSpeechEffectsInput = SpeechEffectHandler.execute(currentAnalysis.get(Feature.INPUT_STRING)).replace(" ", "");
							String undiactiziedInput = StringProcessor.undiacritize(noSpeechEffectsInput);
							String undiacritizedAndSANormalized = StringProcessor.normalizeA(undiactiziedInput, true);
							
							currentAnalysis.put(Feature.SCORE, "1.000100");
							currentAnalysis.put(Feature.DIAC, undiacritizedAndSANormalized);
							currentAnalysis.put(Feature.LEX, noSpeechEffectsInput+"_0");
							currentAnalysis.put(Feature.BWHASH, "+#"+undiacritizedAndSANormalized+"/"+Constants.POS_BW_NOUN_PROP+"#+");
							currentAnalysis.put(Feature.BW, currentAnalysis.get(Feature.BWHASH).replace("#", ""));
							currentAnalysis.put(Feature.GLOSS, undiacritizedAndSANormalized);
							currentAnalysis.put(Feature.POS, Constants.POS_NOUN_PROP);
							currentAnalysis.put(Feature.STEM, undiacritizedAndSANormalized);
							currentAnalysis.put(Feature.MSA, undiacritizedAndSANormalized);
							currentAnalysis.put(Feature.SOURCE, "backoff");
							TrivialCaseHandler.assignEmptyFeatures(currentAnalysis);
							backoff = true;
							Logger.getInstance().append(currentAnalysis, "Erroneous analysis! The analysis has a \"No-Analysis\" Madamira counterpart and received a proper-noun backoff.");
						}	
						//Update the counts
						rejectedAnalyses++;
					}else{
						//Update the counts
						if(AnalysisUtils.checkExactEqual(originalAnalysis, currentAnalysis)){
							correctAnalyses++;
						}else if(AnalysisUtils.checkNormUndiacEqual(originalAnalysis, currentAnalysis)){
							normUndiacAnalyses++;
						}else{
							changedAnalyses++;
						}
					}
					
					boolean possibleSpeechEffects = SpeechEffectHandler.execute(currentAnalysis);

					//handle the extensions for the correct analyses
					if(status == Status.SUCCESS){
						//Generate orthographic mappings if required
						if(!trivialCase && !backoff && !possibleSpeechEffects && generateOrthographicMappings){
							handleOrthographicMappings(currentAnalysis);
						}
					}else{
						Logger.getInstance().append(currentAnalysis, "Erroneous analysis!");
					}
					
				//Start of Sentence
				}else if(line.startsWith(Constants.MADIWAN_SENTENCE_PREFIX)){
					String currentSentence = line.replace(Constants.MADIWAN_SENTENCE_PREFIX, "").trim();
					sentences.add(currentSentence);
					wordIndex = 0;
				
				//End of Sentence
				}else if(line.startsWith(Constants.MADIWAN_SENTENCE_BREAK)){
					if(currentSentenceAnalyses.size()>0)
						madiwanAnalyses.add(currentSentenceAnalyses);
					currentSentenceAnalyses = new ArrayList<Map<Feature,String>>();
					sentenceIndex++;
				}
	        }
	        
	        //Close the readers
	        is.close();
	        br.close();
	        
	        //Log the counts
	        Logger.getInstance().appendCounts(totalAnalyses, correctAnalyses, normUndiacAnalyses, changedAnalyses, rejectedAnalyses);
	        
	        //Generate the MLE mappings if required
	        if(generateMLEText){
	        	generateMLEWordMappings();
	        }
	        
	        //Print the output files
	        printOutputFiles();
	        
	        System.out.println("Madiwan analyses have been read and processed successfully!");

		}catch(Exception e){
			System.out.println("Error reading the Madiwan analyses!");
			Logger.getInstance().append("Error reading the Madiwan analyses!");
		}
		return madiwanAnalyses;
	}
	
	private void handleOrthographicMappings(Map<Feature, String> analysis){	
		
		//Read the morphemes
		String prefixMorph = analysis.get(Feature.BWHASH).replaceAll("^(.*)\\#.*\\#.*", "$1").replaceAll("\\/.*?(\\+|$)", "$1").replace("+", "");
		String stemMorph = analysis.get(Feature.BWHASH).replaceAll("^.*\\#(.*)\\#.*$", "$1").replaceAll("\\/.*?(\\+|$)", "$1").replace("+", "");
		String suffixMorph = analysis.get(Feature.BWHASH).replaceAll(".*\\#.*\\#(.*)$", "$1").replaceAll("\\/.*?(\\+|$)", "$1").replace("+", "");
		
		//Build a morpheme-based diac
		String splitBW = prefixMorph+"+"+stemMorph+"+"+suffixMorph;
		
		//Split the diac and the raw word
		String processedDiac = analysis.get(Feature.DIAC).replace("_", "");
		String splitDiac = SplitHandler.getBestDiacSplit(splitBW, analysis.get(Feature.DIAC));
		if(!splitDiac.contains("+")) //could not find a split, something is wrong!
			return;
		String splitWord = SplitHandler.getBestRawSplit(splitDiac, analysis.get(Feature.PROCESSED));
		
		//discover and record the mappings from "raw" to "diac"
		String[] wordSplits = (" "+splitWord+" ").split("\\+");
		String[] diacSplits = (" "+splitDiac+" ").split("\\+");
		for(int i=0; i<wordSplits.length; i++){
			String wordPart = StringProcessor.normalizeAYP(StringProcessor.undiacritize(wordSplits[i].trim()), true);
			String diacPart =  StringProcessor.normalizeAYP(StringProcessor.undiacritize(diacSplits[i].trim()), false);
			if(wordPart.length()>0 && diacPart.length()>0 && !wordPart.equals(diacPart)){
				Map<String, Map<String, Integer>> mappings = i==0? prefixMappings : (i==1? stemMappings : suffixMappings);
				if(!mappings.containsKey(diacPart))
					mappings.put(diacPart, new HashMap<String, Integer>());
				if(!mappings.get(diacPart).containsKey(wordPart))
					mappings.get(diacPart).put(wordPart, 0);
				mappings.get(diacPart).put(wordPart, mappings.get(diacPart).get(wordPart)+1);
			}
		}
		
		//Update the Stem feature
		analysis.put(Feature.STEM, diacSplits[1]);
		
		//Update word mappings
		if(!wordMappings.containsKey(analysis.get(Feature.PROCESSED)))
			wordMappings.put(analysis.get(Feature.PROCESSED), new HashMap<String, Integer>());
		if(!wordMappings.get(analysis.get(Feature.PROCESSED)).containsKey(processedDiac))
			wordMappings.get(analysis.get(Feature.PROCESSED)).put(processedDiac, 0);
		wordMappings.get(analysis.get(Feature.PROCESSED)).put(processedDiac, wordMappings.get(analysis.get(Feature.PROCESSED)).get(processedDiac)+1);
	}
	
	//Generating the MLE mappings
	private void generateMLEWordMappings(){
		for(Entry<String, Map<String, Integer>> outEntry : wordMappings.entrySet()){
			String mleMapping = "";
			int max = -1;
			for(Entry<String, Integer> inEntry : wordMappings.get(outEntry.getKey()).entrySet()){
				if(inEntry.getValue()>max){
					mleMapping = inEntry.getKey();
					max = inEntry.getValue();
				}
			}
			mleWordMappings.put(outEntry.getKey(), mleMapping);
		}
	}
	
	private void printOutputFiles(){
		try{
		
			//File writers
			PrintWriter magoldPW = new PrintWriter(outputPath+Constants.FILE_MAGOLD_EXTENSION);
			PrintWriter mlePW = null; //MLE text
			if(generateMLEText){
				mlePW = new PrintWriter(outputPath+Constants.FILE_MLE_TEXT_EXTENSION);
			}
				        
	        //Loop over the sentences
	        for(int i=0; i<sentences.size(); i++){
	        	//Write the sentence lines
	    		String sentence = sentences.get(i);
	    		magoldPW.write(Constants.MADAMIRA_SENTENCE_ID+(i+1)+"\n");
	    		magoldPW.write(Constants. MADAMIRA_SENTENCE_ID+sentence+"\n");
	    		
	    		//Get word list
	    		String[] words = sentence.split("\\s");
	    		String mleSentence = "";
	    		
	    		//Loop over sentence analyses
	    		for(int j=0; j<madiwanAnalyses.get(i).size(); j++){
	    			Map<Feature, String> currentAnalysis = madiwanAnalyses.get(i).get(j);
	    			String word = words[j];
	    			//Write the word and MADIWAN lines
	    			magoldPW.write(Constants.MADIWAN_INPUT_STRING_PREFIX+word+"\n");
	    			magoldPW.write(Constants.MADIWAN_DIWAN_PREFIX+currentAnalysis.get(Feature.DIWAN)+"\n");
					//Write the word line
					magoldPW.write(Constants.MADIWAN_WORD_PREFIX+word+"\n");
	    			
					//Append the MLE sentence if required
					if(generateMLEText){
						if(	currentAnalysis.get(Feature.POS).equals(Constants.POS_FOREIGN) || 
							currentAnalysis.get(Feature.POS).equals(Constants.POS_PUNC) || 
							currentAnalysis.get(Feature.POS).equals(Constants.POS_DIGIT)){
			    			mleSentence+=mleWordMappings.get(word)+" ";
	    				}else{
		    				String output = mleWordMappings.get(currentAnalysis.get(Feature.PROCESSED));
		    				mleSentence+=(output == null? currentAnalysis.get(Feature.PROCESSED) : output)+" ";
	    				}
	    			}
	    		
					//Write the analysis line
					magoldPW.write(getAnalysisString(madiwanAnalyses.get(i).get(j))+"\n");
					
					//End the word
	    			magoldPW.write(Constants.MADAMIRA_WORD_BREAK+"\n");
				}
	    		
	    		//End the sentence
	    		magoldPW.write(Constants.MADAMIRA_SENTENCE_BREAK+"\n");
	    		magoldPW.write(Constants.MADAMIRA_WORD_BREAK+"\n");
	    		
				//Append the MLE sentence if required
				if(generateMLEText){
					mlePW.write(mleSentence.trim()+"\n");
				}
	        }
	        
	        //Close the writers
	        magoldPW.close();
	        if(mlePW != null){
	        	mlePW.close();
	        }
	        
	        //Generate orthographic mappings if required
			if(generateOrthographicMappings){
	        	generateOrthographicMapping(prefixMappings, outputPath+Constants.FILE_PREFIX_MAPPING_EXTENSION);
	        	generateOrthographicMapping(stemMappings, outputPath+Constants.FILE_STEM_MAPPING_EXTENSION);
	        	generateOrthographicMapping(suffixMappings, outputPath+Constants.FILE_SUFFIX_MAPPING_EXTENSION);
	        }
		}catch(Exception e){}
	}
		
	//Reading the value of a specific feature
	private String getFeatureValue(String analysisLine, Feature feature){
		
		//Score Feature
		if(feature == Feature.SCORE)
			return analysisLine.split("\\s")[0].substring(1);
		
		//UNDEF Case
		if(!analysisLine.contains(feature.name().toLowerCase()))
			return Constants.UNDEF;
		if(!analysisLine.contains(feature.name().toLowerCase()) || analysisLine.contains(feature.name().toLowerCase()+": "))
			return Constants.UNDEF;
		
		//Read the Feature Value
		String featureValue = analysisLine.replaceAll("^.*"+feature.name().toLowerCase()+":(\\S+).*$", "$1");
		
		//In case of a clitic, normalize its Hamzated Alef and diacritize it.
		if(feature == Feature.PRC3 || feature == Feature.PRC2 || feature == Feature.PRC1 || feature == Feature.PRC0 || feature == Feature.ENC2 || feature == Feature.ENC1 || feature == Feature.ENC0){
			if(!(!featureValue.contains("_")||featureValue.equals("na")||featureValue.equals("no")||featureValue.contains("obj")||featureValue.endsWith("_pron")||featureValue.startsWith("part_"))){
				featureValue = StringProcessor.undiacritize(StringProcessor.normalizeA(featureValue.split("\\_")[0], true))+"_"+featureValue.split("\\_")[1];	
			}
		}
		return featureValue;
	}
	
	//Converting a word analysis into an analysis string
	private String getAnalysisString(Map<Feature, String> analysis){
		StringBuilder sb = new StringBuilder();
		for(Feature feature : Feature.values()){
			
			//Exclude the following features from the analysis string
			if(feature == Feature.ANNO || feature == Feature.DIWAN_SOURCE || feature == Feature.DIWAN || 
					feature == Feature.SENTENCE_ID || feature == Feature.INPUT_STRING || feature == Feature.PROCESSED){
				continue;
			
			//Score feature
			}else if(feature == Feature.SCORE){
				sb.append("*"+analysis.get(feature)+" ");
			
			//Any other feature
			}else{
				sb.append(feature.name().toLowerCase()+":"+analysis.get(feature)+" ");
			}
		}
		return sb.toString().trim();
	}
	
	//Generating the orthographic mappings
	private void generateOrthographicMapping( Map<String, Map<String, Integer>> mappings, String outputFile){
		try{
			//Loop over the mappings to print out
			PrintWriter pw = new PrintWriter(outputFile);
			for(Entry<String, Map<String, Integer>> entry1 : mappings.entrySet()){
				for(Entry<String, Integer> entry2 : entry1.getValue().entrySet()){
					if(entry2.getValue()>1){
						pw.write(entry1.getKey()+"\t?\t?\t?\t?\t>>\t"+entry2.getKey()+"\t=\t=\t=\t=\n");
					}
				}
			}
			//Close the writer
			pw.close();
		}catch(Exception e){}
	}

}
