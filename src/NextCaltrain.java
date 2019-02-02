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

  private final int openSansMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  private final int north_weekday[][] = {
      {101,301,363},{103,336,398},{305,368,407},{309,386,428},{207,398,444},
      {211,414,477},{313,432,471},{215,441,487},{319,446,491},{217,458,504},
      {221,474,538},{323,492,533},{225,501,547},{329,507,551},{227,521,569},
      {231,531,592},{233,554,609},{135,587,650},{237,623,677},{139,647,708},
      {143,706,768},{147,766,828},{151,826,888},{155,886,948},{257,896,950},
      {159,947,1010},{261,975,1022},{263,993,1056},{365,1004,1051},
      {267,1014,1062},{269,1040,1086},{371,1025,1071},{273,1049,1113},
      {375,1060,1107},{277,1074,1122},{381,1085,1131},{279,1100,1146},
      {283,1109,1173},{385,1120,1167},{287,1141,1189},{289,1151,1202},
      {191,1180,1242},{193,1217,1280},{195,1277,1340},{197,1337,1400},
      {199,1384,1445}};

  private final int south_weekday[][] = {
      {102,295,351},{104,325,384},{206,365,414},{208,375,434},{310,395,441},
      {212,405,453},{314,419,457},{216,425,472},{218,435,494},{320,455,501},
      {222,465,513},{324,479,517},{226,485,532},{228,495,554},{330,515,561},
      {232,525,573},{134,540,600},{236,585,635},{138,600,660},{142,660,720},
      {146,720,780},{150,780,840},{152,840,900},{254,883,932},{156,900,960},
      {258,934,986},{360,972,1006},{262,983,1024},{366,998,1035},
      {268,1018,1063},{370,1036,1076},{272,1047,1088},{376,1058,1095},
      {278,1078,1123},{380,1096,1135},{282,1103,1144},{386,1118,1155},
      {288,1138,1183},{190,1170,1232}, {192,1230,1292},{194,1290,1352},
      {196,1360,1422},{198,1445,1504}};

  private final int north_weekend[][] = {
      {423,552,641},{801,613,685},{425,642,731},{427,732,821},
      {429,822,911},{431,912,1001},{433,1002,1091},{803,1063,1135},
      {435,1092,1181},{437,1182,1271},{439,1272,1361},{441,1362,1451},
      {421,451,537},{443,1382,1471},{501,389,439},{503,419,469},
      {505,1034,1084},{507,1117,1167}};

  private final int south_weekend[][] = {
      {422,463,556},{424,553,646},{426,643,736},{802,696,764},
      {428,733,826},{430,823,916},{432,913,1006},{434,1003,1096},
      {436,1093,1186},{804,1146,1214},{438,1183,1276},{440,1273,1366},
      {442,1347,1440},{444,1420,1509},{502,367,413},{504,427,473},
      {506,1005,1051},{508,1065,1111}};

  private final String daysOfWeek[] = {
      "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

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
    private int hr24;
    private int hour;
    private int minute;
    private int second;
    private String ampm;
    private String strTime;
    private String strWeek;
    private int dotw;
    private boolean weekday;
    private int last_state = -1;
    private int last_minute = -1;
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
      alternate.addElement(new Integer(211));
      alternate.addElement(new Integer(221));
      alternate.addElement(new Integer(231));
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
      hr24 = calendar.get(Calendar.HOUR_OF_DAY);
      hour = calendar.get(Calendar.HOUR);
      minute = calendar.get(Calendar.MINUTE);
      second = calendar.get(Calendar.SECOND);
      ampm = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "am" : "pm";
      dotw = calendar.get(Calendar.DAY_OF_WEEK); // daysOfWeek[dotw];
      weekday = ((dotw > 1) && (dotw < 7));
      currentMinutes = hr24 * 60 + minute;
      if (hour < 1) hour += 12;
      if (state == -1) state = calendar.get(Calendar.AM_PM);
      strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute + " " + ampm;
      // (second < 10 ? ":0" : ":") + second
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      g.setColor(WHITE);
      g.setFont(largeFont);
      g.drawString(strTime, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      // Load some page defaults
      if (state == 0) {
        from = "Palo Alto to";
        from_alt = "Menlo Park to";
        dest = "San Francisco";
        data = (weekday) ? north_weekday : north_weekend;
      } else {
        from = "San Francisco";
        from_alt = "";
        dest = "to Palo Alto";
        data = (weekday) ? south_weekday : south_weekend;
      }
      int index = 0;
      while (stopOffset == -1) {
        if (currentMinutes > data[data.length - 1][1]) {
          stopOffset = 0;
        } else if (data[index][1] > currentMinutes) {
          stopOffset = index;
        }
        index++;
      }
      g.setFont(largeFont);
      strWeek = daysOfWeek[dotw];
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.setColor(WHITE);
      letterFont = openSansDemi;
      if ((from_alt.length() > 0) && (alternate.contains(new Integer(data[stopOffset][0])))) {
        letters(g, from_alt, (width / 2) - (lettersWidth(from_alt) / 2), 30);
      } else {
        letters(g, from, (width / 2) - (lettersWidth(from) / 2), 30);
      }
      letters(g, dest, (width / 2) - (lettersWidth(dest) / 2), 52);

      selectionMinutes = data[stopOffset][1];
      betweenMinutes = selectionMinutes - currentMinutes;
      if (betweenMinutes < 0) {
        g.setColor(DARK);
          blurb = "";
      } else if (betweenMinutes < 1) {
        g.setColor(YELLOW);
        letterFont = openSansDemi;
        blurb = (second % 2 == 0) ? "ARRIVING" : "";
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
      g.drawRoundRect(0, startPosition + 27, width - 1, optionLeading, 9, 9);
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
      for (int i = stopOffset; i < stopOffset + stopWindow; i++) {
        position += optionLeading;
        int n = (i >= data.length) ? i - data.length : i;
        betweenMinutes = data[n][1] - currentMinutes;
        int trip = data[n][0];
        int d_hr = data[n][1] / 60;
        int d_min = data[n][1] % 60;
        String depart_ampm = "am";
        if (d_hr > 11 && d_hr < 24) depart_ampm = "pm";
        if (d_hr > 12) d_hr -= 12;
        String depart = "" + d_hr + (d_min < 10 ? ":0" : ":") + d_min;
        int a_hr = data[n][2] / 60;
        int a_min = data[n][2] % 60;
        String arrive_ampm = "am";
        if (a_hr > 11 && a_hr < 24) arrive_ampm = "pm";
        if (a_hr > 24) a_hr -= 24;
        if (a_hr > 12) a_hr -= 12;
        String arrive = "" + a_hr + (a_min < 10 ? ":0" : ":") + a_min;
        numberFont = number21;

        g.setFont(largeFont);
        g.setColor((betweenMinutes < 0) ? CYAN : WHITE);

        boolean is_alt = (alternate.contains(new Integer(trip)));
        String pre = is_alt ? "\\:" : "#";
        g.drawString(pre + trip, padding, position - 2, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        numbers(g, depart, depart_align - numbersWidth(depart), position - 6);
        g.drawString(" " + depart_ampm, depart_align, position, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        numbers(g, arrive, arrive_align - numbersWidth(arrive), position - 6);
        g.drawString(" " + arrive_ampm, arrive_align, position, Graphics.LEFT | Graphics.TOP);

      }
      g.drawImage(hamburgerImage, 0, height - 2, Graphics.LEFT | Graphics.BOTTOM);
      g.drawImage(backarrowImage, width, height - 2, Graphics.RIGHT | Graphics.BOTTOM);
      g.setFont(largeFont);
      g.drawString(strWeek, width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);
      painting = false;
      last_state = state;
      last_minute = minute;
    }

  }

}
