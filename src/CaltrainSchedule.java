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
  private static Image badge = null;
  private static Image fontImage = null;
  private static int fontMultiplier = 4;
  private static String fontFile = "font28x36.png"; // most characters are 6 units wide
  private static String chrIndex = "m MWw ?KNOQTVXY <>JLScrs (),/1;=fjt{} !.:I[]`il '|";
  private static String chrWidth = "9 888 777777777 55555555 444444444444 333333333 22";
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
    private int width = getWidth();
    private int height = getHeight();
    private int panel_height = width / 10 * 7;
    private int panel_offset = height - panel_height - 20;

    public FontCanvas(CaltrainSchedule parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      try {
        badge = Image.createImage ("/badge.png");
        fontImage = Image.createImage (fontFile);
      } catch (Exception ex) {
      }
    }

    public void customFont(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        int cw = 6;
        int ch = 9;
        char character = phrase.charAt(i);
        int ascii = (int) character;
        if (ascii > 32 || ascii < 127) {
          int cx = ((ascii - 32) % 8) * 7 * fontMultiplier;
          int cy = ((ascii - 32) / 8) * 9 * fontMultiplier;
          if (ascii == 34 || ascii == 92) {
            cw = 3; // straight-double-quote & backslash
          } else {
            int chrWidthIndex = chrIndex.indexOf(character);
            System.out.println("chrWidthIndex: " + chrWidthIndex);
            if (chrWidthIndex != -1) {
              cw = 4; // chrWidth & wide offset ()
            }
          }
          cw *= fontMultiplier;
          ch *= fontMultiplier;
          g.setClip(fx, fy, cw, ch);
          g.drawImage(fontImage, fx - cx, fy - cy, Graphics.LEFT | Graphics.TOP);
          g.setColor(0,0,0);
          g.drawRect(fx, fy, cw, ch);
        }
        fx += cw;
      }
    }

    public void paint(Graphics g) {
      g.setColor(0, 0, 0);
      g.fillRect(0, 0, width, height + 25);
      g.setColor(130, 200, 170); // Green Screen
      g.fillRect(0, panel_offset, width, panel_height);

      Font font1 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
      Font font2 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
      Font font3 = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,Font.SIZE_SMALL);

      calendar = Calendar.getInstance(TimeZone.getTimeZone("US/Pacific"));
      currentDate = calendar.getTime();

      // when System.getProperty("phone.imei")
      g.drawImage (badge, width / 2, height / 3 - 30, Graphics.VCENTER | Graphics.HCENTER);

      g.setColor(0, 0, 0);
      int position = panel_offset + 20;
      g.setFont(font1);
      g.drawString("BIG TEXT", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 10;
      g.setFont(font2);
      g.drawString(currentDate + "", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 10;
      g.setFont(font3);
      //g.drawString("SMALL TEXT", 10, position, Graphics.LEFT | Graphics.TOP);
      //position = position + font1.getHeight() + 10;
      //g.drawString("1: " + System.getProperty("phone.imei"), 10, position, Graphics.LEFT | Graphics.TOP);
      //position = position + font1.getHeight() + 10;
      //g.drawString("2 " + System.getProperty("com.nokia.imei"), 10, position, Graphics.LEFT | Graphics.TOP);
      //position = position + font1.getHeight() + 10;
      //g.drawString("3: " + System.getProperty("com.nokia.mid.imei"), 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 10;
      customFont(g, "hello World!", 20, panel_offset + 100);
      //g.setClip(100, height - 100, 300, 77);
      //g.drawImage(fontImage, 100, 20, Graphics.LEFT | Graphics.TOP);
      painting = false;
    }
  }
}
