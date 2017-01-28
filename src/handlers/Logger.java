package handlers;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constants.Feature;
import constants.Status;

public class Logger {
	
	private static Logger loggerInstance; //Singleton instance
	
	private List<String> log;
	
	//Returning an instance of the singleton
	public static Logger getInstance(){
		if(loggerInstance == null)
			loggerInstance = new Logger(); 
		return loggerInstance;
	}
	
	//Constructor
	private Logger(){
		log = new ArrayList<String>();
	}
	
	//Appending to the log
	public void append(String text){
		if(!text.equals("null"))
			log.add(text);
	}
	
	//Appending to the log
	public void append(Map<Feature, String> map, String text){
		if(map != null && text != null)
			log.add(map.get(Feature.SENTENCE_ID)+":"+map.get(Feature.INPUT_STRING)+"\t"+text);
	}
	
	//Appending count information to the log
	public void appendCounts(int totalAnalyses, int correctAnalyses, int normUndiacAnalyses, int changedAnalyses, int rejectedAnalyses){
		log.add("\n");
		log.add("Total analyses: "+totalAnalyses);
		log.add("Correct analyses : "+correctAnalyses+" ("+getPercentage(correctAnalyses,totalAnalyses)+"%)");
		log.add("Correct analyses except for Norm/Undiac: "+normUndiacAnalyses+" ("+getPercentage(normUndiacAnalyses,totalAnalyses)+"%)");
		log.add("Changed analyses (other than form Norm/Undiac): "+changedAnalyses+" ("+getPercentage(changedAnalyses,totalAnalyses)+"%)");
		log.add("Rejected analyses (backogg to Madamira):"+rejectedAnalyses+" ("+getPercentage(rejectedAnalyses,totalAnalyses)+"%)");	
	}
	
	//Printing the log out
	public Status print(String outputFile){
		try{
			//Loop over the messages to print out
			PrintWriter pw = new PrintWriter(outputFile);
			for(String str : log){
				pw.write(str+"\n");
			}
			pw.close();
			System.out.println("Check the log at: "+outputFile+"!");
			return Status.FAIL;
		}catch(Exception e){
			System.out.println("Error printing out the log!");
			return Status.SUCCESS;
		}
	}
	
	private static double getPercentage(int num1, int num2){
		try{
			if(num2 == 0)
				return 0;
			DecimalFormat df = new DecimalFormat("###.#");
			return df.parse(df.format((float)num1/(float)num2*100)).doubleValue();
		}catch(Exception e){
			return 0;
		}
	}
 
}
