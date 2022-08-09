package org.zkoss.web.util.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maicon Mauricio
 */
public class TagDehyphen {
    private static final Pattern TAG_PATTERN = Pattern.compile("((</?)(\\w+:?)?([\\w-]+)(/?>))");

    public static String dehyphen(String content) {
        Matcher matcher = TAG_PATTERN.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String tag = matcher.group(1);
            matcher.appendReplacement(sb, tag.replaceAll("-", ""));
        }
        return sb.toString();
    }
}
