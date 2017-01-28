package handlers;

import java.util.HashSet;
import java.util.Set;

public class SplitHandler {
	
	//Getting the best word segmentation for a given diac
	public static String getBestRawSplit(String diac, String word){
		if(diac.matches("^\\+.*\\+$"))
			return "+"+word+"+";
		//selectedSplit is what holds the solution eventually
		String selectedSplit = word;
		try{
			int smallestDistance = Integer.MAX_VALUE;
			int smallestPrefixLength = Integer.MAX_VALUE;
			for(int i=0; i<=word.length(); i++){
				for(int j=i; j<=word.length(); j++){
					//3-way splits
					String splitStr = word.substring(0,i)+"+"+word.substring(i,j)+"+"+word.substring(j);
					//Reject invalid cases
					if(word.substring(i,j).contains("_") || word.substring(i,j).equals(""))
						continue;
					//Get the edit distance between the current candidate split and the morphemes
					int distance=getLevenshteinDistance(splitStr, diac);
					int prefixLength = i;
					//Update the smallest distance and the selected split if necessary
					if((distance < smallestDistance) || (distance == smallestDistance && prefixLength < smallestPrefixLength)){
						smallestDistance = distance;
						smallestPrefixLength = prefixLength;
						selectedSplit = splitStr;
					}
				}
			}
		}catch(Exception e){
			System.out.println("An unexpected error has occurred!"+"\t"+"SplitHandler");
		}
		//The best raw Split
		return selectedSplit;
	}
	
	//Getting the best diac segmentation for a given BW tag
	public static String getBestDiacSplit(String bw, String diac){
		//selectedSplit is what holds the solution eventually
		String selectedSplit = diac;
		try{
			bw = bw.replace("#", "").replace("(null)", "");
			//Get the morphemes out of the BW tag
			String morphemes = bw.contains("/")? bw.replaceAll("\\/.*?(\\+|$)", "$1") : bw;
			//Get the number of splits
			int splitCount = morphemes.length() - morphemes.replace("+", "").length();
			//Initialize the list of candidate solutions (splits)
			Set<String> solutions = new HashSet<String>();
			//Get all the possible splits
			addPluses(diac, splitCount, solutions);
			
			//Get the best split value, that is the one that minimizes the edit distance versus the morphemes
			//Initially, the smallest edit distance is so big
			int smallestDistance = Integer.MAX_VALUE;
			//Loop over the candidate splits
			for(String candidate : solutions){
				//Reject invalid cases
				if((bw.startsWith("+") && !candidate.startsWith("+")) || (bw.endsWith("+") && !candidate.endsWith("+")))
					continue;
				if(candidate.contains("++"))
					continue;
				if(candidate.matches("^.*\\+.*\\_.*\\+.*$"))
						continue;
				//Get the edit distance between the current candidate split and the morphemes
				int distance=getLevenshteinDistance(candidate, morphemes);
				//Update the smallest distance and the selected split if necessary
				if((distance < smallestDistance)){
					smallestDistance = distance;
					selectedSplit = candidate;
				}
			}
		}catch(Exception e){
			System.out.println("An unexpected error has occurred!"+"\t"+"SplitHandler");
		}
		//The best diac Split
		return selectedSplit;
	}
	
	//Getting all the possible strings for a given number of splits (recursion)
	public static void addPluses(String diac, int count, Set<String> solutions){
		//Count = 0? We are done! Add the current solution and return.
		if(count==0){ 
			solutions.add(diac);
			return;
		}
		//Add a plus in all the possible places
		for(int i=0; i<=diac.length(); i++){
			//Keep track of the current value for backtracking
			String current = diac;
			//Construct the new split
			diac=diac.substring(0,i)+"+"+diac.substring(i);
			//Call recursively with a decremented count
			addPluses(diac, count-1, solutions);
			//Backtracking
			diac = current;
		}
		return;
	}
	
	//Getting the minimum edit distances between two Strings (dynamic programming)
	public static int getLevenshteinDistance(String str1, String str2) {
		//Trivial case
		if(str1.equals(str2))
			return 0;
		
		//Initialize the edit distance matrix
		//Everything whould be zero, except for the first row and first column which should have the cost of addition and deletion, respectively.
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];
    	distance[0][0] = 0;
        for (int i = 1; i <= str1.length(); i++)
        	distance[i][0] = distance[i-1][0]+1;
        for (int j = 1; j <= str2.length(); j++)
        	distance[0][j] = distance[0][j-1]+1;

        //The cost of any cell is the minimum cost out of the costs of addition, deletion and substitution.
        for (int i = 1; i <= str1.length(); i++){
            for (int j = 1; j <= str2.length(); j++){
            	int cost = 0;
            	char s_i = str1.charAt(i - 1);
            	char s_j = str2.charAt(j - 1);
            	if(s_i == s_j){
					cost = 0;
				}else{
					cost = 1;
				}
                distance[i][j] = Math.min(Math.min(distance[i - 1][j]+1, distance[i][j - 1]+1),distance[i - 1][j - 1] + cost);
            }
        }
        //The minimum edit distance
        return distance[str1.length()][str2.length()];
    }
}
