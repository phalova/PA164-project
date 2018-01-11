import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;

import java.io.*;
import java.util.List;

/**
 * Created by lenoch on 11.1.18.
 */
public class NumberOfWords {

    public static void main(String[] args) throws IOException {
        //loading parser
        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        String[] options = {"-nthreads", "5"};
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel, options);

        int numberOfFiles = 1;
        int numberOfWords;
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/nn_positiveVerbs.csv"));
        BufferedReader reader = new BufferedReader(new FileReader("/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/neg_positiveVerbs.csv"));
        String path = "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/aclImdb/train/neg";

        String line = reader.readLine();
        while(line!=null){
//        for(int i=0;i<3; i++){
            String[] split = line.split(";");
            System.out.println(numberOfFiles + " " + split[0]);
            numberOfWords = numberOfWords(path+"/"+split[0], lp);
            line = line + ";" + numberOfWords;
            writer.write(line);
            writer.newLine();
            numberOfFiles++;
            line = reader.readLine();
        }
        writer.close();
        reader.close();
    }

    private static int numberOfWords(String filename, LexicalizedParser lp) {
        int numberOfWords = 0;
        for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {

            for(int i=0;i<sentence.size();i++){
                if(!sentence.get(i).toString().equals(";")&& !sentence.get(i).toString().equals("`")
                        &&!sentence.get(i).toString().equals("'") &&!sentence.get(i).toString().equals(".")
                        &&!sentence.get(i).toString().equals(",") &&!sentence.get(i).toString().equals("?")
                        &&!sentence.get(i).toString().equals("!")&&!sentence.get(i).toString().equals("...")
                        &&!sentence.get(i).toString().equals(":")&&!sentence.get(i).toString().equals("...")&&
                        !sentence.get(i).toString().equals("``") &&!sentence.get(i).toString().equals("''")){
                    //System.out.println(sentence.get(i));
                    numberOfWords ++;
                }

            }

        }
        return numberOfWords;
    }
}