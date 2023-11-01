public class MatchUtil {
    public static String FormatMatchString(final String str) {
        final StringBuilder matcher = new StringBuilder();
        for (String word : str.split(" ")) {
            if (word == null) continue;

            // Filter out any special characters that might break the query
            word = word.replaceAll("[*+\\-\"'~()@<>]", "");
            if (word.isBlank()) continue;

            //Process the autocomplete string and set query
            //Each word is a prefix (append * to each word)
            //Each word is required (prepend + to each word)
            matcher.append("+").append(word).append("* ");
        }

        return matcher.toString();
    }
}
