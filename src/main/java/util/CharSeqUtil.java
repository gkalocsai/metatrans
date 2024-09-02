package util;

public class CharSeqUtil {

    public static String[] sentences(String text) {

        int sentenceCounter = 0;

        int i = indexOfNextSentenceSeparator(text, 0) + 1;

        if (i > 0)
            sentenceCounter++;
        while (i > 0) {
            i = indexOfNextSentenceSeparator(text, i) + 1;
            sentenceCounter++;

        }
        String[] result = new String[sentenceCounter];

        int start = 0;
        sentenceCounter = 0;
        i = indexOfNextSentenceSeparator(text, start);
        result[sentenceCounter++] = text.substring(start, i);
        start = i + 1;
        while (i > 0) {
            i = indexOfNextSentenceSeparator(text, i) + 1;
            if (i <= 0)
                break;
            result[sentenceCounter++] = text.substring(start, i);
            start = i;
        }
        return result;
    }

    private static int indexOfNextSentenceSeparator(String text, int i) {
        if (i >= text.length())
            return -1;
        int k;
        for (k = i; k < text.length() - 2; k++) {
            char c = text.charAt(k);
            if ("?!.".contains("" + c)) {
                char c2 = text.charAt(k + 1);
                char c3 = text.charAt(k + 2);
                if (c2 == '\"')
                    c2 = c3;
                if (Character.isWhitespace(c2) || Character.isUpperCase(c2)) {
                    return k;
                }

            }
        }
        return k;
    }

    public static String trailingChars(String s, char trailingChar, int expectedLength) {
        int currentLength = s.length();
        if (expectedLength <= currentLength)
            return s.substring(0, expectedLength);
        int diff = expectedLength - currentLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diff; i++) {
            sb.append(trailingChar);
        }
        return s + sb.toString();
    }

    public static String leadingChars(String s, char leadingChar, int expectedLength) {
        int currentLength = s.length();
        if (expectedLength <= currentLength)
            return s.substring(0, expectedLength);
        int diff = expectedLength - currentLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diff; i++) {
            sb.append(leadingChar);
        }
        return sb.toString() + s;
    }

    public static boolean isNotEmpty(CharSequence s) {
        return !CharSeqUtil.isEmptyOrNull(s);
    }

    public static boolean isEmptyOrNull(CharSequence cs) {
        if (cs == null) {
            return true;
        } else {
            return cs.length() == 0;
        }
    }

    public static String removeMultilineComments(String s) {
        StringBuilder sb = new StringBuilder();
        boolean insideComment = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!insideComment && ch == '/' && isCharAtIndex('*', s, i + 1)) {
                i++;
                insideComment = true;
            } else if (insideComment && ch == '*' && isCharAtIndex('/', s, i + 1)) {
                i++;
                insideComment = false;
            } else if (!insideComment && (ch == '\"' || ch == '\'')) {
                sb.append(ch);
                int last = getClosingQuotaInEscaped(s, i);
                if (last < 0) {
                    sb.append(s.substring(i + 1));
                    return sb.toString();
                } else {
                    sb.append(s.substring(i + 1, last + 1));
                }

                i = last;
            } else if (!insideComment) {
                sb.append(ch);
            }

        }
        return sb.toString();
    }

    private static boolean isCharAtIndex(char c, CharSequence seq, int i) {
        if (i < 0 || i >= seq.length())
            return false;
        return seq.charAt(i) == c;
    }

    public static int getClosingIndexInEscaped(CharSequence s, int openingIndex) {

        if (s == null || openingIndex < 0 || openingIndex >= s.length())
            return -1;

        char openingChar = s.charAt(openingIndex);

        if (isOpeningBrace(openingChar)) {
            return getClosingBraceIndexInEscaped(s, openingIndex);
        } else if (isQuota(openingChar)) {
            return getClosingQuotaInEscaped(s, openingIndex);
        } else {
            return -1;
        }
    }

    public static int getClosingQuotaInEscaped(CharSequence str, int openingQuotaIndex) {
        char opening = str.charAt(openingQuotaIndex);

        for (int i = openingQuotaIndex + 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\\') {
                i++;
                continue;
            }
            if (ch == opening) {
                return i;
            }
        }
        return -1;
    }

    public static int getNonQuotedIndex(CharSequence str, CharSequence pattern, int from) {

        boolean insideSingle = false;
        boolean insideDouble = false;

        for (int i = from; i < str.length(); i++) {
            char c = str.charAt(i);
            if (insideSingle && c == '\'') {
                insideSingle = false;
            } else if (insideDouble && c == '\"') {
                insideDouble = false;
            } else if (c == '\'' && !insideDouble) {
                insideSingle = true;
            } else if (c == '\"' && !insideSingle) {
                insideDouble = true;
            } else if (!insideSingle && !insideDouble) {
                if (matches(str, i, pattern))
                    return i;
            }
        }
        return -1;
    }

    private static boolean matches(CharSequence str, int from, CharSequence pattern) {
        if (str.length() - from < pattern.length())
            return false;
        for (int i = 0; i < pattern.length(); i++) {
            if (str.charAt(i + from) != pattern.charAt(i))
                return false;
        }
        return true;
    }

    private static int getClosingBraceIndexInEscaped(CharSequence str, int openingBraceIndex) {
        char opening = str.charAt(openingBraceIndex);
        char closing = getClosingBrace(opening);
        int level = 1;

        boolean insideSingle = false;
        boolean insideDouble = false;

        for (int i = openingBraceIndex + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (insideSingle && c == '\'') {
                insideSingle = false;
            } else if (insideDouble && c == '\"') {
                insideDouble = false;
            } else if (c == '\'' && !insideDouble) {
                insideSingle = true;
            } else if (c == '\"' && !insideSingle) {
                insideDouble = true;
            } else if (!insideSingle && !insideDouble && c == opening) {
                level++;
            } else if (!insideSingle && !insideDouble && c == closing) {
                level--;
                if (level == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean isOpeningBrace(char c) {
        return "{([".contains("" + c);
    }

    private static boolean isQuota(char c) {
        return "\"\'".contains("" + c);
    }

    private static char getClosingBrace(char opening) {
        String openingChars = "{[(";
        String closingPairs = "}])";
        int closingIndex = openingChars.indexOf(opening);
        if (closingIndex < 0) {
            throw new RuntimeException("Illegal opening brace: " + opening);
        }
        return closingPairs.charAt(closingIndex);
    }

    public static String resolveFormattedSeq(CharSequence input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '\\' && c != '[') {
                sb.append(c);
            } else if (input.charAt(i) == '\\') {
                if (i + 1 == input.length()) {
                    sb.append("\\");
                    return sb.toString();
                }
                char next = input.charAt(i + 1);
                sb.append(resolveEscapedChar(next));
                i++;
            } else if (input.charAt(i) == '[') {
                sb.append((char) resolveCharcode(input, i + 1));
                i = getClosingIndexInEscaped(input, i);
            }
        }
        return sb.toString();
    }

    private static int resolveCharcode(CharSequence input, int startIndex) {
        StringBuilder sb = new StringBuilder();
        if (startIndex < 0 || startIndex >= input.length()) {
            return -1;
        } else {
            for (int i = startIndex; i < input.length(); i++) {
                char d = input.charAt(i);
                if (d >= '0' && d <= '9') {
                    sb.append(d);
                } else {
                    return Integer.valueOf(sb.toString());
                }
            }
        }
        return Integer.valueOf(sb.toString());
    }

    private static char resolveEscapedChar(char next) {
        String input = "nrts";
        String output = "\n\r\t ";
        int outputindex = input.indexOf(next);
        if (outputindex < 0)
            return next;
        else
            return output.charAt(outputindex);
    }

}
