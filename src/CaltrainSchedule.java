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
  static Image badge = null;
  static Image fontImage = null;
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
        fontImage = Image.createImage ("/font28x36.png");
      } catch (Exception ex) {
      }
    }

    public void customFont(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        char character = phrase.charAt(i);
        int ascii = (int) character;
        int cx = ascii % 280;
        int cy = ascii / 360;
        if ("WwM".indexOf(character) == -1) { cx -= 5; }
        if ("m".indexOf(character) == -1) { cx -= 10; }
        int cw = 30;
        int ch = 36;
        if (ascii < 40 || ascii > 177) {
          fx += 28;
        } else {
          g.setClip(fx, fy, cw, ch);
          g.drawImage(fontImage, fx - cx, fy, Graphics.LEFT | Graphics.TOP);
          fx += 28;
        }
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
      g.drawString("SMALL TEXT", 10, position, Graphics.LEFT | Graphics.TOP);
      position = position + font1.getHeight() + 10;
      //customFont(g, "9:21pm", 100, panel_offset + 100);
      g.setClip(100, height - 100, 300, 77);
      g.drawImage(fontImage, 100, 20, Graphics.LEFT | Graphics.TOP);
      painting = false;
    }
  }
}
