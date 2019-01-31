import java.io.*;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.lang.Integer;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/*
 * proto app
 */
public class NextCaltrain extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private Command cmd_Exit = null;
  private FontCanvas fontCanvas = null;
  private final int padding = 2;
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

  private final int openSansMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  private final String northbound[] = { "101 5:01_am 6:03_am",
      "103 5:36_am 6:38_am",  "305 6:08_am 6:47_am",  "207 6:38_am 7:24_am",
      "309 6:26_am 7:08_am",  "211 6:54_am 7:57_am", // Menlo Park
      "313 7:12_am 7:51_am",  "215 7:21_am 8:07_am", "319 7:26_am 8:11_am",
      "217 7:38_am 8:24_am",  "221 7:54_am 8:58_am", // Menlo Park
      "323 8:12_am 8:53_am",  "225 8:21_am 9:07_am",  "329 8:27_am 9:11_am",
      "227 8:41_am 9:29_am",  "231 8:51_am 9:52_am", // Menlo Park
      "233 9:14_am 10:09_am", "135 9:47_am 10:50_am","237 10:23_am 11:17_am",
     "139 10:47_am 11:48_am","143 11:46_am 12:48_pm","147 12:46_pm 1:48_pm",
      "151 1:46_pm 2:48_pm",  "155 2:46_pm 3:48_pm",  "257 2:56_pm 3:50_pm",
      "159 3:47_pm 4:50_pm",  "261 4:15_pm 5:02_pm",  "263 4:33_pm 5:36_pm",
      "365 4:44_pm 5:31_pm",  "267 4:54_pm 5:42_pm",  "269 5:20_pm 6:06_pm",
      "371 5:05_pm 5:51_pm",  "273 5:29_pm 6:33_pm",  "375 5:40_pm 6:27_pm",
      "277 5:54_pm 6:42_pm",  "279 6:20_pm 7:06_pm",  "381 6:05_pm 6:51_pm",
      "283 6:29_pm 7:33_pm",  "385 6:40_pm 7:27_pm",  "287 7:01_pm 7:49_pm",
      "289 7:11_pm 8:02_pm",  "191 7:40_pm 8:42_pm",  "193 8:17_pm 9:20_pm",
      "195 9:17_pm 10:20_pm","197 10:17_pm 11:20_pm","199 11:04_pm 12:05_am"};

  private final String southbound[] = { "102 4:55_am 5:51_am",
      "104 5:25_am 6:24_am",  "206 6:05_am 6:54_am",  "208 6:15_am 7:14_am",
      "310 6:35_am 7:21_am",  "212 6:45_am 7:33_am",  "314 6:59_am 7:37_am",
      "216 7:05_am 7:52_am",  "218 7:15_am 8:14_am",  "320 7:35_am 8:21_am",
      "222 7:45_am 8:33_am",  "324 7:59_am 8:37_am",  "226 8:05_am 8:52_am",
      "228 8:15_am 9:14_am",  "330 8:35_am 9:21_am",  "232 8:45_am 9:33_am",
      "134 9:00_am 10:00_am", "236 9:45_am 10:35_am","138 10:00_am 11:00_am",
     "142 11:00_am 12:00_pm","146 12:00_pm 1:00_pm",  "150 1:00_pm 2:00_pm",
      "152 2:00_pm 3:00_pm",  "254 2:43_pm 3:32_pm",  "156 3:00_pm 4:00_pm",
      "258 3:34_pm 4:26_pm",  "360 4:12_pm 4:46_pm",  "262 4:23_pm 5:04_pm",
      "366 4:38_pm 5:15_pm",  "268 4:58_pm 5:43_pm",  "370 5:16_pm 5:56_pm",
      "272 5:27_pm 6:08_pm",  "376 5:38_pm 6:15_pm",  "278 5:58_pm 6:43_pm",
      "380 6:16_pm 6:55_pm",  "282 6:23_pm 7:04_pm",  "386 6:38_pm 7:15_pm",
      "288 6:58_pm 7:43_pm",  "190 7:30_pm 8:32_pm",  "192 8:30_pm 9:32_pm",
      "194 9:30_pm 10:32_pm","196 10:40_pm 11:42_pm", "198 12:05_am 1:04_am"};

 private final String daysOfWeek[] = {
     "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

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

  public void commandAction(Command c, Displayable d) {
    if (c == cmd_Exit) {
      try {
        destroyApp(true);
        notifyDestroyed();
      } catch (MIDletStateChangeException e) {
        e.printStackTrace();
      }
    }
  }

  public void itemStateChanged(Item item) {}

  class FontCanvas extends Canvas {

    private int state = -1;
    private String from = "";
    private String from_alt = "";
    private String dest = "";
    private Vector pressed = new Vector();
    private Vector alternate = new Vector();
    private NextCaltrain parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FRAME_DELAY = 40;
    private int hour;
    private int minute;
    //private int second;
    private String ampm;
    private String strTime;
    //private String dotw;
    private int last_state = -1;
    private int last_minute = -1;
    private String data[];
    Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private String debug = "";
    private int northboundOffset = 8;
    private int southboundOffset = 30;
    private int stopOffset = -1;
    private int stopWindow = 7;

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
      last_minute = -1; // force full paint after sleep
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
          repaint(width - largeFont.stringWidth(strTime) - padding,
              padding, largeFont.stringWidth(strTime), largeFont.getHeight());
        }
      };
      // when showing only minutes, inverval should be next minute change
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
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

    public int lettersWidth(String phrase) {
      int length = 0;
      for (int i = 0; i < phrase.length(); i++) {
        int ascii = ((int) phrase.charAt(i));
        int cw = (ascii >= 32 && ascii <= 126) ? openSansMetrics[ascii - 32] : 0;
        length += cw;
      }
      return length;
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
      pressed.addElement(getKeyName(keyCode));

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        stopOffset = -1;
        repaint();
        break;
      case Canvas.UP:
        stopOffset = (stopOffset == 0) ? data.length - stopWindow : --stopOffset;
        repaint();
        break;
      case Canvas.DOWN:
        stopOffset = (stopOffset == data.length - stopWindow) ? 0 : ++stopOffset;
        repaint();
        break;
      case Canvas.LEFT:
        stopOffset = -1;
        state = (state == 0) ? 1 : 0;
        break;
      case Canvas.RIGHT:
        stopOffset = -1;
        state = (state == 0) ? 1 : 0;
        break;
      }
      last_minute = -1; // force full paint
      this.repaint();
    }

    public void paint(Graphics g) {
      Calendar calendar = Calendar.getInstance();
      hour = calendar.get(Calendar.HOUR);
      minute = calendar.get(Calendar.MINUTE);
      //second = calendar.get(Calendar.SECOND);
      ampm = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "am" : "pm";
      //dotw = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];

      if (hour < 1) hour += 12;
      if (state == -1) state = calendar.get(Calendar.AM_PM);
      strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute + " " + ampm;
      //    (second < 10 ? ":0" : ":") + second
      g.setColor(0x000000);
      g.fillRect(0, 0, width, height);
      g.setColor(0xFFFFFF);
      g.setFont(largeFont);
      g.drawString(strTime, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      if (state == last_state && minute == last_minute) {
        return; // nothing else to repaint
      }
      // Load some page defaults
      if (state == 0) {
        from = "Palo Alto";
        from_alt = "Menlo";
        dest = "to San Francisco";
        data = northbound;
        if (stopOffset == -1) stopOffset = northboundOffset;
      } else {
        from = "San Francisco";
        from_alt = "";
        dest = "to Palo Alto";
        data = southbound;
        if (stopOffset == -1) stopOffset = southboundOffset;
      }
      //debug = "" + stopOffset;
      g.setColor(0xFFFFFF);
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      //g.drawString(dotw, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      letterFont = openSansDemi;
      if (from_alt.length() > 0) {
        String both = from + " / " + from_alt;
        g.setColor(0xFFFFFF);
        int leftmost = (width / 2) - (lettersWidth(both) / 2);
        letters(g, from, leftmost, 30);
        g.setColor(0xFFAA00);
        letters(g, " / " + from_alt, leftmost + lettersWidth(from), 30);
      } else {
        g.setColor(0xFFFFFF);
        letters(g, from, (width / 2) - (lettersWidth(from) / 2), 30);
      }
      g.setColor(0xFFF300);
      letters(g, dest, (width / 2) - (lettersWidth(dest) / 2), 52);

      // 211 221 231
      alternate.addElement(new Integer(211));
      alternate.addElement(new Integer(221));
      alternate.addElement(new Integer(231));
      int position = 85;
      int gutter = 13;
      int trip_width = largeFont.stringWidth("#321");
      int ampm_width = smallFont.stringWidth(" pm");
      int time_width = numbersWidth("12:22");
      int arrive_align = width - padding - ampm_width;
      int depart_align = arrive_align - gutter - time_width - ampm_width;
      for (int i = stopOffset; i < stopOffset + stopWindow; i++) {
        int x0 = data[i].indexOf(" ");
        int x1 = data[i].lastIndexOf(' ');
        int x2 = data[i].length();
        String trip = data[i].substring(0, x0);
        String depart_tmp = data[i].substring(1 + x0, x1);
        String depart = depart_tmp.substring(0, depart_tmp.indexOf("_"));
        String depart_ampm = depart_tmp.substring(1 + depart_tmp.indexOf("_"));
        String arrive_tmp = data[i].substring(1 + x1, x2);
        String arrive = arrive_tmp.substring(0, arrive_tmp.indexOf("_"));
        String arrive_ampm = arrive_tmp.substring(1 + arrive_tmp.indexOf("_"));
        numberFont = number21;

        g.setFont(smallFont);
        boolean is_alt = (alternate.contains(Integer.valueOf(trip)));
        g.setColor(is_alt ? 0xFFAA00 : 0x00DDFF);
        String pre = is_alt ? "\\:" : "#";
        g.drawString(pre + trip, padding, position, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        g.setColor(is_alt ? 0xFFAA00 : 0xFFFFFF);
        numbers(g, depart, depart_align - numbersWidth(depart), position - 6);
        g.drawString(" " + depart_ampm, depart_align, position, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        g.setColor(0xFFF300);
        numbers(g, arrive, arrive_align - numbersWidth(arrive), position - 6);
        g.drawString(" " + arrive_ampm, arrive_align, position, Graphics.LEFT | Graphics.TOP);

        position += 30;
      }
      g.drawImage(hamburgerImage, 0, height, Graphics.LEFT | Graphics.BOTTOM);
      g.drawImage(backarrowImage, width, height, Graphics.RIGHT | Graphics.BOTTOM);
      g.setFont(largeFont);
      g.drawString(debug, width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);
      painting = false;
      last_state = state;
      last_minute = minute;
    }

  }

}
