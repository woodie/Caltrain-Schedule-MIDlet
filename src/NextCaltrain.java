import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

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
  private static Image openSansDemi = null;
  private static Image openSansLight = null;
  private static Image letterFont = null;
  private static Image hamburgerImage = null;
  private static Image backarrowImage = null;
  private String data[];

  private final int openSansMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  private final String northbound[] = {
      "319 7:26 8:11", "217 7:38 8:24", "323 8:12 8:53", "225 8:21 9:07",
      "329 8:27 9:11", "227 8:41 9:29", "231 8:51 9:52", "233 9:14 10:09"};

  private final String southbound[] = {
      "268 4:58 5:43", "370 5:16 5:56", "272 5:27 6:08", "376 5:38 6:15",
      "278 5:58 6:43", "380 6:16 6:55", "282 6:23 7:04", "386 6:38 7:71"};

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

    private int state = -1;
    private String from = "";
    private String dest = "";
    private Vector vect = new Vector();
    private NextCaltrain parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FRAME_DELAY = 40;
    private int hour;
    private int minute;
    private int second;

    public FontCanvas(NextCaltrain parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();

      try {
        number21 = Image.createImage ("/numbers14x21.png");
        number30 = Image.createImage ("/numbers22x30.png");
        openSansBold = Image.createImage ("/sans-bold-20.png");
        openSansDemi = Image.createImage ("/sans-demi-20.png");
        openSansLight = Image.createImage ("/sans-light-20.png");
        hamburgerImage = Image.createImage ("/hamburger.png");
        backarrowImage = Image.createImage ("/backarrow.png");
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
          repaint(width / 2 - 50, height - 50, 100, 50);
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
      Calendar calendar = Calendar.getInstance();
      hour = calendar.get(Calendar.HOUR);
      minute = calendar.get(Calendar.MINUTE);
      second = calendar.get(Calendar.SECOND);
      if (hour < 1) hour += 12;
      if (state == -1) state = calendar.get(Calendar.AM_PM);
      String strTime = "" + hour + 
          (minute < 10 ? ":0" : ":") + minute +
          (second < 10 ? ":0" : ":") + second;

      // Load some page defaults
      if (state == 0) {
        from = "Palo Alto to";
        dest = "San Francisco";
        data = northbound;
      } else {
        from = "San Francisco";
        dest = "to Palo Alto";
        data = southbound;
      }
      g.fillRect(0, 0, width, height);
      g.setColor(0xFFFFFF);
      letterFont = openSansDemi;
      letters(g, "Next Caltrain", padding, padding);
      g.setColor(0xFFF200);
      letters(g, from, padding, 30);
      letters(g, dest, padding, 52);

      int position = 78;
      for (int i = 0; i < data.length; i++) {
        int x0 = data[i].indexOf(" ");
        int x1 = data[i].lastIndexOf(32);
        int x2 = data[i].length();
        String trip = data[i].substring(0, x0);
        String depart = data[i].substring(1 + x0, x1);
        String arrive = data[i].substring(1 + x1, x2);
        g.setColor(0x00DDFF);
        letterFont = openSansLight;
        letters(g, "#" + trip, padding, position);
        g.setColor((trip == "231") ? 0xFFAA00 : 0xFFFFFF);
        numberFont = number21;
        int offset1 = (depart.length() > 4) ? 31 : 24;
        numbers(g, depart, (width / 2) - offset1, position - 2);
        g.setColor(0xFFFFFF);
        int offset2 = (arrive.length() > 4) ? 64 : 50;
        numbers(g, arrive, width - (offset2) - padding, position - 2);
        position += 26;
      }
      g.setColor(0xFFFFFF);
      numberFont = number21;
      int timeWidth = (strTime.length() - 1) * 14;
      numbers(g, strTime, (width / 2) - (timeWidth / 2), height - 30);
      g.drawImage(hamburgerImage, 0, height, Graphics.LEFT | Graphics.BOTTOM);
      g.drawImage(backarrowImage, width, height, Graphics.RIGHT | Graphics.BOTTOM);
      painting = false;
    }

  }

}
