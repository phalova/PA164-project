import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

import java.io.*;
import java.util.*;

/**
 * @author  Petra Halova
 * Working directory: /home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/stanford-parser-full-2017-06-09/stanford-parser-3.8.0-models
 */
public class PartOfSpeech {

    public static void main(String[] args) throws IOException {
        int numberOfFile =0;
        String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        String[] options = {"-retainTmpSubcategories"};
        LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
        TreebankLanguagePack tlp = lp.getOp().langpack();
        LinkedList<String> files = new LinkedList<>();
        HashMap<String,LinkedList<Integer>> table = new HashMap<>();
        int position = -1;
        final File negFile = new File(args[0]);
        final File posFile = new File(args[1]);
        position = processFile(negFile,table,position,files,lp,tlp);
        processFile(posFile,table,position,files,lp,tlp);
        printTable(table, files);
    }


    public static void posTagger(String filename, LexicalizedParser lp, TreebankLanguagePack tlp, HashMap<String,LinkedList<Integer>> table, int position, ArrayList<Set<String>> correctionSets){

        //Read sentence by sentence
        for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            String sent = "";
            for (int i = 0; i < sentence.size(); i++) {
                sent += sentence.get(i) + " ";
            }

            // Tokenizatioon of extracted sentence
            Tokenizer<? extends HasWord> toke =
                    tlp.getTokenizerFactory().getTokenizer(new StringReader(sent));
            List<? extends HasWord> sentence2 = toke.tokenize();
            Tree parse = lp.parse(sentence2);

            //output
            for (int i = 0; i < parse.taggedYield().size(); i++) {
                String pos = parse.taggedYield().get(i).toString();
                String[] split = pos.split("/");
//                if (!split[1].equals(".") && !split[1].equals(",") && !split[0].equals("<")
//                        && !split[0].equals(">")&& !split[0].equals("/")
//                        && !split[0].equals("") && !split[0].equals("``") && !split[0].equals("''"))

                if(!correctionSets.get(0).contains(split[0]) && !correctionSets.get(1).contains(split[1])) {
                    String key = keyCorrection(split);
//                    String key = split[0].toLowerCase() + "/" + split[1];
                    if(table.containsKey(key)) {
                        if (table.get(key).size()!= position+1) {
                            table.get(key).addLast(1);
                        } else {
                            Integer value = table.get(key).get(position);
                            value++;
                            table.get(key).set(position, value);
                        }
                    }
                    else{
                        createNewColumn(table,key, position);
                    }
                }
            }
        }
    }

    private static void createNewColumn(HashMap<String, LinkedList<Integer>> table, String newWord, int position){
        LinkedList<Integer> column = new LinkedList<>();
        for(int i =0; i<position;i++){
            column.addLast(0);
        }
        column.addLast( 1);
        table.put(newWord, column);
    }

    private static void addZeros(HashMap<String,LinkedList<Integer>> table, int position){
        for(String key: table.keySet()){
            int size = table.get(key).size();
            if(size != position+1){
                for(int i=0;i<=position-size;i++){
                    table.get(key).addLast(0);
                }
            }
        }
    }


    private static void printTable(HashMap<String, LinkedList<Integer>> table, LinkedList<String> filenames) throws IOException {

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(table.keySet());
        Collections.sort(keys);

        // "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/posNER.csv"
        String path = "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/POS.csv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        String lineOfKeys ="file names;";
        for(String key:keys) {
            lineOfKeys += key+";";
        }
        writer.write(lineOfKeys);
        writer.newLine();
        for(int i=0;i<filenames.size();i++){
            String line = filenames.get(i)+";";
            for(String key:keys){
                line += table.get(key).get(i)+";";
            }
            writer.write(line);
            writer.newLine();
        }
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

    public static ArrayList<Set<String>> initializeCorrectionSets(){
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

    private static int processFile(File file, HashMap<String,LinkedList<Integer>> table, int position, LinkedList<String> files, LexicalizedParser lp, TreebankLanguagePack tlp ){
        ArrayList<Set<String>> correctionSets = initializeCorrectionSets();
        int numberOfFile = 1;
        for (final File fileEntry : file.listFiles()) {
            files.addLast(fileEntry.getName());
            System.out.println(numberOfFile +" "+fileEntry.getName());
            numberOfFile++;
            position++;
            posTagger(fileEntry.getAbsolutePath(), lp, tlp, table, position, correctionSets);
            addZeros(table,position);
        }
        return position;
    }
}
