import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.*;
import edu.stanford.nlp.trees.*;

import java.io.*;
import java.util.*;

/**
 * @author Petra Halova
 * For running this program is necessary:
 * Set working directory: "/home/your path to parser/stanford-parser-full-2017-06-09/stanford-parser-3.8.0-models"
 * Set program arguments:
 * "/home/path to directory with files to process"
 * "/home/path to file, where the results will be written/file_name.csv"
 * This class counts for each review number of verbs, which are not negative
 * Part of speech Stanford parser and Stanford parser for processing grammatical structure are used
 */
public class GrammaticalStructure {

    public static void main(String[] args) throws IOException {
        //loading parser
        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        String[] options = {"-nthreads", "5"};
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel,options);

        int numberOfVerbs;
        int numberOfFiles = 1;
        final File folder = new File(args[0]);
        String path = args[1];
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        //for each file count number of positive verbs and write the results to the file
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(numberOfFiles +" "+fileEntry.getName());
            numberOfVerbs= numberOfPositiveVerbs(lp,fileEntry.getAbsolutePath());
            writer.write(fileEntry.getName()+";"+numberOfVerbs);
            writer.newLine();
            numberOfFiles++;
        }
        writer.close();
    }

    /**
     * @param lp lexicalized parser
     * @param filename path to file, which should be processed
     * @return number of verbs, which are not in relation with not
     */
    public static int numberOfPositiveVerbs(LexicalizedParser lp, String filename) {

        int count = 0;
        //parse to sentences
        for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            HashMap<String, Integer> verbsInSentence = new HashMap<>();
            Tree parse;
            String sent = "";
            for(int i=0;i<sentence.size();i++){
                sent += sentence.get(i) + " ";
            }

            // loading and using an explicit tokenizer
            TokenizerFactory<CoreLabel> tokenizerFactory =
                    PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
            Tokenizer<CoreLabel> tok =
                    tokenizerFactory.getTokenizer(new StringReader(sent));
            List<CoreLabel> rawWords2 = tok.tokenize();
            parse = lp.apply(rawWords2);

            TreebankLanguagePack tlp = lp.treebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            edu.stanford.nlp.trees.GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

            //Part of speech recognition
            for (int i = 0; i < parse.taggedYield().size(); i++) {
                String pos = parse.taggedYield().get(i).toString();
                String verb = posParser(pos);
                if(verb!=null) {
                    if(verbsInSentence.containsKey(verb)){
                        verbsInSentence.replace(verb,verbsInSentence.get(verb)+1);
                    }else{
                        verbsInSentence.put(verb,1);
                    }
                }
            }

            //determines which verbs are negated
            for (TypedDependency aTdl : tdl) {
                String dependency = String.valueOf(aTdl);
                if (dependency.startsWith("neg")) {
                    String verb = dependencyParser(dependency);
                    if(verbsInSentence.containsKey(verb)){
                        verbsInSentence.replace(verb, verbsInSentence.get(verb)-1);

                    }
                }
            }
            count += numberOfVerbsInSentence(verbsInSentence);
        }
        return count;
    }

    /**
     * Parse tagged dependencies
     * @param dependency tagged dependency, for example neg for negation
     * @return verb which is in relation with not or null
     */
    private static String dependencyParser(String dependency){
        String[] split = dependency.split("[(-,-]");
     //   System.out.println(split[0]+"-"+split[1]+","+split[3]);
        return split[1];
    }

    /**
     * Parse part of speech tags to find verbs only
     * @param pos tagged word
     * @return verb or null
     */
    private static String posParser(String pos){
        String[] split = pos.split("/");

        if(split[1].startsWith("VB")&&!split[1].startsWith("VBN")){
          //  System.out.println(split[0]+"/"+split[1]);
            return split[0];
        }
        return null;
    }

    private static Integer numberOfVerbsInSentence(HashMap<String, Integer> verbsInSentence){
        Integer count =0;
        for(String key: verbsInSentence.keySet()){
            count += verbsInSentence.get(key);
        }
        return count;
    }
}
