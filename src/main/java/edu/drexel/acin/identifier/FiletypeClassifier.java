package edu.drexel.acin.identifier;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Compression based classifier implementation.
 *
 * @author Vincent Cicirello (Richard Stockton College)
 * @version March 2013
 */
public class FiletypeClassifier {

    public static final int NCD_DISTANCE = 0;
    public static final int CDM_DISTANCE = 1;

    private ArrayList<FiletypeExample> examples;
    private int k;
    private int whichDistance;
    private boolean weighted;


    /**
     * Constructor
     */
    public FiletypeClassifier() {
        examples = new ArrayList<FiletypeExample>();
        k = 1;
        whichDistance = NCD_DISTANCE;
        weighted = false;

    }

    /**
     * Constructor
     *
     * @param filename File containing records consisting of filename and corresponding
     *                 compressed sizes, and file types.
     * @throws FileNotFoundException
     */
    public FiletypeClassifier(String filename) throws FileNotFoundException {
        this();

        Scanner scan = new Scanner(new File(filename));
        String token = scan.nextLine();
        if (!token.equals("(FiletypeClassifier")) {
            scan.close();
            throw new IllegalArgumentException("The file, " + filename + ", is not correctly formatted!");
        }
        while (scan.hasNextLine()) {
            token = scan.nextLine().trim();
            if (token.length() == 0) continue;


            if (token.equals(")")) {
                break;
            } else if (token.contains("(K") || token.contains("(WK")) {
                String[] pair = token.split("\\s+");
                if (!pair[0].equals("(K") && !pair[0].equals("(WK")) {
                    scan.close();
                    throw new IllegalArgumentException("Undefined entry in classifier file, " + filename);
                }
                if (pair[0].equals("(WK")) weighted = true;
                pair = pair[1].trim().split("\\)");
                k = Integer.parseInt(pair[0]);
                continue;
            } else if (token.contains("(DISTANCE")) {
                String[] pair = token.split("\\s+");
                if (!pair[0].equals("(DISTANCE")) {
                    scan.close();
                    throw new IllegalArgumentException("Undefined entry in classifier file, " + filename);
                }
                pair = pair[1].trim().split("\\)");
                whichDistance = Integer.parseInt(pair[0]);
                continue;
            } else if (!token.equals("(FiletypeExample") && !token.equals("(class")) {
                scan.close();
                throw new IllegalArgumentException("At least one record in the file, " + filename + ", is not correctly formatted!");
            }

            if (token.equals("(FiletypeExample")) {
                String file = "";
                int size = 0;
                String type = "";

                token = scan.nextLine();
                String[] pair = token.split("=");
                if (!pair[0].equals("filename")) {
                    scan.close();
                    throw new IllegalArgumentException("At least one record in the file, " + filename + ", is not correctly formatted!");
                } else {
                    file = pair[1];
                    if (pair.length > 2) {
                        for (int i = 2; i < pair.length; i++) {
                            file += ("=" + pair[i]);
                        }
                    }
                }
                token = scan.nextLine();
                pair = token.split("[=,]");
                if (!pair[0].equals("compressedSize")) {
                    scan.close();
                    throw new IllegalArgumentException("At least one record in the file, " + filename + ", is not correctly formatted!");
                } else {
                    size = Integer.parseInt(pair[1]);
                }
                token = scan.nextLine();
                pair = token.split("[=,]");
                if (!pair[0].equals("filetype")) {
                    scan.close();
                    throw new IllegalArgumentException("At least one record in the file, " + filename + ", is not correctly formatted!");
                } else {
                    type = pair[1];
                }
                token = scan.nextLine();


                if (!token.equals(")")) {
                    scan.close();
                    throw new IllegalArgumentException("At least one record in the file, " + filename + ", is not correctly formatted!");
                }
                FiletypeExample nextOne = null;

                try {
                    nextOne = new FiletypeExample(file, type);
                } catch (IOException e) {

                    System.out.println("BAD FILE " + file);
                    continue;
//					throw new IllegalArgumentException("At least one record in the file, " + filename + ", is an unreadable file: " + file);
                }

                examples.add(nextOne);
            }
        }

        while (scan.hasNextLine()) {
            token = scan.nextLine();
            if (token.trim().length() > 0) {
                scan.close();
                throw new IllegalArgumentException("The file, " + filename + ", contains unexpected data after end of classifier definition!");
            }
        }
        scan.close();


    }

    /**
     * Counts number of training examples with a given class.
     *
     * @param classType the class
     * @return Number of training instances with the given classType
     */
    public int countClass(String classType) {
        int count = 0;
        for (FiletypeExample e : examples) {
            if (e.getFiletype().equalsIgnoreCase(classType)) count++;
        }
        return count;
    }

    public int getDistanceMeasure() {
        return whichDistance;
    }

    public boolean getIsWeighted() {
        return weighted;
    }


    /**
     * Saves the classifier's state to a textfile.
     *
     * @param filename Output filename.
     * @throws FileNotFoundException
     */
    public void saveClassifierToFile(String filename) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(filename);
        out.println(toString());
        out.close();
    }

    /**
     * Adds an example to the classifier.
     *
     * @param example The new example to add to the classifier.
     * @throws IOException
     */
    protected void addToClassifier(FiletypeExample example) throws IOException {

        examples.add(example);
    }


    /**
     * Trains the classifier from a list of instances.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @throws FileNotFoundException
     */
    public void train(String trainingDataFile, int distanceMeasure) throws FileNotFoundException {
        train(trainingDataFile);
        whichDistance = distanceMeasure;
    }


    /**
     * Trains the classifier from a list of instances.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @throws FileNotFoundException
     */
    public void train(String trainingDataFile) throws FileNotFoundException {
        Scanner scan = new Scanner(new File(trainingDataFile));
        String[] pair = new String[2];
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            if (line.length() == 0) continue;
            //String[] pair = new String[2]; //line.split("\\s+");
            if (line.lastIndexOf("\t".charAt(0)) < 0) { //if (pair.length != 2) {
                scan.close();
                throw new IllegalArgumentException("Training data file not formatted correctly.");
            }
            pair[0] = line.substring(0, line.lastIndexOf("\t".charAt(0)));
            pair[1] = line.substring(line.lastIndexOf("\t".charAt(0)) + 1);
            try {
                addToClassifier(new FiletypeExample(pair[0], pair[1]));
            } catch (IOException e) {
                System.out.println("Error in training instance: " + pair[0] + "\t" + pair[1]);
            }

        }
        scan.close();
        k = 1;
    }


    /**
     * Trains the classifier from a list of instances.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @throws FileNotFoundException
     */
    public void train(Map<String, String> samples) throws FileNotFoundException {
        for (Map.Entry<String, String> e : samples.entrySet()) {
            try {
                addToClassifier(new FiletypeExample(e.getKey(), e.getValue()));
            } catch (IOException ex) {
                System.out.println("Error in training instance: " + e.getKey() + "\t" + e.getValue());
            }
        }
        k = 1;
    }

    /**
     * Trains the classifier from a list of instances, using leave one out cross validation
     * for determining k.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @param distanceMeasure  The distance measure to train on.
     * @throws IOException
     */
    public void trainKNearest(Map<String, String> samples, int distanceMeasure) throws IOException {
        train(samples);
        whichDistance = distanceMeasure;


        int[] correctClassCounts = new int[examples.size()]; // new version

        for (int i = examples.size() - 1; i >= 0; i--) {
            Collections.swap(examples, i, examples.size() - 1);
            FiletypeExample last = examples.remove(examples.size() - 1);

            updateKCounts(last, whichDistance, correctClassCounts); // new version


            examples.add(last);
            gc();
        }

        int bestK = 1;
        for (int i = 2; i < correctClassCounts.length; i++) {
            if (correctClassCounts[i] > correctClassCounts[bestK]) {
                bestK = i;
            }
        }
        k = bestK;


    }

    /**
     * Trains the classifier from a list of instances, using leave one out cross validation
     * for determining k.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @param distanceMeasure  The distance measure to train on.
     * @throws IOException
     */
    public void trainKNearest(String trainingDataFile, int distanceMeasure) throws IOException {
        train(trainingDataFile);
        whichDistance = distanceMeasure;


        int[] correctClassCounts = new int[examples.size()]; // new version

        for (int i = examples.size() - 1; i >= 0; i--) {
            Collections.swap(examples, i, examples.size() - 1);
            FiletypeExample last = examples.remove(examples.size() - 1);

            updateKCounts(last, whichDistance, correctClassCounts); // new version


            examples.add(last);
            gc();
        }

        int bestK = 1;
        for (int i = 2; i < correctClassCounts.length; i++) {
            if (correctClassCounts[i] > correctClassCounts[bestK]) {
                bestK = i;
            }
        }
        k = bestK;


    }

    /**
     * Trains the classifier from a list of instances, using leave one out cross validation
     * for determining k.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @param distanceMeasure  The distance measure to train on.
     * @throws IOException
     */
    public void trainWeightedKNearest(String trainingDataFile, int distanceMeasure) throws IOException {
        train(trainingDataFile);
        whichDistance = distanceMeasure;
        weighted = true;


        int[] correctClassCounts = new int[examples.size()]; // new version

        for (int i = examples.size() - 1; i >= 0; i--) {
            Collections.swap(examples, i, examples.size() - 1);
            FiletypeExample last = examples.remove(examples.size() - 1);

            updateKCountsWeighted(last, whichDistance, correctClassCounts); // new version


            examples.add(last);
            gc();
        }

        int bestK = 1;
        for (int i = 2; i < correctClassCounts.length; i++) {
            if (correctClassCounts[i] > correctClassCounts[bestK]) {
                bestK = i;
            }
        }
        k = bestK;


    }

    /**
     * Trains the classifier from a list of instances, using leave one out cross validation
     * for determining k.
     *
     * @param trainingDataFile A text file consisting
     *                         of one instance per line formatted as: filename <tab> filetype.
     * @param distanceMeasure  The distance measure to train on.
     * @throws IOException
     */
    public void trainWeightedKNearest(Map<String, String> samples, int distanceMeasure) throws IOException {
        train(samples);
        whichDistance = distanceMeasure;
        weighted = true;


        int[] correctClassCounts = new int[examples.size()]; // new version

        for (int i = examples.size() - 1; i >= 0; i--) {
            Collections.swap(examples, i, examples.size() - 1);
            FiletypeExample last = examples.remove(examples.size() - 1);

            updateKCountsWeighted(last, whichDistance, correctClassCounts); // new version


            examples.add(last);
            gc();
        }

        int bestK = 1;
        for (int i = 2; i < correctClassCounts.length; i++) {
            if (correctClassCounts[i] > correctClassCounts[bestK]) {
                bestK = i;
            }
        }
        k = bestK;


    }


    /**
     * Uses nearest neighbor to predict class.
     *
     * @param example
     * @param distanceMeasure The distance measure to use for prediction
     * @return Name of class of nearest neighbor.
     * @throws IOException
     */
    private String predictClass(FiletypeExample example, int distanceMeasure) throws IOException {

        ArrayList<String> classNames = new ArrayList<String>(5);
        ArrayList<Double> distances = new ArrayList<Double>(5);

        for (FiletypeExample e : examples) {

            double distance = 0;
            switch (distanceMeasure) {
                case NCD_DISTANCE:
                    distance = e.NCD(example);
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(example);
                    break;
            }
            if (distances.size() < 5 || distance < distances.get(4)) {
                int where = classNames.indexOf(e.getFiletype());
                if (where >= 0) {
                    if (distance < distances.get(where)) {
                        classNames.remove(where);
                        distances.remove(where);
                        where = -1;
                    }
                }
                if (where < 0) {
                    int insertionPoint = Collections.binarySearch(distances, distance);
                    if (insertionPoint < 0) {
                        insertionPoint = -(insertionPoint + 1);
                    } else {
                        insertionPoint++;
                    }
                    classNames.add(insertionPoint, e.getFiletype());
                    distances.add(insertionPoint, distance);
                    if (distances.size() > 5) {
                        distances.remove(5);
                        classNames.remove(5);
                    }
                }
            }

        }

        int M = Math.min(distances.size(), 5);
        String result = "";
        for (int i = 0; i < M; i++) {
            result += classNames.get(i); // + "\t" + distances.get(i);
            if (i < M - 1) result += "\t";
        }

        return result; //className;
    }


    private void updateKCounts(FiletypeExample ex, int distanceMeasure,
                               int[] correctClassCounts) throws IOException {
        int maxVotes = 0;
        String className = "";
        class Pair implements Comparable<Pair> {
            String type;
            double dist;

            Pair(String type, double dist) {
                this.type = type;
                this.dist = dist;
            }

            public int compareTo(Pair other) {
                double diff = dist - other.dist;
                if (diff < 0) return -1;
                else if (diff > 0) return 1;
                else return 0;
            }
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ArrayList<Pair> distances = new ArrayList<Pair>(examples.size());

        for (FiletypeExample e : examples) {
            double distance = 0;
            switch (distanceMeasure) {
                case NCD_DISTANCE:
                    distance = e.NCD(ex);
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(ex);
                    break;
            }
            distances.add(new Pair(e.getFiletype(), distance));
        }

        Collections.sort(distances);

        for (int i = 0; i < distances.size(); i++) {
            Integer val = map.get(distances.get(i).type);
            if (val == null) {
                val = 1;
            } else {
                val = val + 1;
            }
            map.put(distances.get(i).type, val);
            if (val > maxVotes) {
                maxVotes = val;
                className = distances.get(i).type;
            }
            if (className.equals(ex.getFiletype())) {
                correctClassCounts[i + 1]++;
            }
        }
    }


    private void updateKCountsWeighted(FiletypeExample ex, int distanceMeasure,
                                       int[] correctClassCounts) throws IOException {

        double maxVotes = 0;
        String className = "";
        class Pair implements Comparable<Pair> {
            String type;
            double dist;

            Pair(String type, double dist) {
                this.type = type;
                this.dist = dist;
            }

            public int compareTo(Pair other) {
                double diff = dist - other.dist;
                if (diff < 0) return -1;
                else if (diff > 0) return 1;
                else return 0;
            }
        }

        HashMap<String, Double> map = new HashMap<String, Double>();
        ArrayList<Pair> distances = new ArrayList<Pair>(examples.size());

        boolean exactMatch = false;
        int matchCount = 0;
        for (FiletypeExample e : examples) {
            double distance = 0;
            switch (distanceMeasure) {
                case NCD_DISTANCE:
                    distance = e.NCD(ex);
                    if (distance <= 0.0) {
                        exactMatch = true;
                        matchCount++;
                    }
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(ex);
                    if (distance <= 0.5) {
                        exactMatch = true;
                        matchCount++;
                    }
                    break;
            }
            distances.add(new Pair(e.getFiletype(), distance));
        }

        Collections.sort(distances);

        if (!exactMatch) {
            for (int i = 0; i < distances.size(); i++) {
                Double val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1.0 / distances.get(i).dist;
                } else {
                    val = val + 1.0 / distances.get(i).dist;
                }
                map.put(distances.get(i).type, val);
                if (val > maxVotes) {
                    maxVotes = val;
                    className = distances.get(i).type;
                }
                if (className.equals(ex.getFiletype())) {
                    correctClassCounts[i + 1]++;
                }
            }
        } else {
            for (int i = 0; i < matchCount; i++) {
                Double val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1.0;
                } else {
                    val = val + 1.0;
                }
                map.put(distances.get(i).type, val);
                if (val > maxVotes) {
                    maxVotes = val;
                    className = distances.get(i).type;
                }
                if (className.equals(ex.getFiletype())) {
                    correctClassCounts[i + 1]++;
                }
            }
            for (int i = matchCount; i < distances.size(); i++) {
                if (className.equals(ex.getFiletype())) {
                    correctClassCounts[i + 1]++;
                }
            }

        }

    }


    /**
     * Generates an array of format type names associated with the
     * training cases of the classifier after sorting cases by
     * distance to the query example (nearest first).
     *
     * @param queryFilename The query case.
     * @return An array of type names.
     * @throws IOException
     */
    public ArrayList<String> sortTrainingCasesByDistanceToQuery(String queryFilename) throws IOException {
        FiletypeExample ex = new FiletypeExample(queryFilename);

        ArrayList<String> result = new ArrayList<String>(examples.size());
        ArrayList<Double> distances = new ArrayList<Double>(examples.size());

        for (FiletypeExample e : examples) {

            double distance = 0;
            switch (whichDistance) {
                case NCD_DISTANCE:
                    distance = e.NCD(ex);
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(ex);
                    break;
            }
            int insertionPoint = Collections.binarySearch(distances, distance);
            if (insertionPoint < 0) {
                insertionPoint = -(insertionPoint + 1);
            } else {
                insertionPoint++;
            }
            result.add(insertionPoint, e.getFiletype());
            distances.add(insertionPoint, distance);
        }
        return result;
    }

    /**
     * Predicts class of the example using the distance measure and value of k
     * within the classifier.
     *
     * @param exampleFilename
     * @return
     * @throws IOException
     */
    public String predictClass(String exampleFilename) throws IOException {
        FiletypeExample ex = new FiletypeExample(exampleFilename);
        return predictClass(ex);
    }

    /**
     * Predicts class of the example using the distance measure and value of k
     * within the classifier.
     *
     * @param exampleFilename
     * @return
     * @throws IOException
     */
    public String predictClass(InputStream fileStream) throws IOException {
        FiletypeExample ex = new FiletypeExample(fileStream);
        return predictClass(ex);
    }

    /**
     * Predicts class of the example using the distance measure and value of k
     * within the classifier.
     *
     * @param example
     * @return
     * @throws IOException
     */
    private String predictClass(FiletypeExample example) throws IOException {
        if (weighted) {
            return predictClass(example, whichDistance, k, weighted);
        } else {
            return predictClass(example, whichDistance, k);
        }
    }

    /**
     * Uses k-nearest neighbor to predict class.
     *
     * @param example
     * @param distanceMeasure The distance measure to use for prediction
     * @param k               The value of k to use.
     * @return Name of class of chosen by a majority vote of the k nearest neighbors.
     * @throws IOException
     */
    private String predictClass(FiletypeExample example, int distanceMeasure, int k, boolean weighted) throws IOException {

        if (k <= 0) throw new IllegalArgumentException("k must be positive.");
        if (k == 1) return predictClass(example, distanceMeasure);
        if (!weighted) return predictClass(example, distanceMeasure, k);

        double maxVotes = 0;
        String className = "";

        class Pair implements Comparable<Pair> {
            String type;
            double dist;

            Pair(String type, double dist) {
                this.type = type;
                this.dist = dist;
            }

            public int compareTo(Pair other) {
                double diff = dist - other.dist;
                if (diff < 0) return -1;
                else if (diff > 0) return 1;
                else return 0;
            }
        }

        HashMap<String, Double> map = new HashMap<String, Double>();
        ArrayList<Pair> distances = new ArrayList<Pair>(examples.size());

        boolean exactMatch = false;
        int matchCount = 0;
        for (FiletypeExample e : examples) {
            double distance = 0;
            switch (distanceMeasure) {
                case NCD_DISTANCE:
                    distance = e.NCD(example);
                    if (distance <= 0.0) {
                        exactMatch = true;
                        matchCount++;
                    }
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(example);
                    if (distance <= 0.5) {
                        exactMatch = true;
                        matchCount++;
                    }
                    break;
            }
            distances.add(new Pair(e.getFiletype(), distance));
        }

        Collections.sort(distances);


        int count = 0;
        if (!exactMatch) {
            for (int i = 0; i < k && i < distances.size(); i++) {
                Double val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1.0 / distances.get(i).dist;
                    count++;
                } else {
                    val = val + 1.0 / distances.get(i).dist;
                }
                map.put(distances.get(i).type, val);
                if (val > maxVotes) {
                    maxVotes = val;
                    className = distances.get(i).type;
                }
            }
        } else {
            for (int i = 0; i < matchCount; i++) {
                Double val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1.0;
                    count++;
                } else {
                    val = val + 1.0;
                }
                map.put(distances.get(i).type, val);
                if (val > maxVotes) {
                    maxVotes = val;
                    className = distances.get(i).type;
                }
            }
        }

        if (count < 5) {
            if (exactMatch && matchCount < distances.size()) {
                int matchVal = 2;
                if (distanceMeasure != CDM_DISTANCE) {
                    matchVal = (int) Math.ceil(1.0 / distances.get(matchCount).dist);
                    matchVal = (int) Math.pow(10, Math.ceil(Math.log10(matchVal)) + 1);
                }
                for (int i = 0; i < matchCount; i++) {
                    Double val = map.get(distances.get(i).type);
                    val += (matchVal - 1);
                    map.put(distances.get(i).type, val);
                }
            }

            for (int i = (exactMatch) ? matchCount : k; i < distances.size() && count < 5; i++) {
                Double val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1.0 / distances.get(i).dist;
                    count++;
                } else {
                    val = val + 1.0 / distances.get(i).dist;
                }
                map.put(distances.get(i).type, val);
            }
        }

        final String maxClass3 = className;

        class Vote implements Comparable<Vote> {
            double vote;
            String type;

            Vote(String t, double v) {
                vote = v;
                type = t;
            }

            public int compareTo(Vote other) {
                if (type.equals(maxClass3)) {
                    return Integer.MIN_VALUE;
                } else if (other.type.equals(maxClass3)) {
                    return Integer.MAX_VALUE;
                }
                double diff = other.vote - vote;
                if (diff < 0) return -1;
                else if (diff > 0) return 1;
                else return 0;
            }
        }

        Set<String> keys = map.keySet();
        ArrayList<Vote> topFive = new ArrayList<Vote>();
        for (String e : keys) topFive.add(new Vote(e, map.get(e)));
        Collections.sort(topFive);

        if (!className.equals(topFive.get(0).type)) {
            throw new IllegalStateException("vote counting failed");
        }

        String result = ""; //className + "\t" + maxVotes + "\t";

        for (Vote e : topFive) {
            result += e.type + "\t"; // + e.vote + "\t";
        }
        result = result.substring(0, result.length() - 1);

        return result; //className;
    }


    /**
     * Uses k-nearest neighbor to predict class.
     *
     * @param example
     * @param distanceMeasure The distance measure to use for prediction
     * @param k               The value of k to use.
     * @return Name of class of chosen by a majority vote of the k nearest neighbors.
     * @throws IOException
     */
    private String predictClass(FiletypeExample example, int distanceMeasure, int k) throws IOException {

        if (k <= 0) throw new IllegalArgumentException("k must be positive.");
        if (k == 1) return predictClass(example, distanceMeasure);

        int maxVotes = 0;
        String className = "";

        class Pair implements Comparable<Pair> {
            String type;
            double dist;

            Pair(String type, double dist) {
                this.type = type;
                this.dist = dist;
            }

            public int compareTo(Pair other) {
                double diff = dist - other.dist;
                if (diff < 0) return -1;
                else if (diff > 0) return 1;
                else return 0;
            }
        }


        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ArrayList<Pair> distances = new ArrayList<Pair>(examples.size());

        for (FiletypeExample e : examples) {
            double distance = 0;
            switch (distanceMeasure) {
                case NCD_DISTANCE:
                    distance = e.NCD(example);
                    break;
                case CDM_DISTANCE:
                    distance = e.CDM(example);
                    break;
            }
            distances.add(new Pair(e.getFiletype(), distance));
        }

        Collections.sort(distances);
        int count = 0;
        for (int i = 0; i < k && i < distances.size(); i++) {
            Integer val = map.get(distances.get(i).type);
            if (val == null) {
                val = 1;
                count++;
            } else {
                val = val + 1;
            }
            map.put(distances.get(i).type, val);
            if (val > maxVotes) {
                maxVotes = val;
                className = distances.get(i).type;
            }
        }

        if (count < 5) {
            for (int i = k; i < distances.size() && count < 5; i++) {
                Integer val = map.get(distances.get(i).type);
                if (val == null) {
                    val = 1;
                    count++;
                } else {
                    val = val + 1;
                }
                map.put(distances.get(i).type, val);
            }
        }

        final String maxClass3 = className;

        class Vote implements Comparable<Vote> {
            int vote;
            String type;

            Vote(String t, int v) {
                vote = v;
                type = t;
            }

            public int compareTo(Vote other) {
                if (type.equals(maxClass3)) {
                    return Integer.MIN_VALUE;
                } else if (other.type.equals(maxClass3)) {
                    return Integer.MAX_VALUE;
                }
                return other.vote - vote;
            }
        }

        Set<String> keys = map.keySet();
        ArrayList<Vote> topFive = new ArrayList<Vote>();
        for (String e : keys) topFive.add(new Vote(e, map.get(e)));
        Collections.sort(topFive);

        if (!className.equals(topFive.get(0).type)) {
            throw new IllegalStateException("vote counting failed");
        }

        String result = ""; //className + "\t" + maxVotes + "\t";

        for (Vote e : topFive) {
            result += e.type + "\t"; // + e.vote + "\t";
        }
        result = result.substring(0, result.length() - 1);

        return result; //className;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "(FiletypeClassifier\n";
        if (k > 1) {
            if (weighted) result += "(WK " + k + ")\n";
            else result += "(K " + k + ")\n";
        }
        if (k > 1 || whichDistance != NCD_DISTANCE) {
            result += "(DISTANCE " + whichDistance + ")\n";
        }
        for (FiletypeExample e : examples) {
            result += e.toString() + "\n";
        }
        result += ")";
        return result;
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
