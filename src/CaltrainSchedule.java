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
  private static Image northImage = null;
  private static Image southImage = null;
  public Calendar calendar;
  public Date currentDate;

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
        northImage = Image.createImage ("/nb.png");
        southImage = Image.createImage ("/sb.png");
      } catch (Exception ex) {
      }
    }

    public void customFont(Graphics g, String phrase, int fx, int fy) {
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

    public void paint(Graphics g) {
      g.setColor(0, 0, 0);
      g.fillRect(0, 0, width, height);

      calendar = Calendar.getInstance(TimeZone.getTimeZone("US/Pacific"));
      currentDate = calendar.getTime();

      // https://docs.oracle.com/javase/7/docs/api/java/util/Calendar.html
      int hour = calendar.get(Calendar.HOUR_OF_DAY); // or HOUR
      int minute = calendar.get(Calendar.MINUTE);
      String amPm = calendar.get(Calendar.AM_PM)==1 ? "am" : "pm";
      String time = "" + hour + ":" + minute;

      Font font1 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);

      g.drawImage(northImage, 0, 0, Graphics.TOP | Graphics.LEFT);
      g.setColor(255, 255, 255);
      int position = 5;
      g.setFont(font1);
      g.drawString(currentDate + "", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 2;
      g.drawString("Palo Alto / Menlo", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 2;
      g.drawString("to San Francisco", 10, position, Graphics.LEFT | Graphics.TOP);
      customFont(g, time, width - 3 - (time.length() * 19 - 9), 30);
      painting = false;
    }
  }

}
