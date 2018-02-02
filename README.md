# Arabic-Morphology


Functions

1. Morphological Analysis based on Initial Morphological Annottations
-	Automatic correction of the erroneous Madiwan Annotations
-	Logging the erroneous Madiwan Annotations that could not be automatically corrected and replacing them by Madamira analyses
-	Generating new features into the Madiwan annotations such as pos, gender, number, person and stem.
-	Normalizing the Madiwan annotations to match the specs of the underlying dialect such as normalization and undiacritization.

2. MLE Text Generation
-	Generating the Maximum Likelihood Estimate of the raw text based on the annotated text.

3. Orthographic Extension Generating
-	Generating the orthographic extensions from the raw text to the annotated text for prefixes, stems and suffixes.


Usage

java -jar MadiwanToMagoldConverter.jar ${path_of_madiwan_file} ${path_of_madamira_analysis}  ${path_of_simple_morphs} ${path_of_composite_pos} ${path_of_default_pos_to_func} ${path_of_prefix_form_to_func} ${path_of_stem_form_to_func} ${path_of_suffix_form_to_func} ${path_of_special_form_to_func} ${output_name} ${mle_text_generation} ${orthographic_extension_generation}


Input Parameters

1.	${path_of_madiwan_file}
Madiwan includes the manual morphological annotations of the data. It should have the following annotations:
•	SENTENCE_ID: sentence ID wit respect to the whole document
•	SENTENCE: the raw sentence
•	INPUT_STRING: the raw word
•	diac: undiacritized CODA-compliant form with “Alef” normalized at the beginning of every morph.
•	lex: diacritized Lemma
•	bwhash: complete Buckwalter morpheme/POS combination, undiacritized and “Alef” normalized at the beginning of every morph. Also, stem boundaries should me marked by “#”.
•	msa: Standard-Arabic equivalence 
•	gloss: English translation
•	fnum: functional gender
•	fgen: functional number
•	region: origin of the word (“ALL” is an option)
•	word_type: MSA/DIALECTAL
•	diwan_source: MADAMIRA/Egy(CALIMA)… etc
•	anno: diwan_approved/no
Example:
;;; SENTENCE_ID 1
;;; SENTENCE >nA bHbk
;;INPUT_STRING >nA
*1.100000 diac:AnA lex:>anA_1 bwhash:+#AnA/PRON_1S#+ msa:>anA gloss:I fgen:m fnum:s region:ALL word_type:DIALECT diwan_source:MADAMIRA anno:diwan_approved
--------------
;;INPUT_STRING bHbk
*1.100000 diac: bAHbk lex:Hab~_1 bwhash:b/PROG_PART+A/IV1S+#Hb/IV#+k/IVSUFF_DO:2MS msa:>uHib~ak gloss:love fgen:m fnum:s region:EGY word_type:DIALECT diwan_source:Egy(CALIMA) anno:diwan_approved
--------------
SENTENCE BREAK
--------------

2.	${path_of_madamra_file}
Please refer to http://www.nizarhabash.com/publications/MADAMIRA-UserManual.pdf

3.	${path_of_simple_morphs} 
Simple-Morphs is a list of all the possible simple morphemes that could appear in the data, and should cover all the possible simple morphemes in the dialect. For each morpheme, an associated statement tells the right action to take. The statement should be one of the following:
•	CORRECT → the morpheme is correct
•	MSA → the morpheme is correct but MSA (not dialectal)
•	DELETE → the morpheme should be deleted
•	CHANGE: XXX → the morpheme is incorrect and should be replaced by XXX.
•	ERROR → the morpheme is erroneous and there is no easy way to correct it automatically.

4.	${path_of_composite_pos} 
Composite-POS is a list of all the possible POS combinations in the dialect, associated with the 1-based stem index.

5.	${path_of_default_pos_to_func}
Default POS-to-Func is a map from the reduced POS tag set (e.g., noun, verb, adj) to their default functions (features), assuming no affixes.

6.	${path_of_prefix_form_to_func}
Prefix Form-to-Func is a map from prefix morph/POS or POS forms to their associated functions (features).

7.	${path_of_stem_form_to_func}
Prefix Form-to-Func is a map from stem morph/POS or POS forms to their associated functions (features).

8.	${path_of_suffix_form_to_func}
Prefix Form-to-Func is a map from suffix morph/POS or POS forms to their associated functions (features).

9.	${path_of_special_form_to_func}
Prefix Form-to-Func is a map from special cases of morph/POS or POS forms to their associated functions (features).

10.	${output_name} 
The name of the output file, including the path but without extensions

11.	${mle_text_generation}
Yes/No (case insensitive) to indicate whether to generate the MLE (Maximum Likelihood Estimates) output or not

12.	${orthographic_extension_generation}
Yes/No (case insensitive) to indicate whether to generate orthographic extensions that map from the annotated text to the raw text or not


Outputs 

1.	Magold
Magold is an automatically corrected and extended version of Madiwan. It lists the manual annotations of Madiwan in one line, and their automatically corrected version in another line (without msa, word_type, region, diwan_source and anno), where other morphological features are extracted. These features are:
•	word: preprocessed raw input 
•	bw: the same as bwhash but without marking stem boundaries
•	pos: the part-of-speech tag of the stem using a reduced tag set
•	gen: surface gender
•	num: surface number
•	prc3, prc2, prc1, prc0, enc0, enc1: proclitics and enclitics
•	per: person number
•	asp: verb aspect
•	vox: verb voice active/passive
•	mod: ignore -- set as “no” for dialects 
•	stt: ignore -- set as “no” for dialects 
•	cas: ignore -- set as “no” for dialects 
•	rat: rationality
•	source: lex/annotator
•	stem: word stem
Example:

;;; SENTENCE_ID 1
;;; SENTENCE >nA bHbk
;;INPUT_STRING >nA
*1.100000 diac:AnA lex:>anA_1 bwhash:+#AnA/PRON_1S#+ msa:>anA gloss:I fgen:m fnum:s region:ALL word_type:DIALECT diwan_source:MADAMIRA anno:diwan_approved
;;WORD AnA
*1.100000 diac:AnA lex:AnA_1 bw:+AnA/PRON_1S+ bwhash:+#AnA/PRON_1S#+ gloss:I;me pos:pron prc3:0 prc2:0 prc1:0 prc0:0 per:na asp:na vox:na mod:no gen:m fgen:na num:s fnum:s stt:no cas:no enc0:0 enc1:0 enc2:0 rat:y stem:AnA source:annotator
--------------
;;INPUT_STRING bHbk
*1.100000 diac: bAHbk lex:Hab~_1 bwhash:b/PROG_PART+A/IV1S+#Hb/IV#+k/IVSUFF_DO:2MS msa:>uHib~ak gloss:love fgen:m fnum:s region:EGY word_type:DIALECT diwan_source:Egy(CALIMA) anno:diwan_approved
;;WORD bHbk
*1.100000 diac:bAHbk lex:Hab~_1 bw:b/PROG_PART+A/IV1S+Hb/IV+k/IVSUFF_DO:2MS bwhash:b/PROG_PART+A/IV1S+#Hb/IV#+k/IVSUFF_DO:2MS gloss:love;like;want pos:verb prc3:0 prc2:0 prc1:0 prc0:0 per:3 asp:i vox:u mod:no gen:m fgen:m num:s fnum:s stt:no cas:no enc0:k enc1:0 enc2:0 rat:na stem:Hb source:annotator
--------------
SENTENCE BREAK
--------------

2.	MLE
MLE is the Maximum Likelihood Estimate of every word in the raw text based on the annotated text.
Example:
Raw Text:
wHdh DAEt $nTthA rAHt tblg Al$rTh qAlwlhA wlA yhmk rwHy Albyt wHnA bnTlE Al$nTp mn tHt AlArD why rAyHh $Aft bAl$ArE EmAl Albldyh yHfrwn qAlt lhm $dwA Hylkm trY lwn Al$nTp bny
MLE Text:
wHdp DAEt $nTthA rAHt tblg Al$rTp qAlwlhA wlA yhmk rwHy Albyt wAHnA bnTlE Al$nTp mn tHt AlArD why rAyHp $Aft bAl$ArE EmAl Albldyp yHfrwn qAlt lhm $dwA Hylkm trY lwn Al$nTp bny

3.	Orhtographic Extensions
The orthographic extensions are printed out in three files for prefixes, stems and suffixes. The extensions are maps from the raw text to the annotated text, and are exclusive for the process of generating a CLIMA system. 

