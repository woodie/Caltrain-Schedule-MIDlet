import java.io.*;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.lang.Integer;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/*
 * Caltrain Schedule: Palo Alto to San Francisco
 */
public class NextCaltrain extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private Command cmd_Exit = null;
  private FontCanvas fontCanvas = null;
  private CaltrainServie service = null;

  private final int padding = 4;
  private static Image number21 = null;
  private static Image number30 = null;
  private static Image numberFont = null;
  private static Image openSansBold = null;
  private static Image openSansDemi = null;
  private static Image openSansLight = null;
  private static Image letterFont = null;
  private static Image hamburgerImage = null;
  private static Image backarrowImage = null;

  private final String daysOfWeek[] = {
      "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

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
    service = new CaltrainServie();
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

    private String[] stations = CaltrainServieData.south_stops;
    private int departStation = 17;
    private int arriveStation = 1;
    private int state = -1;
    private String from = "";
    private String from_alt = "";
    private String dest = "";
    private Vector pressed = new Vector();
    private NextCaltrain parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FRAME_DELAY = 40;
    private int hr24;
    private int hour;
    private int minute;
    private int second;
    private String ampm;
    private String strTime;
    private String strWeek;
    private int dotw;
    private int last_state = -1;
    private int last_minute = -1;
    private final int LOGICAL = 0;
    private final int FLIPPED = 1;
    private int data[][];
    Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private String blurb = "";
    private int stopOffset = -1;
    private int stopWindow = 6;
    private int currentMinutes = -1;
    private int betweenMinutes = -1;
    private int selectionMinutes = -1;
    private final int CYAN = 0x00AAFF;
    private final int MAGENTA = 0xFF0080;
    private final int YELLOW = 0xFFFF00;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    private final int GREEN = 0x88CC33;
    private final int ORANGE = 0xFF8000;
    private final int GRAY= 0xCCCCCC;
    private final int DARK= 0x666666;

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
          // paint countdown message
          repaint(0, 70, width, 30);
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
        stopOffset = (stopOffset == 0) ? data.length - 1 : --stopOffset;
        repaint();
        break;
      case Canvas.DOWN:
        stopOffset = (stopOffset == data.length - 1) ? 0 : ++stopOffset;
        repaint();
        break;
      case Canvas.LEFT:
        stopOffset = -1;
        state = (state == LOGICAL) ? FLIPPED : LOGICAL;
        break;
      case Canvas.RIGHT:
        stopOffset = -1;
        state = (state == LOGICAL) ? FLIPPED : LOGICAL;
        break;
      case GAME_A:
        departStation = (departStation <= 1) ? stations.length - 1: --departStation;
        break;
      case GAME_B:
        departStation = (departStation == stations.length) ? 1 : ++departStation;
        break;
      case GAME_C:
        arriveStation = (arriveStation <= 1) ? stations.length - 1: --arriveStation;
        break;
      case GAME_D:
        arriveStation = (arriveStation == stations.length) ? 1 : ++arriveStation;
        break;
      }
      if (data.length == 0) stopOffset = 0;
      last_minute = -1; // force full paint
      this.repaint();
    }

    public void paint(Graphics g) {
      Calendar calendar = Calendar.getInstance();
      hr24 = calendar.get(Calendar.HOUR_OF_DAY);
      hour = calendar.get(Calendar.HOUR);
      minute = calendar.get(Calendar.MINUTE);
      second = calendar.get(Calendar.SECOND);
      ampm = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "am" : "pm";
      dotw = calendar.get(Calendar.DAY_OF_WEEK);
      currentMinutes = hr24 * 60 + minute;
      if (hour < 1) hour += 12;
      if (state == -1) state = calendar.get(Calendar.AM_PM);
      strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute + " " + ampm;
      // (second < 10 ? ":0" : ":") + second
      strWeek = daysOfWeek[dotw];
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      g.setColor(WHITE);
      g.setFont(largeFont);
      g.drawString(strTime, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      g.setColor(WHITE);
      g.drawImage(hamburgerImage, 0, height - 2, Graphics.LEFT | Graphics.BOTTOM);
      g.drawImage(backarrowImage, width, height - 2, Graphics.RIGHT | Graphics.BOTTOM);
      g.setFont(largeFont);
      g.drawString(strWeek, width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);

      // Load some page defaults
      from = (state == FLIPPED) ? stations[arriveStation] : stations[departStation];
      dest = (state != FLIPPED) ? stations[arriveStation] : stations[departStation];
      data = service.routes(from, dest, dotw);
      int index = 0;
      while (stopOffset == -1) {
        if (currentMinutes > data[data.length - 1][CaltrainServie.DEPART]) {
          stopOffset = 0;
        } else if (data[index][CaltrainServie.DEPART] >= currentMinutes) {
          stopOffset = index;
        }
        index++;
      }
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.setColor(WHITE);
      letterFont = openSansDemi;
      String from_;
      String dest_;
      if (from.length() > dest.length()) {
        from_ = from;
        dest_ = dest + " to";
      } else {
        from_ = from + " to";
        dest_ = dest;
      }
      letters(g, from_, (width / 2) - (lettersWidth(from_) / 2), 30);
      letters(g, dest_, (width / 2) - (lettersWidth(dest_) / 2), 52);

      selectionMinutes = (data.length < 1) ? 0 : data[stopOffset][CaltrainServie.DEPART];
      betweenMinutes = selectionMinutes - currentMinutes;
      if (data.length < 1) {
        g.setColor(CYAN);
        blurb = (second % 2 == 0) ? "NO TRAINS" : "";
      } else if (betweenMinutes < 0) {
        g.setColor(DARK);
        blurb = "";
      } else if (betweenMinutes < 1) {
        g.setColor(YELLOW);
        letterFont = openSansDemi;
        blurb = (second % 2 == 0) ? "DEPARTING" : "";
      } else {
        g.setColor(GREEN);
        letterFont = openSansDemi;
        if (betweenMinutes > 59) {
          blurb = "in " + (betweenMinutes / 60) + " hr " + (betweenMinutes % 60) + " min";
        } else {
          blurb = "in " + betweenMinutes + " min " + (60 - second) + " sec";
        }
      }

      int optionLeading = 29;
      int startPosition = 80;
      letters(g, blurb, (width / 2) - (lettersWidth(blurb) / 2), startPosition + 3);
      if (data.length > 0) {
        g.drawRoundRect(0, startPosition + 27, width - 1, optionLeading, 9, 9);
      }
      //if (state == last_state && minute == last_minute) {
      //  return; // nothing else to repaint
      //}
      int position = 88;
      int gutter = 8;
      int trip_width = largeFont.stringWidth("#321");
      int ampm_width = smallFont.stringWidth(" pm");
      int time_width = numbersWidth("12:22");
      int arrive_align = width - padding - ampm_width;
      int depart_align = arrive_align - gutter - time_width - ampm_width;
      int maxWindow = (data.length < stopWindow) ? data.length : stopOffset + stopWindow;
      int minWindow = (data.length < stopWindow) ? 0 : stopOffset;
      for (int i = minWindow; i < maxWindow; i++) {
        position += optionLeading;
        int n = (i >= data.length) ? i - data.length : i;
        betweenMinutes = data[n][CaltrainServie.DEPART] - currentMinutes;
        int trip = data[n][CaltrainServie.TRAIN];
        int d_hr = data[n][CaltrainServie.DEPART] / 60;
        int d_mn = data[n][CaltrainServie.DEPART] % 60;
        String depart_ampm = "am";
        if (d_hr > 11 && d_hr < 24) depart_ampm = "pm";
        if (d_hr > 12) d_hr -= 12;
        String depart = "" + d_hr + (d_mn < 10 ? ":0" : ":") + d_mn;
        int a_hr = data[n][CaltrainServie.ARRIVE] / 60;
        int a_mn = data[n][CaltrainServie.ARRIVE] % 60;
        String arrive_ampm = "am";
        if (a_hr > 11 && a_hr < 24) arrive_ampm = "pm";
        if (a_hr > 24) a_hr -= 24;
        if (a_hr > 12) a_hr -= 12;
        String arrive = "" + a_hr + (a_mn < 10 ? ":0" : ":") + a_mn;

        numberFont = number21;
        g.setFont(largeFont);
        g.setColor((betweenMinutes < 0) ? CYAN : WHITE);

        String pre = false ? "\\:" : "#";
        g.drawString(pre + trip, padding, position - 2, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        numbers(g, depart, depart_align - numbersWidth(depart), position - 6);
        g.drawString(" " + depart_ampm, depart_align, position, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        numbers(g, arrive, arrive_align - numbersWidth(arrive), position - 6);
        g.drawString(" " + arrive_ampm, arrive_align, position, Graphics.LEFT | Graphics.TOP);
      }
      last_state = state;
      last_minute = minute;
    }

  }

}
