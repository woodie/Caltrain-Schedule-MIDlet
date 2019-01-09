import java.io.*;
import java.lang.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CaltrainSchedule extends MIDlet {
  public static final boolean COLOR = false;
  public static final boolean DEBUG = false;
  private Display display = null;
  private FontCanvas fontCanvas = null;
  private boolean painting = false;
  private boolean northbound = true;
  private static Image numbersImage = null;
  private static Image openSansLight = null;
  private static Image openSansDemi = null;
  private static Image openSansBold = null;
  private static Image letterFont = null;
  public Calendar calendar;
  public Date currentDate;
  public TimeZone timezone;

  private final int semiFontWidth[] = {
      8,   6,  9, 14, 13, 18, 16,  5, 7,   7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };


  public CaltrainSchedule() {
    display = Display.getDisplay(this);
    fontCanvas = new FontCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(fontCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  class FontCanvas extends Canvas {
    private CaltrainSchedule parent = null;
    private int width;
    private int height;

    public FontCanvas(CaltrainSchedule parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      try {
        numbersImage = Image.createImage ("/numbers19x33.png");
        openSansLight = Image.createImage ("/sans-light-20.png");
        openSansDemi = Image.createImage ("/sans-demi-20.png");
        openSansBold = Image.createImage ("/sans-bold-20.png");
      } catch (Exception ex) {
      }
    }

    public void numbers(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        int cw = 19; // or 10
        int ch = 33;
        int intValue = ((int) phrase.charAt(i)) - 48;
        if (intValue >= 0 && intValue <= 10) {
          int cx = intValue * cw;
          if (intValue == 10) { cw = 10; }
          g.setClip(fx, fy, cw, ch);
          g.drawImage(numbersImage, fx - cx, fy, Graphics.LEFT | Graphics.TOP);
          fx += cw;
        }
      }
    }

    public void letters(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        int cw = 20;
        int ch = 22;
        int ascii = ((int) phrase.charAt(i));
        if (ascii >= 32 && ascii <= 126) {
          int cx = ((ascii - 32) / 8) * cw;
          int cy = ((ascii - 32) % 8) * ch;
          cw = semiFontWidth[ascii - 32];
          g.setClip(fx, fy, cw, ch);
          g.fillRect(fx, fy, cw, ch);
          g.drawImage(letterFont, fx - cx, fy - cy, Graphics.LEFT | Graphics.TOP);
          fx += cw;
        }
      }
    }

    public void paint(Graphics g) {
      g.setColor(0, 0, 0);
      g.fillRect(0, 0, width, height);

      timezone = TimeZone.getTimeZone("US/Pacific");
      calendar = Calendar.getInstance(timezone);
      currentDate = calendar.getTime();
      //TimeZone gmt = TimeZone.getTimeZone("GMT");
      //long offset = gmt.getRawOffset() - timezone.getRawOffset();
      int offset = -8; // or 7 during DST
      // https://supportweb.cs.bham.ac.uk/docs/java/j2me/api/java/util/Calendar.html
      // https://supportweb.cs.bham.ac.uk/docs/java/j2me/api/java/util/TimeZone.html
      int hour_gmt = calendar.get(Calendar.HOUR); // or HOUR_OF_DAY
      int hour = hour_gmt + offset;
      if (hour < 1) { hour += 12; }
      int minute = calendar.get(Calendar.MINUTE);
      String amPm = calendar.get(Calendar.AM_PM) == 1 ? "am" : "pm";
      String time = "" + hour + (minute < 10 ? ":0" : ":") + minute;

      Font font1 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
      g.setColor(255, 255, 255);
      int position = 5;
      g.setFont(font1);
      g.drawString(currentDate + "", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 2;
      g.drawString("Palo Alto / Menlo", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 2;
      g.drawString("to San Francisco", 10, position, Graphics.LEFT | Graphics.TOP);
      numbers(g, time, width - 3 - (time.length() * 19 - 9), 30);

      letterFont = openSansLight;
      g.setColor(255, 127, 255);
      letters(g, "Some light text", 10, 110);

      letterFont = openSansDemi;
      g.setColor(255, 255, 127);
      letters(g, "Some demi text", 10, 140);

      letterFont = openSansBold;
      g.setColor(127, 255, 255);
      letters(g, "Some bold text", 10, 170);

      letterFont = openSansLight;
      g.setColor(255, 255, 255);
      letters(g, "Back to white text", 10, 200);

      letterFont = openSansLight;
      g.setColor(255, 255, 255);
      letters(g, "ALL CAPS TEXT", 10, 230);

      letterFont = openSansLight;
      g.setColor(127, 127, 127);
      letters(g, "lowercase in gray", 10, 260);

      painting = false;
    }
  }

}
