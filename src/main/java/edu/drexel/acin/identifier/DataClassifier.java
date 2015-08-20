package edu.drexel.acin.identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vincent Cicirello
 * @version April 2012
 */
public class DataClassifier {

    public static enum Distance {
        NCD,
        CDM
    }

    public static enum Algorithm {
        DEFAULT,
        KNN,
        WKNN
    }

    public static void usage() {
        System.out.println("Usage:");
        System.out.println("To train the classifier:");
        System.out.println("\t -T trainingDataFilename outputClassifierFilename <optional options list>");
        System.out.println("To classify unknown instances:");
        System.out.println("\t -C classifierFilename instanceListFilename");
        System.out.println("Options List:");
        System.out.println("\t -H Use headers only, where classifier considers the first 16KB (16384 bytes) of a file to be its header.");
        System.out.println("\t -TR Use trailers only, where classifier considers the last 16KB (16384 bytes) of a file to be its trailer.");
        //System.out.println("\t -HT Use combination of headers and trailers.");
        System.out.println("\t -D0 NCD Distance");
        System.out.println("\t -D1 CDM Distance");

        System.out.println("\t -KNN Train for K-nearest neighbors, using leave one out cross validation to find k.");
        System.out.println("\t -WKNN Train for Weighted K-nearest neighbors, using leave one out cross validation to find k.");

        System.out.println("Format for Training Data Files:");
        System.out.println("\t One instance per line formatted as: filename <tab> filetype");
        System.out.println("Format for Instance List Files:");
        System.out.println("\t One instance per line formatted as: filename");
    }

    public static final float TEST_RATIO = 0.2f;

    public static Map<String, String> readTrainingFile(String filename) throws FileNotFoundException {
        Map<String, String> samples = new HashMap<String, String>();

        Scanner scan = new Scanner(new File(filename));
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            if (line.length() > 0) {
                int tabIndex = line.lastIndexOf('\t');
                if (tabIndex < 0) { //if (pair.length != 2) {
                    scan.close();
                    throw new IllegalArgumentException("Training data file not formatted correctly.");
                }
                samples.put(line.substring(0, tabIndex), line.substring(tabIndex + 1));
            }
        }
        System.out.println("loaded " + samples.size() + " samples");
        scan.close();
        return samples;
    }

    public static void doCrossFold(String filename) {
        final Map<String, String> samples;
        try {
            //Read samples file
            samples = readTrainingFile(filename);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataClassifier.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }


        //Extract all of the type names
        List<String> labels = new ArrayList<String>(new HashSet<String>(samples.values()));

        //Invert the training set to group by types instead of files
        Map<String, Set<String>> inverted = new TreeMap<String, Set<String>>();
        for (Map.Entry<String, String> e : samples.entrySet()) {
            Set<String> matchingFiles = inverted.get(e.getValue());
            if (matchingFiles == null) {
                matchingFiles = new TreeSet<String>();
                inverted.put(e.getValue(), matchingFiles);
            }
            matchingFiles.add(e.getKey());
        }

        int[][] confusionMatrix = new int[labels.size()][labels.size()];

        int numCorrect = 0;
        long starttime = System.currentTimeMillis();
        for (int run = 0; run < 5; run++) {
            //TODO: Split samples, by TEST_RATIO
            final Map<String, String> trainset = new HashMap<String, String>();
            final Map<String, String> testset = new HashMap<String, String>();
            System.out.println("= ========================================");

            int i = 0;
            for (Map.Entry<String, Set<String>> e : inverted.entrySet()) {
                for (String sampleName : e.getValue()) {
                    boolean testSample = false;
                    i++;
                    if (i % 5 == run) {
                        testSample = true;
                    }

                    //Figure out if this should be a test sample

                    if (testSample) {
                        testset.put(sampleName, e.getKey());
                        //System.out.println(e.getKey());
                    } else {
                        trainset.put(sampleName, e.getKey());
                    }
                }
            }

            //TODO: Remove this


            //TODO: Train classifier with trainset
            FiletypeClassifier fc = doTrain(Algorithm.DEFAULT, Distance.CDM, trainset);

            //TODO: Test classifier with testset
            float score = doClassify(fc, testset, labels, confusionMatrix);
            numCorrect += testset.size() * score;

            //TODO: Grade results and print summary
            System.out.println("Score=" + score);
        }
        System.out.println("Totalscore= " + (float) numCorrect / samples.size());
        for (int i = 0; i < labels.size(); i++) {
            System.out.print(labels.get(i));
            for (int j = 0; j < labels.size(); j++) {
                System.out.print("\t" + confusionMatrix[i][j]);

            }
            System.out.println();

        }
        System.out.println("ALL DONE");
        long endtime = System.currentTimeMillis();
        long elapsedtime = endtime - starttime;
        System.out.println("Finished in " + (elapsedtime / 1000) + " seconds");
    }

    public static FiletypeClassifier doTrain(boolean wknn, boolean knn, boolean ncd, String dataFileName) {
        FiletypeClassifier fc = new FiletypeClassifier();
        try {
            if (wknn) {
                fc.trainWeightedKNearest(dataFileName, ncd ? FiletypeClassifier.NCD_DISTANCE : FiletypeClassifier.CDM_DISTANCE);
            } else if (knn) {
                fc.trainKNearest(dataFileName, ncd ? FiletypeClassifier.NCD_DISTANCE : FiletypeClassifier.CDM_DISTANCE);
            } else {
                fc.train(dataFileName);
            }
        } catch (IOException e) {
            System.out.println("Error: The training file doesn't exist.");
            System.exit(1);
        }

        System.out.println("Training Completed Successfully.");
        return fc;
    }

    public static FiletypeClassifier doTrain(Algorithm algorithm, Distance distance, Map<String, String> trainset) {
        FiletypeClassifier fc = new FiletypeClassifier();
        try {
            if (algorithm == Algorithm.WKNN) {
                fc.trainWeightedKNearest(trainset, distance == Distance.NCD ? FiletypeClassifier.NCD_DISTANCE : FiletypeClassifier.CDM_DISTANCE);
            } else if (algorithm == Algorithm.KNN) {
                fc.trainKNearest(trainset, distance == Distance.NCD ? FiletypeClassifier.NCD_DISTANCE : FiletypeClassifier.CDM_DISTANCE);
            } else {
                fc.train(trainset);
            }
        } catch (IOException e) {
            System.out.println("Error: The training file doesn't exist.");
            System.exit(1);
        }

        System.out.println("Training Completed Successfully.");
        return fc;
    }

    public static void doClassify(FiletypeClassifier fc, String dataFileName) {

        Scanner scan = null;
        try {
            scan = new Scanner(new File(dataFileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error: The instance list file, " + dataFileName + ", does not exist.");
            System.exit(1);
        }

        while (scan.hasNextLine()) {
            String nextFile = scan.nextLine().trim();
            if (nextFile.length() > 0) {

                String type1 = "";
                try {
                    type1 = fc.predictClass(nextFile);
                } catch (IOException e) {
                    System.out.println("A file IO exception occurred during prediction, likely caused by a failure to find a training instance.");
                }
                System.out.print(nextFile + "\t");
                System.out.println(type1);
                System.out.flush();
                gc();
            }
        }
        scan.close();
    }

    public static void doClassify(FiletypeClassifier fc, InputStream fileStream) {

        Scanner scan = new Scanner(fileStream);

        while (scan.hasNextLine()) {
            String nextFile = scan.nextLine().trim();
            if (nextFile.length() > 0) {

                String type1 = "";
                try {
                    type1 = fc.predictClass(nextFile);
                } catch (IOException e) {
                    System.out.println("A file IO exception occurred during prediction, likely caused by a failure to find a training instance.");
                }
                System.out.print(nextFile + "\t");
                System.out.println(type1);
                System.out.flush();
                gc();
            }
        }
        scan.close();
    }

    public static float doClassify(FiletypeClassifier fc, Map<String, String> testset, List<String> labels, int[][] confusionMatrix) {

        int numCorrect = 0;

        for (Map.Entry<String, String> e : testset.entrySet()) {
            String nextFile = e.getKey();
            String correctLabel = e.getValue();
            if (nextFile.length() > 0) {

                String type1 = "";
                try {
                    type1 = fc.predictClass(nextFile);
                } catch (IOException ex) {
                    System.out.println("A file IO exception occurred during prediction, likely caused by a failure to find a training instance.");
                }
                String[] types = type1.split("\t");
                String bestGuess = types[0];

                confusionMatrix[labels.indexOf(correctLabel)][labels.indexOf(bestGuess)]++;

                if (bestGuess.equals(correctLabel)) {
                    System.out.print("CORRECT: " + nextFile + "\t");
                    System.out.println(bestGuess);
                    numCorrect++;

                } else {
                    System.out.print("INCORRECT: " + nextFile + "\t");
                    System.out.println(bestGuess);
                    System.out.println("SHOULD BE " + correctLabel);
                }
                System.out.flush();
                gc();
            }
        }
        return (float) numCorrect / testset.size();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        doCrossFold("/home/omodolu95/NetBeansProjects/ClassifierCodeEtc/src/Unknown Formatstrainset.txt");
    }

    private static void gc() {
        Object obj = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        int i = 0;
        while (ref.get() != null && i < 20) {
            System.gc();
            i++;
        }
    }
}
