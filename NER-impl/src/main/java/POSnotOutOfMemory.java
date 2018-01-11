import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

import java.io.*;
import java.util.*;

/**
 * Created by lenoch on 8.1.18.
 * Working directory: /home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/stanford-parser-full-2017-06-09/stanford-parser-3.8.0-models
 */
public class POSnotOutOfMemory {
    /** This example shows a few more ways of providing input to a parser.
     *
     *  Usage: ParserDemo2 [grammar [textFile]]
     */
    public static void main(String[] args) throws IOException {
        String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        String[] options = {"-nthreads","20"};
        LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
        TreebankLanguagePack tlp = lp.getOp().langpack();
        final File file = new File(args[0]);
        processFile(file,lp,tlp);
    }


    public static void posTagger(File file, LexicalizedParser lp, TreebankLanguagePack tlp, ArrayList<Set<String>> correctionSets) throws IOException{

        HashMap<String,Integer> table = new HashMap<>();

        //Read sentence by sentence
        for (List<HasWord> sentence : new DocumentPreprocessor(file.getAbsolutePath())) {
            String sent = "";
            for (int i = 0; i < sentence.size(); i++) {
                sent += sentence.get(i) + " ";
            }

            Tree parse;
            try {
                // Tokenizatioon of extracted sentence
                Tokenizer<? extends HasWord> toke =
                        tlp.getTokenizerFactory().getTokenizer(new StringReader(sent));
                List<? extends HasWord> sentence2 = toke.tokenize();

                parse = lp.parse(sentence2);
            }catch(Exception e){
                System.out.println("Warning, the file"+file.getName()+" is not possible to parse");
                break;
            }
            //output
            for (int i = 0; i < parse.taggedYield().size(); i++) {
                String pos = parse.taggedYield().get(i).toString();
                String[] split = pos.split("/");

                if(!correctionSets.get(0).contains(split[0]) && !correctionSets.get(1).contains(split[1])) {
                    String key = keyCorrection(split);
                    if(table.containsKey(key)) {
                        table.replace(key,table.get(key)+1);
                    } else{
                        table.put(key,1);
                    }
                }
            }
        }
        printTable(table,file.getName());
    }


    private static void printTable(HashMap<String, Integer> table, String filename)throws IOException{

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(table.keySet());
        Collections.sort(keys);

        String[] split = filename.split("\\.");
        // "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/posNER.csv"
        String path = "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/POS/"+split[0]+".csv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        String lineOfKeys ="file names;";
        String lineOfNumbers = filename+";";
        for(String key:keys) {
            lineOfKeys += key+";";
            lineOfNumbers += +table.get(key)+";";
        }
        writer.write(lineOfKeys);
        writer.newLine();
        writer.write(lineOfNumbers);
        writer.newLine();

        writer.close();
    }

    private static String keyCorrection(String[] split){
        String key;
        if(split[1].equals("CD")){
            key="1/CD";
        }else if(split[0].equals("'s") && split[1].equals("VBZ")){
            key = "is/VBZ";
        }else if(split[0].equals("n't") && split[1].equals("RB")){
            key="not/RB";
        }else{
            key = split[0].toLowerCase() + "/" + split[1];
        }
        return key;
    }

    private static ArrayList<Set<String>> initializeCorrectionSets(){
        ArrayList<Set<String>> correctionSets = new ArrayList<>();
        HashSet<String> correctionSetWords = new HashSet<>();
        HashSet<String> correctionSetTags = new HashSet<>();

        correctionSetWords.add("<");
        correctionSetWords.add(">");
        correctionSetWords.add("/");
        correctionSetWords.add("");
        correctionSetWords.add("``");
        correctionSetWords.add("''");

        correctionSetTags.add(".");
        correctionSetTags.add(",");

        correctionSets.add(0, correctionSetWords);
        correctionSets.add(1, correctionSetTags);
        return correctionSets;
    }

    private static void processFile(File file, LexicalizedParser lp, TreebankLanguagePack tlp ) throws IOException{
        ArrayList<Set<String>> correctionSets = initializeCorrectionSets();
        int count = 0;
        for (final File fileEntry : file.listFiles()) {
            System.out.println(count + " " + fileEntry.getName());
            count++;
            posTagger(fileEntry, lp, tlp, correctionSets);
        }
    }
}

