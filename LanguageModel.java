import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /**
     * Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the
     * same seed value will produce the same random texts. Good for debugging.
     */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production.
     */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        In in = new In(fileName);
        String corpus = in.readAll();
        int corpusLength = corpus.length();
        for (int i = 0; i <= corpusLength - windowLength - 1; i++) {
            String window = corpus.substring(i, i + windowLength);
            char nextChar = corpus.charAt(i + windowLength);
            List charDataList;
            if (CharDataMap.containsKey(window)) {
                charDataList = CharDataMap.get(window);
            } else {
                charDataList = new List();
                CharDataMap.put(window, charDataList);
            }
            int charIndex = charDataList.indexOf(nextChar);
            if (charIndex != -1) {
                ListIterator iter = charDataList.listIterator(charIndex);
                iter.current.cp.count++;
            } else {
                charDataList.addFirst(nextChar);
            }
        }

        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    void calculateProbabilities(List probs) {
        if (probs.getSize() == 0) {
            return;
        }
        int totalCharsOccurences = 0;
        double accumulatedCp = 0;
        ListIterator currIter = probs.listIterator(0);
        while (currIter.hasNext()) {
            totalCharsOccurences += currIter.current.cp.count;
            currIter.next();
        }

        currIter = probs.listIterator(0);
        while (currIter.hasNext()) {
            currIter.current.cp.p = (double) currIter.current.cp.count / totalCharsOccurences;
            accumulatedCp += currIter.current.cp.p;
            currIter.current.cp.cp = accumulatedCp;
            currIter.next();
        }
    }

    // Returns a random character from the given probabilities list.
    char getRandomChar(List probs) {
        // If the list is empty, an exception should be thrown but
        // I don't want to change the method signature,
        // so dependents like tests will be broken
        double randomSample = this.randomGenerator.nextDouble();
        ListIterator iter = probs.listIterator(0);
        int listSize = probs.getSize();
        for (int i = 0; i < listSize; i++) {
            if (randomSample < iter.current.cp.cp) {
                break;
            }
            iter.next();
        }
        return iter.current.cp.chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during
     * training.
     * 
     * @param initialText     - text to start with. If initialText's last substring
     *                        of size numberOfLetters
     *                        doesn't appear as a key in Map, we generate no text
     *                        and return only the initial text.
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        // Your code goes here
        if(initialText.length() < windowLength) {
            return initialText;
        }
        while(initialText.length() < textLength) {
            String window = initialText.substring(initialText.length() - this.windowLength);
            if (!CharDataMap.containsKey(window)) {
                break;
            }
            List probs = CharDataMap.get(window);
            char nextChar = getRandomChar(probs);
            initialText += nextChar;
        }
        return initialText;
    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        // Your code goes here
    }
}
