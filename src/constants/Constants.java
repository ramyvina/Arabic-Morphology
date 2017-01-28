package constants;

public class Constants {
	
	//MADAMIRA
	public static final String MADAMIRA_WORD_PREFIX = ";;WORD ";
	public static final String MADAMIRA_ANALYSIS_REGEX = "^[\\*\\^\\_].*";
	public static final String MADAMIRA_NO_ANALYSIS_PREFIX = ";;NO-ANALYSIS";
	public static final String MADAMIRA_WORD_BREAK = "--------------";
	public static final String MADAMIRA_SENTENCE_ID = ";;; SENTENCE_ID ";
	public static final String MADAMIRA_SENTENCE_BREAK = "SENTENCE BREAK";
	public static final String MADAMIRA_SENTENCE_PREFIX = ";;; SENTENCE ";
	
	//MADIWAN
	public static final String MADIWAN_INPUT_STRING_PREFIX = ";;INPUT_STRING ";
	public static final String MADIWAN_WORD_PREFIX = ";;WORD ";
	public static final String MADIWAN_DIWAN_PREFIX = ";;DIWAN ";
	public static final String MADIWAN_ANALYSIS_REGEX = "^\\*(1|0).*";
	public static final String MADIWAN_NO_ANALYSIS_PREFIX = ";;NO-ANALYSIS";
	public static final String MADIWAN_WORD_BREAK = "--------------";
	public static final String MADIWAN_SENTENCE_PREFIX = ";;; SENTENCE ";
	public static final String MADIWAN_SENTENCE_ID = ";;; SENTENCE_ID ";
	public static final String MADIWAN_SENTENCE_BREAK = "SENTENCE BREAK";
	public static final String MADIWAN_DIWAN_APPROVED = "diwan_approved";
	
	//MAGOLD
	public static final String MAGOLD_NOUN_PROP_ANALYSIS = "1.000100 diac:* lex:*_0 bw:+*/NOUN_PROP+ bwhash:+#*/NOUN_PROP#+ gloss:* pos:noun_prop prc3:0 prc2:0 prc1:0 prc0:0 per:na asp:na vox:na mod:no gen:m fgen:m num:s fnum:s stt:no cas:no enc0:0 enc1:0 enc2:0 rat:y stem:* source:backoff";	
	
	//WORD
	public static String PUNC_REGEX = "^\\s*[\\-\\=\\\"\\_\\:\\#\\@\\!\\?\\^\\/\\(\\)\\[\\]\\%\\;\\\\\\+\\.\\,]+\\s*$";
	public static String DIGIT_REGEX = ".*\\d.*";
	public static String FOREIGN_REGEX = ".*[BCceGIJLMOPQRUVWX].*";
	public static String ARABIC_REGEX = ".*[\\[\\]\\+\\-AbtvjHxd\\*rzs\\$SDTZEgfqklmnhwypY\\<\\>\\|\\}\\&\\'].*";
	public static String LATIN_MARKER = "@@LAT@@";
	
	//STEM DETECTION
	public static String STEM_DETECTION_CORE_POS_REGEX1 = "IV|PV|CV|IV_PASS|PV_PASS|CV_PASS|NOUN|NOUN_PROP|NOUN_QUANT|NOUN_NUM|ADJ|ADJ_COMP|ADJ_NUM|ADV|ABBREV|FOREIGN|INTERJ|INTERROG_PRON|ACT_PARTIC|PASS_PARTIC";
	public static String STEM_DETECTION_CORE_POS_REGEX2 = "REL_PRON";
	public static String STEM_DETECTION_CORE_POS_REGEX3 = "(mi?n|fi?y|Ea?no?|Ea?la?Y|[a\\<]i?l\\~?A)\\/PREP|(lA|la?[mn]o?|la?yo?s[ao]|g[ai]?yo?r|Ea?dA)\\/NEG_PART";
	public static String[] STEM_DETECTION_CORE_POS_REGEX4 = {"SUB_CONJ","DEM","INTERROG_PRON","VERB","RESTRIC_PART","FOCUS_PART","PREP", "REL_PRON","NEG_PART","PART", "PARTICLE"};

	//FEATURES
	public static String FEATURE_NUMBER_SINGULAR = "s";
	public static String FEATURE_NUMBER_DUAL = "d";
	public static String FEATURE_NUMBER_PLURAL = "p";
	public static String FEATURE_NUMBER_NA = "na";
	public static String FEATURE_GENDER_MASCULIN = "m";
	public static String FEATURE_GENDER_FEMININ = "f";
	public static String FEATURE_GENDER_NA = "na";
	
	//MORPHEME DICTIONARY
	public static String MORPHEME_DICTIONARY_ERROR = "ERROR";
	public static String MORPHEME_DICTIONARY_DELETE = "DELETE";
	public static String MORPHEME_DICTIONARY_CHANGE = "CHANGE: ";
	public static String MORPHEME_DICTIONARY_MSA = "MSA";
	
	//FILES
	public static String FILE_MAGOLD_EXTENSION = ".magold";
	public static String FILE_PREFIX_MAPPING_EXTENSION = ".prefix.map";
	public static String FILE_STEM_MAPPING_EXTENSION = ".stem.map";
	public static String FILE_SUFFIX_MAPPING_EXTENSION = ".suffix.map";
	public static String FILE_MLE_TEXT_EXTENSION = ".mle";
	public static String FILE_LOG_EXTENSION = ".log";
	public static String FILE_INVALID_NAME_REGEX = ".*[^-_.A-Za-z0-9].*";
	
	//POS
	public static final String POS_FOREIGN = "foreign";
	public static final String POS_DIGIT = "digit";
	public static final String POS_PUNC = "punc";
	public static final String POS_NOUN_PROP = "noun_prop";
	public static final String POS_BW_FOREIGN = "FOREIGN";
	public static final String POS_BW_DIGIT = "NOUN_NUM";
	public static final String POS_BW_PUNC = "PUNC";
	public static final String POS_BW_NOUN_PROP = "NOUN_PROP";
	
	//General
	public static final String MAGOLD_EXTENSION = ".magold";
	public static final String UNDEF = "UNDEF";
	public static final String OVERWRITTEN = "[OVERWRITTEN]";
	public static final String COMMENT_PREFIX = ";";
	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String YES_NO_REGEX = "^(YES|NO)$";


}
