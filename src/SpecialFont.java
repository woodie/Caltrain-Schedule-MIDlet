import java.io.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A simple utility to provide larger fonts to MIDP.
 */
public class SpecialFont {

  private static Image lettersImage = null;
  private static Image numbersImage = null;
  private static String lettersFile = "/sans-demi-20.png";
  private static String numbersFile = "/numbers14x21.png";
  public static final int lettersBaseline = 16;
  public static final int numbersBaseline = 19;

  private final int fontMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  public SpecialFont() {
    try {
      numbersImage = Image.createImage (numbersFile);
      lettersImage = Image.createImage (lettersFile);
    } catch (Exception ex) {
    }
  }

  public int lettersWidth(String phrase) {
    int length = 0;
    for (int i = 0; i < phrase.length(); i++) {
      int ascii = ((int) phrase.charAt(i));
      int cw = (ascii >= 32 && ascii <= 126) ? fontMetrics[ascii - 32] : 0;
      length += cw;
    }
    return length;
  }

  public void letters(Graphics g, String phrase, int fx, int fy) {
    int width = g.getClipWidth();
    int height = g.getClipHeight();
    for (int i = 0; i < phrase.length(); i++) {
      int cw = 20;
      int ch = 22;
      int ascii = ((int) phrase.charAt(i));
      if (ascii >= 32 && ascii <= 126) {
        int cx = ((ascii - 32) / 8) * cw;
        int cy = ((ascii - 32) % 8) * ch;
        cw = fontMetrics[ascii - 32];
        g.setClip(fx, fy, cw, ch);
        g.fillRect(fx, fy, cw, ch);
        g.drawImage(lettersImage, fx - cx, fy - cy, Graphics.LEFT | Graphics.TOP);
        fx += cw;
        g.setClip(0 ,0, width, height);
      }
    }
  }

  public int numbersWidth(String phrase) {
    int length = 0;
    for (int i = 0; i < phrase.length(); i++) {
      int intValue = ((int) phrase.charAt(i)) - 48;
      if (intValue >= 0 && intValue <= 9) {
        length += 14;
      } else if (intValue == 10) {
        length += 7;
      }
    }
    return length;
  }

  public void numbers(Graphics g, String phrase, int fx, int fy) {
    int width = g.getClipWidth();
    int height = g.getClipHeight();
    for (int i = 0; i < phrase.length(); i++) {
      int cw = 14;
      int ch = 21;
      int intValue = ((int) phrase.charAt(i)) - 48;
      if (intValue >= 0 && intValue <= 10) {
        int cx = intValue * cw;
        if (intValue == 10) { cw = cw / 2; }
        g.setClip(fx, fy, cw, ch);
        g.fillRect(fx, fy, cw, ch);
        g.drawImage(numbersImage, fx - cx, fy, Graphics.LEFT | Graphics.TOP);
        fx += cw;
        g.setClip(0 ,0, width, height);
      }
    }
  }

}
