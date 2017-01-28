package handlers;

import java.util.Map;

import constants.Feature;

public class SpeechEffectHandler{

	//Removing speech effects
	public static String execute(String word){

		// 1. all '{' goes to 'A'
        word = word.replaceAll("\\{", "A");
        // 2. reduce Y, p, |, <, &, }, ' to single occurrence
        word = word.replaceAll("([pY<'&}|])(\\1)*", "$1");
        
        // 3.1 for all AA cases (exactly two A's)
        //   Special cases:
        // a.  yAAmA  -->  yAmA
        word = word.replaceAll("yAAmA", "yAmA");
        // b.  The following cases should be split after the first A:
        // yAArb  -->  yA rb
        // yAAqmr  -->  yA qmr
        // lAAlh  -->  lA Alh
        // AlAAllh  -->  AlA Allh
        // wlAAyh  -->  wlA Ayh
        word = word.replaceAll("(^yAA+)rb$", "yA rb");
        word = word.replaceAll("(^yAA+)qmr$", "yA qmr");
        word = word.replaceAll("(^lAA+)lh$", "lA Alh");
        word = word.replaceAll("^AlAAllh$", "AlA Allh");
        word = word.replaceAll("^wlAAyh$", "wlA Ayh");
        
        // 3.2  The yA case: every word that starts with yAA\S (where \S is
        // an Arabic letter) should be split after the first A: e.g.,
        //        yAAbw  -->  yA Abw
        word = word.replaceAll("(^yAA)(\\S+)", "yA A$2");

        // 3.3  Finally, the default case is to reduce all AA+ occurrences to a single A.
        word = word.replaceAll("AA+", "A");
        
        
        // 4. Three or more repetitions: Nizar, Ahmed, Ramy and Wael analyzed the
        // top 400 elongated types (contributing to 73% of elongated tokens) and
        // found that 371 types are reduced to 1 letters while the rest (29 = 7%)
        // are either reduced to 2 letters or special cases. Nizar suggested annotating
        // the bottom 100 cases to see whether this percentage is preserved or not.
        // It was found that all of these cases reduce to one letter.
        // It was decided to put the 29 (7%) cases in a lookup table, and reduce
        // everything else to 1 letter.
        // The 28 Cases that should be put in a hash:

        // a. Normal elongated two-letter word:
        // Elongated Form 	Normalized Form
        // Alll+h 	Allh
        // Alll+y 	Ally
        // Altqyyy+m 	Altqyym
        // tqyyy+m 	tqyym
        // wAlll+h 	wAllh
        // mmm+tAz 	mmtAz
        // mmm+kn 	mmkn
        // mkrrr+ 	mkrr
        word = word.replaceAll("^Alll+h$", "Allh");
        word = word.replaceAll("^Alll+y$", "Ally");
        word = word.replaceAll("^Altqyyy+m$", "Altqyym");
        word = word.replaceAll("^tqyyy+m$", "tqyym");
        word = word.replaceAll("^wAlll+h$", "wAllh");
        word = word.replaceAll("^mm+tA+z+$", "mmtAz");
        word = word.replaceAll("^mmm+kn$", "mmkn");
        word = word.replaceAll("^mkrrr+$", "mkrr");

        //b. A special word: Part of yArb elongated as yAAA+ AAA+rb.
        //        AAA+rb   --->   rb
        // This will fix it since yAAA+ is reduced to yA.
        word = word.replaceAll("\\bAAA+rb\\b", "rb");

        //c. Single Repeated letter representing a sound: They should be
        // normalized to two-letters. If this is applied as a rule on all
        // single-letter elongated words (which is recommended), then these
        // cases don't have to be in the lookup hash.
        // Elongated Form 	Normalized Form
        //hhh+p hh
         word = word.replaceAll("hhh+p", "hh");
       
        //5. Repeated letter that is part of a two-letter word: Some of these
        // cases are part of elongated words that are split into multiple tokens,
        // so it is suggested they are normalized while preserving the
        // elongation phenomena; so that they are not confused with actual
        // Arabic words. This will allow an error correction system to easily
        // fix them later or a statistical system (e.g., SMT) to correctly
        // handle them with surrounding tokens.
        //Elongated Form 	Normalized Form 	Comments
        //hAAA+ 	hA
        //Ammm+ 	Ammm 	 should not be confused with Am and Amm
        //wAAA+ 	wAA
        //yyy+h 	yyh
        //EAAA+ 	EA
        //hwww+ 	hww 	Many of them are the Egyptian word hwh (He)
        //www+k 	wwk 	Part of mbrwk: e.g. mbrrr+ www+k
        //AAA+z 	AAAz 	Part of mmtAz: e.g., mmm+t AAA+z
        //AAA+m 	AAAm 	Part of HrAm, slAm, etc
        //Awww+ 	Aww 	Part of Awy, Awfr: e.g., Awww+ frrr+, Awww+ www+y
        word = word.replaceAll("hAAA+", "hA");
        word = word.replaceAll("Ammm+", "Ammm");
        word = word.replaceAll("wAAA+", "wAA");
        word = word.replaceAll("yyy+h", "yyh");
        word = word.replaceAll("EAAA+", "EA");
        word = word.replaceAll("hwww+", "hww");
        word = word.replaceAll("www+k", "wwk");
        word = word.replaceAll("AAA+z", "AAAz");
        word = word.replaceAll("AAA+m", "AAAm");
        word = word.replaceAll("Awww+", "Aww");

        //6. FINALLY, other than these cases, every three-letter repetitions
        // (except digits) should be reduced to one occurrence.
        word = word.replaceAll("([^\\d])\\1\\1+", "$1");
        
        //Remove spaces
        word = word.replaceAll("\\s", "");
        
        return word;
	}
	
	//Whether the analysis has speech effects that have not been processed.
	public static boolean execute(Map<Feature, String> analysis){
		boolean suspecious = isSuspecious(analysis.get(Feature.LEX)) ||
							isSuspecious(analysis.get(Feature.DIAC)) ||
							isSuspecious(analysis.get(Feature.BW));
		if(suspecious){
			Logger.getInstance().append(analysis, "It is suspected that the analysis might require speech effect handling!");
		}
		return suspecious;
	}
	
	//Is the word equal to itself after removing speech effects
	private static boolean isSuspecious(String string){
		return !SpeechEffectHandler.execute(string).equals(string);
	}

}
