package handlers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import constants.Constants;

import main.Main;

public class ResourceHolder {
	
	private static ResourceHolder resourceHolderInstance = null; //Singleton Instance

	private Map<String, String> posCombinationMap;
	private Map<String, String> morphemeDictionaryMap;
	private Map<String, String> prefixFormToFuncMap;
	private Map<String, String> stemFormToFuncMap;
	private Map<String, String> suffixFormToFuncMap;
	private Map<String, String> specialFormToFuncMap;
	private Map<String, String> defaultPosFuncMap;
	
	//Returning an instance of the singleton
	public static ResourceHolder getInstance(){
		if(resourceHolderInstance == null)
			resourceHolderInstance = new ResourceHolder();
		
		//If reading any of the files was not successful, return a null instance
		if(	resourceHolderInstance.getPosCombinationMap() == null ||
			resourceHolderInstance.getMorphemeDictionaryMap() == null ||
			resourceHolderInstance.getPrefixFormToFuncMap() == null ||
			resourceHolderInstance.getStemFormToFuncMap() == null ||
			resourceHolderInstance.getSuffixFormToFuncMap() == null ||
			resourceHolderInstance.getSpecialFormToFuncMap() == null ||
			resourceHolderInstance.getDefaultPosFuncMap() == null)
			return null;
		
		return resourceHolderInstance;
	}
	
	//Constructor
	private ResourceHolder(){
		posCombinationMap = readFileAsMap(Main.posCombinationFile);
		morphemeDictionaryMap = readFileAsMap(Main.morphemeDictionaryFile);
		prefixFormToFuncMap = readFileAsMap(Main.prefixFormToFuncFile);
		stemFormToFuncMap = readFileAsMap(Main.stemFormToFuncFile);
		suffixFormToFuncMap = readFileAsMap(Main.suffixFormToFuncFile);
		specialFormToFuncMap = readFileAsMap(Main.specialFormToFuncFile);
		defaultPosFuncMap = readFileAsMap(Main.defaultPosFuncFile);
	}
	
	//Reading a 2-column file into a map
    private static Map<String, String> readFileAsMap(String filePath){
    	String line = "";
    	try{
    		InputStream is = new FileInputStream(filePath);
    		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    		Map<String, String> map = new HashMap<String, String>();
            while ((line = br.readLine()) != null){
            	line = line.trim();
            	if(line.equals("") || line.startsWith(Constants.COMMENT_PREFIX))
            		continue;
            	map.put(line.split("\\t+\\s*\\t*")[0].trim(), line.split("\\t+\\s*\\t*")[1].trim());
            }
            return map;
    	}catch(Exception e){
    		System.out.println("Wrong Format in "+filePath+": "+line);
    		System.out.println("Error reading "+filePath+"!");
    		return null;
    	}
    }

	public static void setResourceHolderInstance(
			ResourceHolder resourceHolderInstance) {
		ResourceHolder.resourceHolderInstance = resourceHolderInstance;
	}

	public Map<String, String> getPosCombinationMap() {
		return posCombinationMap;
	}

	public Map<String, String> getMorphemeDictionaryMap() {
		return morphemeDictionaryMap;
	}

	public Map<String, String> getPrefixFormToFuncMap() {
		return prefixFormToFuncMap;
	}

	public Map<String, String> getStemFormToFuncMap() {
		return stemFormToFuncMap;
	}

	public Map<String, String> getSuffixFormToFuncMap() {
		return suffixFormToFuncMap;
	}
	
	public Map<String, String> getSpecialFormToFuncMap() {
		return specialFormToFuncMap;
	}

	public Map<String, String> getDefaultPosFuncMap() {
		return defaultPosFuncMap;
	}

}
