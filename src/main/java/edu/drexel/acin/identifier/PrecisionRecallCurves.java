package edu.drexel.acin.identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Scanner;

//TIME  import java.lang.management.ManagementFactory;
//TIME  import java.lang.management.ThreadMXBean;


/**
 * Driver class for running experiments for the executable paper
 * in the special issue on executable papers for 3d object retrieval
 * (Computers and Graphics).
 *
 * @author Vincent Cicirello (Richard Stockton College)
 * @version March 2013
 */
public class PrecisionRecallCurves {


    public static void main(String[] args) {

        String dataFileName = args[0];

        PrintStream out = null;

        if (args.length > 1) {
            String outFile = args[1];

            try {
                out = new PrintStream(outFile);
            } catch (FileNotFoundException e1) {

            }
        }

        String fileNCDH = "collage.ncd.h.knn.classifier";
        String fileNCDT = "collage.ncd.t.knn.classifier";
        String fileCDMH = "collage.cdm.h.knn.classifier";
        String fileCDMT = "collage.cdm.t.knn.classifier";

        Scanner scan = null;
        try {
            scan = new Scanner(new File("classifier.config"));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Scanner lineScan = new Scanner(line);
                String which = lineScan.next();
                which = which.substring(0, 4);
                if (which.equalsIgnoreCase("NCDH")) {
                    fileNCDH = lineScan.next();
                } else if (which.equalsIgnoreCase("NCDT")) {
                    fileNCDT = lineScan.next();
                } else if (which.equalsIgnoreCase("CDMH")) {
                    fileCDMH = lineScan.next();
                } else if (which.equalsIgnoreCase("CDMT")) {
                    fileCDMT = lineScan.next();
                }
            }
        } catch (Exception e) {
            //System.out.println("Error: The configuration file does not exist.");
            //System.exit(1);
        }

        if (scan != null) scan.close();


        System.out.println("#STATUS: The classifiers are loading.");
        FiletypeClassifier ncdHClass = null;
        FiletypeClassifier ncdTClass = null;
        FiletypeClassifier cdmHClass = null;
        FiletypeClassifier cdmTClass = null;
        try {
            ncdHClass = new FiletypeClassifier(fileNCDH);
            System.out.println("#STATUS: Loading classifiers 25% complete.");
            ncdTClass = new FiletypeClassifier(fileNCDT);
            System.out.println("#STATUS: Loading classifiers 50% complete.");
            cdmHClass = new FiletypeClassifier(fileCDMH);
            System.out.println("#STATUS: Loading classifiers 75% complete.");
            cdmTClass = new FiletypeClassifier(fileCDMT);
            System.out.println("#STATUS: Loading classifiers 100% complete.");
        } catch (FileNotFoundException e) {
            System.out.println("Error: The classifier file does not exist: " + e);
            System.exit(1);
        }

        scan = null;
        try {
            scan = new Scanner(new File(dataFileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error: The instance list file, " + dataFileName + ", does not exist.");
            System.exit(1);
        }

        //System.out.println("This Experiment May Take a Few Minutes.");
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> correctClasses = new ArrayList<String>();

        System.out.println("#STATUS: Loading test cases.");
        int numIntervals = 1;
        while (scan.hasNextLine()) {
            String nextFileClass = scan.nextLine().trim();
            if (nextFileClass.length() > 0) {
                if (nextFileClass.charAt(0) == '#') continue;

                String[] pair = nextFileClass.split("\\t+");
                String nextFile = pair[0];
                String correct = pair[1];

                fileNames.add(nextFile);
                correctClasses.add(correct);
                int count = ncdHClass.countClass(correct);
                if (count <= 0) count = 1;
                if ((numIntervals % count) != 0) {
                    int n1 = (count > numIntervals) ? count : numIntervals;
                    int n2 = (count < numIntervals) ? count : numIntervals;
                    numIntervals = n1;
                    while ((numIntervals % n2) != 0) numIntervals += n1;
                }
            }
        }
        scan.close();
        System.out.println("#STATUS: Completed loading test cases (" + fileNames.size() + " cases).");

        int countSkipped = 0;
        System.out.println("#This Experiment May Take a Few Minutes.");

        double[] PRhNCD = new double[numIntervals + 1];
        double[] PRtNCD = new double[numIntervals + 1];
        double[] PRhCDM = new double[numIntervals + 1];
        double[] PRtCDM = new double[numIntervals + 1];


        int statusInterval = (int) Math.round(fileNames.size() * 0.1);
        if (statusInterval < 1) statusInterval = 1;
//TIME		timingInit();

        for (int i = 0; i < fileNames.size(); i++) {
            gc();
            if (i > 0 && (i % statusInterval == 0)) {
                System.out.println("#STATUS: Processing " + (100.0 * i / fileNames.size()) + "% complete.");
                System.out.flush();
            }
            String file = fileNames.get(i);
            String correct = correctClasses.get(i);

            ArrayList<String> sortedCases = null;
            try {
//TIME				long start = cpuTime();
                sortedCases = ncdHClass.sortTrainingCasesByDistanceToQuery(file);
//TIME				long cpuNano = cpuTime()-start;
//TIME				double cpuSec = cpuNano / 1000000000.0;
//TIME				System.out.print(cpuSec);
            } catch (IOException e) {
                System.out.println("#Error: Failed to open testcase.  Skipping case.");
                countSkipped++;
                continue;
            }

            int numMatchingCases = countMatchingCases(correct, sortedCases);

            double[][] rp = getPrecisionRecall(correct, sortedCases, numMatchingCases);
            updatePRCurves(rp, PRhNCD);

            try {
//TIME				long start = cpuTime();
                sortedCases = ncdTClass.sortTrainingCasesByDistanceToQuery(file);
//TIME				long cpuNano = cpuTime()-start;
//TIME				double cpuSec = cpuNano / 1000000000.0;
//TIME				System.out.print("\t" + cpuSec);
            } catch (IOException e) {
                System.out.println("Error: Failed to open testcase.  EXITING.");
                //countSkipped++;
                //continue;
                System.exit(0);
            }

            rp = getPrecisionRecall(correct, sortedCases, numMatchingCases);
            updatePRCurves(rp, PRtNCD);

            try {
//TIME				long start = cpuTime();
                sortedCases = cdmHClass.sortTrainingCasesByDistanceToQuery(file);
//TIME				long cpuNano = cpuTime()-start;
//TIME				double cpuSec = cpuNano / 1000000000.0;
//TIME				System.out.print("\t" + cpuSec);
            } catch (IOException e) {
                System.out.println("Error: Failed to open testcase.  EXITING.");
                //countSkipped++;
                //continue;
                System.exit(0);
            }

            rp = getPrecisionRecall(correct, sortedCases, numMatchingCases);
            updatePRCurves(rp, PRhCDM);

            try {
//TIME				long start = cpuTime();
                sortedCases = cdmTClass.sortTrainingCasesByDistanceToQuery(file);
//TIME				long cpuNano = cpuTime()-start;
//TIME				double cpuSec = cpuNano / 1000000000.0;
//TIME				System.out.println("\t" + cpuSec);
            } catch (IOException e) {
                System.out.println("Error: Failed to open testcase.  EXITING.");
                //countSkipped++;
                //continue;
                System.exit(0);
            }

            rp = getPrecisionRecall(correct, sortedCases, numMatchingCases);
            updatePRCurves(rp, PRtCDM);
        }
        System.out.println("#STATUS: Processing complete.  Generating graph data.");
        int N = fileNames.size() - countSkipped;
        ave(N, PRhNCD);
        ave(N, PRtNCD);
        ave(N, PRhCDM);
        ave(N, PRtCDM);

        double interval = 1.0 / (PRhNCD.length - 1);
        System.out.println("#");
        System.out.println("#R\thNCD\ttNCD\thCDM\ttCDM");
        if (out != null) out.println("#R\thNCD\ttNCD\thCDM\ttCDM");
        for (int i = 0; i < PRhNCD.length; i++) {
            if (out != null)
                out.println((i * interval) + "\t" + PRhNCD[i] + "\t" + PRtNCD[i] + "\t" + PRhCDM[i] + "\t" + PRtCDM[i]);
            System.out.println((i * interval) + "\t" + PRhNCD[i] + "\t" + PRtNCD[i] + "\t" + PRhCDM[i] + "\t" + PRtCDM[i]);
        }


    }

/*	TIME
    private static ThreadMXBean threadBean;
	private static void timingInit() {
		threadBean = ManagementFactory.getThreadMXBean();
	}
	
	private static long cpuTime() {
		return threadBean.getCurrentThreadCpuTime();
	}
	*/


    private static void updatePRCurves(double[][] rp, double[] curve) {
        //double interval = 1.0 / (curve.length-1);
        double denom = curve.length - 1;
        int j = 0;
        for (int i = 0; i < rp[0].length; i++) {
            //int end = (int)(rp[0][i] / interval);
            for (; (j / denom) <= rp[0][i]; j++) {
                curve[j] += rp[1][i];
            }
            //for ( ; j <= end; j++) {
            //	curve[j] += rp[1][i];
            //}
        }
    }

    private static void ave(int N, double[] curve) {
        for (int i = 0; i < curve.length; i++) {
            curve[i] /= N;
        }
    }


    private static double[][] getPrecisionRecall(String correct,
                                                 ArrayList<String> sortedCases, int numMatchingCases) {

        int tp = 0;
        double numMatched = numMatchingCases;

        double[][] rp = new double[2][numMatchingCases + 1];
        int rIndex = 0;

        for (int i = 0; i < sortedCases.size(); i++) {
            if (sortedCases.get(i).equalsIgnoreCase(correct)) tp++;

            double r = tp / numMatched;
            if (r > rp[0][rIndex]) {
                rIndex++;
                rp[0][rIndex] = r;
            }
            double p = tp / (i + 1.0);
            for (int j = rIndex; j >= 0; j--) {
                if (p > rp[1][j]) rp[1][j] = p;
                else break;
            }
        }

        return rp;
    }


    private static int countMatchingCases(String correct,
                                          ArrayList<String> sortedCases) {
        int count = 0;

        for (String s : sortedCases) {
            if (s.equalsIgnoreCase(correct)) count++;
        }

        return count;
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
