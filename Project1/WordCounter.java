import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Simple word counter program that counts the number of different words in a
 * text and print out a table in html.
 *
 * @author Peter Luo
 */
public final class WordCounter {

    /**
     * Simple Comparator nested class to compare two Strings in lexicographical
     * order.
     *
     * @author Peter Luo
     *
     */
    private static class StringLT implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {

            return s1.compareTo(s2);
        }

    }

    /**
     * Default constructor--private to prevent instantiation.
     */
    private WordCounter() {
        // no code needed here
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        for (int i = 0; i < str.length(); i++) {
            if (!charSet.contains(str.charAt(i))) {
                charSet.add(str.charAt(i));
            }
        }
    }

    /**
     * Return the first "word" which defines as a string of letters from the
     * beginning index to a separator in the input String.
     *
     * @param text
     *            the String that words will be extracted from
     * @param position
     *            the initial position in the String that starts searching
     * @param separators
     *            a set of separators that separate "words"
     * @param finalPos
     *            copy back the endPos to the parameter
     * @return return the first "word" appears in the String
     */
    private static String nextWord(String text, int position,
            Set<Character> separators, int[] finalPos) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int startPos = position; //the start position
        int endPos = startPos; //the end position
        boolean flag = true; //indicate whether the word should be returned
        String result = "";

        while (startPos < text.length() && endPos <= text.length() && flag) {
            /*
             * Make sure the character at the start point is that a separator
             */
            if (separators.contains(text.charAt(startPos))) {
                startPos++;
                endPos++;
            } else {
                /*
                 * Substring if there's no separator even if Pos reaches the end
                 */
                if (endPos == text.length()) {

                    if (!separators.contains(text.charAt(endPos - 1))) {
                        result = text.substring(startPos, endPos);
                        flag = false;
                    } else {
                        result = text.substring(startPos, endPos - 1);
                        flag = false;
                    }
                } else {
                    /*
                     * Substring the word when meets a separator
                     */
                    if (separators.contains(text.charAt(endPos))) {
                        result = text.substring(startPos, endPos);
                        flag = false;
                    } else {
                        endPos++;
                    }
                }
            }

        }
        /*
         * Copy the endPos back to the parameter
         */
        finalPos[0] = endPos;

        return result;
    }

    /**
     * Generates a map to store the words and the number of times of appearance.
     *
     * @param in
     *            the reader of the input text file
     * @param separators
     *            a set of separators to separate words
     * @return return a map with words and number of appearance
     */
    private static Map<String, Integer> generateMap(SimpleReader in,
            Set<Character> separators) {

        Map<String, Integer> map = new Map1L<>();
        String nextLine = ""; //the line of String that simple reader reads
        String nextWord = ""; //the word returned from the line
        int index = 0; //the position of the pointer
        int[] arr = new int[1]; //the end position
        while (!in.atEOS()) {
            nextLine = in.nextLine();
            while (index < nextLine.length()) {
                nextWord = nextWord(nextLine, index, separators, arr);
                if (map.hasKey(nextWord)) {
                    map.replaceValue(nextWord, map.value(nextWord) + 1);
                } else {
                    map.add(nextWord, 1);
                }
                index = arr[0] + 1; //start search the next word from the last end point
            }
            index = 0;
        }
        return map;
    }

    /**
     * Write a html page based on the map.
     *
     * @param fileName
     *            the output file name
     * @param inputFile
     *            the input file name
     * @param map
     *            the map stores the words and appear times
     */
    private static void processPage(String fileName, String inputFile,
            Map<String, Integer> map) {

        SimpleWriter out = new SimpleWriter1L(fileName);
        Queue<String> sortedList = new Queue1L<>();
        Map<String, Integer> tempMap = map.newInstance();
        tempMap.transferFrom(map);

        /*
         * Iterate over the map
         */
        while (tempMap.size() > 0) {
            Map.Pair<String, Integer> nextPair = tempMap.removeAny();
            sortedList.enqueue(nextPair.key());
            map.add(nextPair.key(), nextPair.value());
        }

        /*
         * Sort the queue based on alphabetical order.
         */
        Comparator<String> ci = new StringLT();
        sortedList.sort(ci);

        out.println("<html>");
        out.println("<head>");
        out.println("<title>");
        out.println("Words Counted in " + inputFile);
        out.println("</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>");
        out.println("Words Counted in " + inputFile);
        out.println("</h2>");
        out.println("<hr />");
        out.println("<table border =\"1\">");
        out.println("<tr>");
        out.print("<th>");
        out.print("Words");
        out.println("</th>");
        out.print("<th>");
        out.print("Counts");
        out.println("</th>");
        out.println("</tr>");

        while (sortedList.length() > 0) {
            String nextWord = sortedList.dequeue();
            int value = map.value(nextWord);

            out.println("<tr>");
            out.print("<td>");
            out.print(nextWord);
            out.println("</td>");
            out.print("<td>");
            out.print(value);
            out.println("</td>");
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</body>");
        out.print("</html>");

        out.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {

        SimpleReader in = new SimpleReader1L();
        SimpleWriter write = new SimpleWriter1L();

        /*
         * Ask for the name for input and output file name
         */
        write.println("Please type the name of your input file");
        String inputName = in.nextLine();
        write.println(
                "Please provide the name you want for your output html file");
        String outputName = in.nextLine();
        SimpleReader read = new SimpleReader1L(inputName);

        Set<Character> characterSet = new Set1L<>();
        generateElements("! ,.-\";_?<>", characterSet); //create separator set
        Map<String, Integer> repository = generateMap(read, characterSet);

        processPage(outputName, inputName, repository);

        /*
         * Close streams
         */
        in.close();
        write.close();
    }
}
