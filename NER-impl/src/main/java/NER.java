import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Petra Halová on 26.11.17.
 */
public class NER {

    /**
     * Sums number of words classified as persons, locations and organisations
     * @param out output from classifier
     * @return count of persons, locations, organisations and number of words
     */
    private static int[] sumClassifiedNER(List<List<CoreLabel>> out, BufferedWriter allWords, String fileName) {
        // number of person, location, organisation, number of words
        int[] count = {0,0,0,0};

        String tag = null;
        for (List<CoreLabel> sentence : out) {
            for (CoreLabel word : sentence) {
                String s = word.get(CoreAnnotations.AnswerAnnotation.class);
                if (s.equals("PERSON")) {
                    count[0]++;
                    tag = "PERSON";
                } else if (s.equals("LOCATION")) {
                    count[1]++;
                    tag = "LOCATION";
                } else if (s.equals("ORGANIZATION")) {
                    count[2]++;
                    tag="ORGANIZATION";
                }
                count[3]++;
                    try {
                        allWords.write(fileName + ";" + word.toString(CoreLabel.OutputFormat.WORD) + ";" + tag);
                        allWords.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                tag = null;
            }
        }
        return count;
    }

    /**
     * formatting of output
     * @param nameOfFile name of classified file
     * @param person number of persons in file
     * @param location number of locations in file
     * @param organisation number of organisations in file
     * @param numberOfWords in file
     * @return line containing absolute and relative(due to number of words in file) count of classified entities
     */
    private static String outputLine(String nameOfFile, int person, int location, int organisation, int numberOfWords){
        return  nameOfFile + ";" + person + ";" + location + ";"
                + organisation + ";" + (person+location+organisation) + ";" + numberOfWords
                + ";" + new BigDecimal(person*1.0 / numberOfWords).setScale(2, RoundingMode.HALF_UP).doubleValue()
                + ";" + new BigDecimal(location*1.0 / numberOfWords).setScale(2, RoundingMode.HALF_UP).doubleValue()
                + ";" + new BigDecimal(organisation*1.0 / numberOfWords).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @param args specify absolute paths to necessary folders
     * [0] absolute path to classifier
     * [1] absolute path to folder containing files to classify
     * [2] absolute path of file (containing name of file) with suffix .csv, where the output will be written
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // String absolutePath = "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/stanford-ner-2017-06-09/classifiers/english.all.3class.distsim.crf.ser.gz"
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(args[0]);

        //"/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/aclImdb/train/pos"
        final File folder = new File(args[1]);

        // "/home/lenoch/Dokumenty/UMI/Strojove uceni a prirozeny jazyk/posNER.csv"
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
        writer.write("name;number of persons;number of locations;number of organisations;number of named entities;number of words;relative number of Persons;relative number of locations;relative number of organisations");
        writer.newLine();

        BufferedWriter allWords = new BufferedWriter((new FileWriter("words.txt")));
        for (final File fileEntry : folder.listFiles()) {
            List<List<CoreLabel>> out = classifier.classifyFile(args[1]+ "/" + fileEntry.getName());
            int[] statistic = sumClassifiedNER(out, allWords, fileEntry.getName());
            writer.write(outputLine(fileEntry.getName(),statistic[0],statistic[1],statistic[2],statistic[3]));
            writer.newLine();
        }
        writer.close();
        allWords.close();
    }

}
