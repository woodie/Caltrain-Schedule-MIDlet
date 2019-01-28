import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*
 * proto app
 */
public class NextCaltrain extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private FontCanvas fontCanvas = null;
  private final int padding = 4;
  private boolean painting = false;
  private static Image number21 = null;
  private static Image number30 = null;
  private static Image numberFont = null;
  private static Image openSansBold = null;
  private static Image openSansLight = null;
  private static Image letterFont = null;
  private static Image northImage = null;
  private static Image southImage = null;
  private static Image backgroundImage = null;
  public Calendar calendar;

  private final int openSansMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  public NextCaltrain() {
    display = Display.getDisplay(this);
    fontCanvas = new FontCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(fontCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  public void commandAction(Command c, Displayable d) {}

  public void itemStateChanged(Item item) {}

  class FontCanvas extends Canvas {

    private int state = 0;
    private String from = "";
    private String dest = "";
    private Vector vect = new Vector();
    private NextCaltrain parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FRAME_DELAY = 40;

    public FontCanvas(NextCaltrain parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      try {
        number21 = Image.createImage ("/numbers14x21.png");
        number30 = Image.createImage ("/numbers22x30.png");
        openSansBold = Image.createImage ("/sans-bold-20.png");
        openSansLight = Image.createImage ("/sans-light-20.png");
        northImage = Image.createImage ("/north.png");
        southImage = Image.createImage ("/south.png");
      } catch (Exception ex) {
      }
    }

    protected void showNotify() {
      startFrameTimer();
    }

    protected void hideNotify() {
      stopFrameTimer();
    }

    protected void startFrameTimer() {
      timer = new Timer();
      updateTask = new TimerTask() {
        public void run() {
          // paint the clock
          repaint(width - 100, 0, 100, 50);
        }
      };
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    public void numbers(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        int cw = 14;
        int ch = 21;
        int intValue = ((int) phrase.charAt(i)) - 48;
        if (intValue >= 0 && intValue <= 10) {
          int cx = intValue * cw;
          if (intValue == 10) { cw = cw / 2; }
          g.setClip(fx, fy, cw, ch);
          g.fillRect(fx, fy, cw, ch);
          g.drawImage(numberFont, fx - cx, fy, Graphics.LEFT | Graphics.TOP);
          fx += cw;
          g.setClip(0 ,0, width, height);
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
          cw = openSansMetrics[ascii - 32];
          g.setClip(fx, fy, cw, ch);
          g.fillRect(fx, fy, cw, ch);
          g.drawImage(letterFont, fx - cx, fy - cy, Graphics.LEFT | Graphics.TOP);
          fx += cw;
          g.setClip(0 ,0, width, height);
        }
      }
    }

    public void keyPressed(int keyCode){
      vect.addElement(getKeyName(keyCode));
      state = (state == 0) ? 1 : 0;
      this.repaint();
    }

    public void paint(Graphics g) {
      calendar = Calendar.getInstance();
      int hour = calendar.get(Calendar.HOUR); if (hour < 1) { hour += 12; }
      int minute = calendar.get(Calendar.MINUTE);
      //int second = calendar.get(Calendar.SECOND);
      String strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute;

      // Load some page defaults
      if (state == 0) {
        backgroundImage = northImage;
        from = "San Francisco";
        dest = "to Palo Alto";
      } else {
        backgroundImage = southImage;
        from = "Palo Alto to";
        dest = "San Francisco";
      }
      g.drawImage(backgroundImage, width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER);

      g.setColor(0xFFFFFF);
      letterFont = openSansBold;
      letters(g, "Next Caltrain", 4, 4);
      //letters(g, strTime, width - (strTime.length() * 12) + 2, 4);
      numberFont = number21;
      int offset = (hour > 9) ? 38 : 52;
      numbers(g, strTime, width - offset, 4);
      g.setColor(0xFFF200);
      letters(g, from, 4, 30);
      letters(g, dest, 4, 52);
      painting = false;
    }

  }

}
