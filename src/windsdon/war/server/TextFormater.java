package windsdon.war.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;

/**
 *
 * @author Windsdon
 */
public class TextFormater {

    private static int[] ia(int a, int b) {
        int[] r = {a, b};
        return r;
    }

    private static class ColorAttribute {

        public int start;
        public int end;
        public Color color;

        public ColorAttribute(int s, int e, Color c) {
            start = s;
            end = e;
            color = c;
        }
    }
    private static Color[] defaultColors = {
        Color.black,
        Color.white,
        Color.red,
        Color.darkGray,
        Color.lightGray,
        new Color(0x0000aa),
        new Color(0x00aa00),
        Color.yellow
    };

    public static AttributedString get(String raw, Color defaultColor, Font font) {
        //AttributedString r = new AttributedString(raw);
        //int 
        String text = raw.replaceAll("§\\[[0-9xabcdef]+\\]", "").replaceAll("§[a-z0-9]", "");
        if (text.length() == 0) {
            text = " ";
        }
        AttributedString r = new AttributedString(text);
        r.addAttribute(TextAttribute.FOREGROUND, defaultColor);
        r.addAttribute(TextAttribute.FONT, font);
        ArrayList<int[]> bold = new ArrayList<>();
        ArrayList<int[]> underline = new ArrayList<>();
        ArrayList<int[]> strike = new ArrayList<>();
        ArrayList<int[]> italic = new ArrayList<>();
        ArrayList<ColorAttribute> colors = new ArrayList<>();

        boolean prevSim = false;
        boolean openBracket = false;
        int boldStart = -1;
        int strikeStart = -1;
        int underlineStart = -1;
        int italicStart = -1;
        int colorStart = -1;
        String colorBuffer = "";
        Color color = defaultColor;
        int jump = 0;

        int i = 0;
        for (; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (openBracket) {
                if (c == ']') {
                    openBracket = false;
                    try {
                        color = new Color(Integer.decode(colorBuffer));
                    } catch (NumberFormatException e) {
                        try {
                            color = new Color(Integer.decode("0x"+colorBuffer));
                        } catch (NumberFormatException m) {
                            color = defaultColor;
                        }
                    }
                    colorStart = i - jump;
                    colorBuffer = "";
                } else if (String.valueOf(c).matches("[0-9xabcdef]")) {
                    colorBuffer += Character.toString(c);
                } else {
                    openBracket = false;
                    colorBuffer = "";
                }
                jump++;
            } else if (prevSim) {
                prevSim = false;
                if (String.valueOf(c).matches("[0-" + (defaultColors.length - 1) + "]")) {
                    if (colorStart != -1) {
                        colors.add(new ColorAttribute(colorStart, i - jump - 1, color));
                        colorStart = i - jump;
                        color = defaultColors[Integer.parseInt(Character.toString(c))];
                    } else {
                        colorStart = i - jump;
                        color = defaultColors[Integer.parseInt(Character.toString(c))];
                    }
                } else {
                    switch (c) {
                        case 'b':
                            if (boldStart != -1) {
                                bold.add(ia(boldStart, i - jump));
                                boldStart = -1;
                            } else {
                                boldStart = i - jump;
                            }
                            break;
                        case 'u':
                            if (underlineStart != -1) {
                                underline.add(ia(underlineStart, i - jump));
                                underlineStart = -1;
                            } else {
                                underlineStart = i - jump;
                            }
                            break;
                        case 's':
                            if (strikeStart != -1) {
                                strike.add(ia(strikeStart, i - jump));
                                strikeStart = -1;
                            } else {
                                strikeStart = i - jump;
                            }
                            break;
                        case 'i':
                            if (italicStart != -1) {
                                italic.add(ia(italicStart, i - jump));
                                italicStart = -1;
                            } else {
                                italicStart = i - jump;
                            }
                            break;
                        case '[':
                            openBracket = true;
                            if (colorStart != -1) {
                                colors.add(new ColorAttribute(colorStart, i - jump, color));
                                colorStart = i - jump;
                            } else {
                                colorStart = i - jump;
                            }
                            break;
                        case 'r':
                            if (boldStart != -1) {
                                bold.add(ia(boldStart, i - jump));
                                boldStart = -1;
                            }
                            if (underlineStart != -1) {
                                underline.add(ia(underlineStart, i - jump));
                                underlineStart = -1;
                            }
                            if (strikeStart != -1) {
                                strike.add(ia(strikeStart, i - jump));
                                strikeStart = -1;
                            }
                            if (italicStart != -1) {
                                italic.add(ia(italicStart, i - jump));
                                italicStart = -1;
                            }
                            if (colorStart != -1) {
                                colors.add(new ColorAttribute(colorStart, i - jump, color));
                                colorStart = -1;
                            } else {
                                colorStart = -1;
                            }
                            break;

                    }
                }
                jump++;
            } else if (c == '§') {
                prevSim = true;
                jump++;
            } else {
                continue;
            }
        }

        if (boldStart != -1) {
            bold.add(ia(boldStart, i - jump));
        }
        if (underlineStart != -1) {
            underline.add(ia(underlineStart, i - jump));
        }
        if (strikeStart != -1) {
            strike.add(ia(strikeStart, i - jump));
        }
        if (italicStart != -1) {
            italic.add(ia(italicStart, i - jump));
        }
        if (colorStart != -1) {
            colors.add(new ColorAttribute(colorStart, i - jump, color));
        }

        for (int j = 0; j < bold.size(); j++) {
            int[] is = bold.get(j);
            r.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, is[0], is[1]);
        }
        for (int j = 0; j < strike.size(); j++) {
            int[] is = strike.get(j);
            r.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, is[0], is[1]);
        }
        for (int j = 0; j < underline.size(); j++) {
            int[] is = underline.get(j);
            r.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, is[0], is[1]);
        }
        if (font != null) {
            for (int j = 0; j < italic.size(); j++) {
                int[] is = italic.get(j);
                r.addAttribute(TextAttribute.FONT, font.deriveFont(Font.ITALIC), is[0], is[1]);
            }
        }
        for (int j = 0; j < colors.size(); j++) {
            ColorAttribute colorAttribute = colors.get(j);
            try {
                r.addAttribute(TextAttribute.FOREGROUND, colorAttribute.color, colorAttribute.start, colorAttribute.end);
            } catch (Exception e) {
            }
        }

        return r;
    }
}
