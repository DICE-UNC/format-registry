package edu.drexel.acin.identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Driver class for running experiments for the executable paper
 * in the special issue on executable papers for 3d object retrieval
 * (Computers and Graphics).
 *
 * @author Vincent Cicirello (Richard Stockton College)
 * @version March 2013
 */
public class Classify {
    public static void main(String[] args) throws FileNotFoundException {

        String dataFileName = args[0];
        PrintStream out = null;

        if (args.length > 1) {
            String outFile = args[1];
            try {
                out = new PrintStream(outFile);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return;
            }
        }


        Scanner scan = null;
        FiletypeClassifier classifier = null;
        try {
            scan = new Scanner(new File("settings.config.txt"));
            String distanceS = "NCD";
            String filePartS = "HEADERS";
            boolean knn = true;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Scanner lineScan = new Scanner(line);
                if (!lineScan.hasNext()) continue;
                String which = lineScan.next();
                which = which.trim();
                if (which.length() >= 8) which = which.substring(0, 8);
                if (which.equalsIgnoreCase("Distance")) {
                    if (!lineScan.hasNext()) continue;
                    distanceS = lineScan.next().trim().toUpperCase();
                } else if (which.equalsIgnoreCase("FilePart")) {
                    if (!lineScan.hasNext()) continue;
                    filePartS = lineScan.next().trim().toUpperCase();
                } else if (which.equalsIgnoreCase("WKNN")) {
                    knn = false;
                } else if (which.equalsIgnoreCase("KNN")) {
                    knn = true;
                }
            }
            if (distanceS.equalsIgnoreCase("NCD") && filePartS.equalsIgnoreCase("HEADERS")) {
                classifier = new FiletypeClassifier("collage.ncd.h.knn.classifier");
                System.out.println("Classifier is using NCD and Headers");
            } else if (distanceS.equalsIgnoreCase("NCD") && filePartS.equalsIgnoreCase("TRAILERS")) {
                if (knn) {
                    classifier = new FiletypeClassifier("collage.ncd.t.knn.classifier");
                    System.out.println("Classifier is using NCD and Trailers");
                } else {
                    classifier = new FiletypeClassifier("collage.ncd.t.wknn.classifier");
                    System.out.println("Classifier is using NCD and Trailers (weighted K nearest neighbors)");
                }
            } else if (distanceS.equalsIgnoreCase("CDM") && filePartS.equalsIgnoreCase("HEADERS")) {
                classifier = new FiletypeClassifier("collage.cdm.h.knn.classifier");
                System.out.println("Classifier is using CDM and Headers");
            } else if (distanceS.equalsIgnoreCase("CDM") && filePartS.equalsIgnoreCase("TRAILERS")) {
                classifier = new FiletypeClassifier("collage.cdm.t.knn.classifier");
                System.out.println("Classifier is using CDM and Trailers");
            } else {
                classifier = new FiletypeClassifier("collage.ncd.h.knn.classifier");
                System.out.println("Classifier is using NCD and Headers");
            }
        } catch (FileNotFoundException e) {
            System.out.println("settings.config.txt is missing.");
            classifier = new FiletypeClassifier("collage.ncd.h.knn.classifier");
            System.out.println("Classifier is using NCD and Headers");
        }
        if (scan != null) scan.close();


        scan = null;
        try {
            scan = new Scanner(new File(dataFileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error: The instance list file, " + dataFileName + ", does not exist.");
            System.exit(1);
        }

        //System.out.println("This Experiment May Take a Few Minutes.");
        ArrayList<String> fileNames = new ArrayList<String>();

        System.out.println("#STATUS: Loading test cases.");
        //int numIntervals = 1;
        while (scan.hasNextLine()) {
            String nextFileClass = scan.nextLine().trim();
            if (nextFileClass.length() > 0) {
                if (nextFileClass.charAt(0) == '#') continue;

                String[] pair = nextFileClass.split("\\t+");
                String nextFile = pair[0];
                //String correct = pair[1];

                fileNames.add(nextFile);
                //correctClasses.add(correct);
            }
        }
        scan.close();
        System.out.println("#STATUS: Completed loading test cases (" + fileNames.size() + " cases).");

        //int statusInterval = (int)Math.round(fileNames.size() * 0.1);
        //if (statusInterval < 1) statusInterval = 1;
//TIME		timingInit();

        for (int i = 0; i < fileNames.size(); i++) {
            gc();
            //if (i > 0 && (i % statusInterval == 0)) {
            //	System.out.println("#STATUS: Processing " + (100.0*i/fileNames.size()) + "% complete.");
            //	System.out.flush();
            //}
            String file = fileNames.get(i);
            try {
                String prediction = classifier.predictClass(file);
                String[] top = prediction.split("\\t+");
                System.out.println("############\n#File: " + file);
                System.out.println("#First N predicted types:");
                System.out.println("N\tPrediction");
                if (out != null) {
                    out.println("############\n#File: " + file);
                    out.println("#First N predicted types:");
                    out.println("N\tPrediction");
                }
                for (int j = 0; j < top.length; j++) {
                    System.out.println((j + 1) + "\t" + top[j]);
                    if (out != null) out.println((j + 1) + "\t" + top[j]);
                }

            } catch (IOException e) {
                System.out.println("#An IO Error occurred while processing testcase: " + file);
                if (out != null) out.println("#An IO Error occurred while processing testcase: " + file);
                System.out.flush();
            }
        }
        if (out != null) out.close();
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
