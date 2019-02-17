import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/*
 * Caltrain Schedule
 */
public class NextCaltrain extends MIDlet
    implements ItemStateListener {

  private Vector pressed = new Vector();
  private Display display = null;
  private UserCanvas userCanvas = null;
  private HelpCanvas helpCanvas = null;
  private MainCanvas mainCanvas = null;
  private TripCanvas tripCanvas = null;
  private int selectedTrain = -1;
  private int stopOffset = -1;
  private int currentMinutes = -1;
  protected CaltrainService service = new CaltrainService();
  private int last_minute = -1;

  public NextCaltrain() {
    display = Display.getDisplay(this);
    userCanvas = new UserCanvas(this);
    helpCanvas = new HelpCanvas(this);
    mainCanvas = new MainCanvas(this);
    tripCanvas = new TripCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(mainCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  public void itemStateChanged(Item item) {}


/*
 * User Canvas
 */
  class UserCanvas extends Canvas {
    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private final int padding = 4;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    GoodTimes updateTime = new GoodTimes(CaltrainServiceData.schedule_date);
    String updatedAt = updateTime.dateString();
    private String timeOfday;

    public UserCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT2")) {
        display.setCurrent(mainCanvas);
      }
    }

    public void paint(Graphics g) {
      GoodTimes goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      String update = Twine.join(" ", "Schedule effective:", updatedAt);
      g.drawString(update, width / 2, 260, Graphics.HCENTER | Graphics.TOP);
    }
  }


/*
 * Menu Canvas
 */
  class HelpCanvas extends Canvas {
    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private static final int FRAME_DELAY = 40;
    private TimerTask updateTask;
    private Timer timer;
    private final int LITE = 0xBBBBBB;
    private final int GRAY = 0x999999;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    private final int YELLOW = 0xFFFF00;
    private final int padding = 4;
    private SpecialFont specialFont = new SpecialFont();
    private String timeOfday;
    int last_sec = -1;
    GoodTimes updateTime = new GoodTimes(CaltrainServiceData.schedule_date);
    String updatedAt = updateTime.dateString();
    String blurb = "Adjust schedule using keypad.";
    String[] nums = {"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};
    String[] ltrs = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz", "", "", ""};
    String[][] hint = {{"", ""},
        {"Change departing", "station (South)"}, {"Select", "past trains"}, {"Change departing", "station (North)"},
        {"Select", "next train", ""}, {"Show train", "station stops", ""}, {"Swop depart", "and arrive stations"},
        {"Change arriving", "station (South)"}, {"Select", "future trains"}, {"Change arriving", "station (North)"}};
    int x = 20;
    int y = 82;
    int w = 54;
    int h = 30;
    int key_pressed = -1;

    public HelpCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
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
          repaint(0, 0, width, height);
        }
      };
      // when showing only minutes, inverval should be next minute change
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT2")) {
        key_pressed = -1;
        display.setCurrent(mainCanvas);
      } else if ((keyCode > Canvas.KEY_NUM0) && (keyCode <= Canvas.KEY_NUM9)) {
        key_pressed = keyCode - Canvas.KEY_NUM0;
      } else {
        key_pressed = -1;
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:  // 5
        key_pressed = 5;
        break;
      case Canvas.UP:    // 2
        key_pressed = 2;
        break;
      case Canvas.DOWN:  // 8
        key_pressed = 8;
        break;
      case Canvas.LEFT:  // 4
        key_pressed = 4;
        break;
      case Canvas.RIGHT: // 6
        key_pressed = 6;
        break;
      }
      last_minute = -1; // force full paint
      this.repaint();
    }

    public void paint(Graphics g) {
      GoodTimes goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      Toolbar.drawBackIcon(g, width - 18, height - 20);

      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      String update = Twine.join(" ", "Schedule effective:", updatedAt);
      g.drawString(update, width / 2, 260, Graphics.HCENTER | Graphics.TOP);

      int sec = goodtimes.second() % 10;
      int cursor = (sec < 5) ? sec * 2 : sec - (9 - sec);
      int lucky = (key_pressed == -1) ? cursor : key_pressed;
      g.setColor(WHITE);
      int w1 = (width - specialFont.lettersWidth(hint[lucky][0])) / 2;
      int w2 = (width - specialFont.lettersWidth(hint[lucky][1])) / 2;
      specialFont.letters(g, hint[lucky][0], w1, 30);
      specialFont.letters(g, hint[lucky][1], w2, 52);
      int n = 1;
      for (int r = 0; r < 4; r++) {
        for (int c = 0; c < 3; c++) {
          int cx = x + (c * 22) + (c * w);
          int cy = y + (r * 12) + (r * h);
          g.setColor((n == lucky) ? YELLOW : GRAY);
          g.fillArc(cx, cy, w, h, 0, 360);
          g.setColor((n == lucky) ? WHITE : LITE);
          g.fillArc(cx + (w / 5), cy + (h / 4), w / 3, h / 3, 0, 360);
          g.setColor(WHITE);
          g.drawArc(cx, cy, w-1, h-1, 0, 360);
          g.setColor(BLACK);
          g.setFont(largeFont);
          g.drawString(nums[n], cx + (w / 2) - 8, cy + 6, Graphics.RIGHT | Graphics.TOP);
          g.setFont(smallFont);
          g.drawString(ltrs[n], cx + (w / 2) - 6, cy + 9, Graphics.LEFT | Graphics.TOP);
          n++;
        }
      }
    }
  }

/*
 * Trip Canvas
 */
  class TripCanvas extends Canvas {
    private SpecialFont specialFont = new SpecialFont();
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private static final int FRAME_DELAY = 40;
    private TimerTask updateTask;
    private Timer timer;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    private final int CYAN = 0x00AAFF;
    private final int RED = 0xFF0000;
    private final int padding = 4;
    private final int NONE = -1;
    private String timeOfday;
    private int offset = 0;
    private int window = 8;
    private int[] times;
    private String[] stops;
    private final String SO_LONG = "South San Francisco";
    private final String CHOPPED = "So San Francisco";

    public TripCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      String currentMenu = null;
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
          repaint(width - largeFont.stringWidth(timeOfday) - padding,
              padding, largeFont.stringWidth(timeOfday), largeFont.getHeight());
        }
      };
      // when showing only minutes, inverval should be next minute change
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT2")) {
        offset = 0; // reset in case we return
        display.setCurrent(mainCanvas);
      }

      switch(getGameAction(keyCode)) {

      case Canvas.UP:
        offset = (offset == 0) ? 0 : --offset;
        break;
      case Canvas.DOWN:
        if ((times.length > window) && (offset < times.length - window)) ++offset;
        break;
      }
      this.repaint();
    }

    public void paint(Graphics g) {
      GoodTimes goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      currentMinutes = goodtimes.currentMinutes();
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);

      CaltrainTrip thisTrip = new CaltrainTrip(selectedTrain);
      String label = thisTrip.label();
      specialFont.letters(g, label, (width / 2) - (specialFont.lettersWidth(label) / 2), 30);
      g.drawString(thisTrip.description(), width / 2, 52, Graphics.HCENTER | Graphics.TOP);

      times = thisTrip.times;
      stops = thisTrip.stops;
      int indent = width - largeFont.stringWidth(CHOPPED);
      g.setFont(largeFont);
      int spacing = 80;

      int maxWindow = (times.length < window) ? times.length : offset + window;
      int minWindow = (times.length < window) ? 0 : offset;
      for (int i = minWindow; i < maxWindow; i++) {
        String shortStop = (stops[i].equals(SO_LONG)) ? CHOPPED : stops[i];
        g.setColor((times[i] - currentMinutes < 0) ? CYAN : WHITE);
        g.drawString(GoodTimes.fullTime(times[i]), indent - 35, spacing, Graphics.RIGHT | Graphics.TOP);
        g.drawString(shortStop, indent, spacing, Graphics.LEFT | Graphics.TOP);
        g.setColor((times[i] - currentMinutes < 0) ? CYAN : RED);
        if (i > offset) g.fillRect(indent - 19, spacing - 12, 2, 14);
        g.setColor(BLACK);
        g.fillArc(indent - 24, spacing + 2, 11, 11, 0, 360);
        g.setColor(WHITE);
        g.drawArc(indent - 24, spacing + 2, 11, 11, 0, 360);
        spacing += 26;
      }
      last_minute = goodtimes.minute();
    }
  }

/*
 * Main Canvas
 */
  class MainCanvas extends Canvas {
    private NextCaltrain parent = null;
    private String[] stations = CaltrainServiceData.south_stops;
    private SpecialFont specialFont = new SpecialFont();
    private static final int FRAME_DELAY = 40;
    private TimerTask updateTask;
    private Timer timer;
    private String[] labels;
    private String from = "";
    private String dest = "";
    private String timeOfday;
    private String blurb = "";
    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int stopOne = 17;
    private int stopTwo = 1;
    private int width;
    private int height;
    private int second;
    private int SWAP = -1;
    private int data[][];
    private int stopWindow = 6;
    private int betweenMinutes = -1;
    private int selectionMinutes = -1;
    private final int padding = 4;
    private final int CYAN = 0x00AAFF;
    private final int YELLOW = 0xFFFF00;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    private final int GREEN = 0x88CC33;
    private final int LITE = 0xDDDDDD;
    private final int GRAY = 0x666666;
    private final int DARK = 0x333333;

    private boolean menuPoppedUp = false;
    private String selectAction = "";
    int menuSelection = 0;
    int subSelect = -1;
    private String[] menuItems = {"User Preferences", "Keypad Commands", "Depart Station",
                                  "Arrive Station", "Swop Stations", "Exit"};
    private int[][] menuHints = {{},{},{1,3},{7,9},{6},{}};

    public MainCanvas(NextCaltrain parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
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
          repaint(width - largeFont.stringWidth(timeOfday) - padding,
              padding, largeFont.stringWidth(timeOfday), largeFont.getHeight());
          // paint countdown message
          repaint(0, 73, width, 30);
        }
      };
      // when showing only minutes, inverval should be next minute change
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    private void setStops(int swap) {
      if (swap != GoodTimes.AM) {
        int tmp = stopOne;
        stopOne = stopTwo;
        stopTwo = tmp;
      }
    }

    private String[] tripLabels(String from, String dest) {
      String[] out = new String[2];
      if (from.length() >= dest.length()) {
        out[0] = from;
        out[1] = Twine.join(" ", "to", dest);
      } else {
        out[0] = Twine.join(" ", from, "to");
        out[1] = dest;
      }
      return out;
    }

    public void menuSelect(boolean down) {
      if (down) {
        if (menuSelection < menuItems.length - 1) {
          if ((menuHints[menuSelection].length > 1) && (subSelect != menuSelection)) {
            subSelect = menuSelection;
          } else {
            subSelect = -1;
            menuSelection += 1;
          }
        } else {
          subSelect = -1;
          menuSelection = 0;
        }
      } else {
        if (menuSelection > 0) {
          if ((menuHints[menuSelection].length > 1) && (subSelect == menuSelection)) {
            subSelect = -1;
          } else {
            menuSelection -= 1;
            if ((menuHints[menuSelection].length > 1) && (subSelect != menuSelection)) {
              subSelect = menuSelection;
            } else {
              subSelect = -1;
            }
          }
        } else {
          subSelect = -1;
          menuSelection = menuHints.length - 1;
        }
      }
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));
      if (getKeyName(keyCode).equals("SOFT1")) {
        if (!menuPoppedUp) menuPoppedUp = true;
      } else if (getKeyName(keyCode).equals("SOFT2")) {
        if (menuPoppedUp) {
          menuPoppedUp = false;
        } else {
          try {
            destroyApp(true);
            notifyDestroyed();
          } catch (MIDletStateChangeException e) {
            e.printStackTrace();
          }
        }
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        if (menuPoppedUp) {
          menuPoppedUp = false;
          if (subSelect == 2) {
            stopOffset = -1; // 3
            stopOne = (stopOne <= 1) ? stations.length - 1: --stopOne;
          } else if (subSelect == 3) {
            stopOffset = -1;  // 9
            stopTwo = (stopTwo <= 1) ? stations.length - 1: --stopTwo;
          } else if (menuSelection == 2) {
            stopOffset = -1;  // 1
            stopOne = (stopOne == stations.length - 1) ? 1 : ++stopOne;
          } else if (menuSelection == 3) {
            stopOffset = -1;  // 7
            stopTwo = (stopTwo == stations.length - 1) ? 1 : ++stopTwo;
          } else if (menuSelection == 4) {
            stopOffset = -1; // 6
            setStops(SWAP);
          } else if (menuSelection == 5) {
            try {
              destroyApp(true);
              notifyDestroyed();
            } catch (MIDletStateChangeException e) {
              e.printStackTrace();
            }
          } else if (menuSelection == 0) {
            display.setCurrent(userCanvas);
          } else if (menuSelection == 1) {
            display.setCurrent(helpCanvas);
          }
          menuSelection = 0;
          subSelect = -1;
        } else {
          if (data.length > 0) display.setCurrent(tripCanvas);
        }
        break;
      case Canvas.UP:    // 2
        if (menuPoppedUp) {
          menuSelect(false);
        } else {
          stopOffset = (stopOffset == 0) ? data.length - 1 : --stopOffset;
        }
        break;
      case Canvas.DOWN:  // 8
        if (menuPoppedUp) {
          menuSelect(true);
        } else {
          stopOffset = (stopOffset == data.length - 1) ? 0 : ++stopOffset;
        }
        break;
      case Canvas.LEFT:  // 4
        stopOffset = -1;
        break;
      case Canvas.RIGHT: // 6
        stopOffset = -1;
        setStops(SWAP);
        break;
      case GAME_A:       // 1
        stopOffset = -1;
        stopOne = (stopOne == stations.length - 1) ? 1 : ++stopOne;
        break;
      case GAME_B:       // 3
        stopOffset = -1;
        stopOne = (stopOne <= 1) ? stations.length - 1: --stopOne;
        break;
      case GAME_C:       // 7
        stopOffset = -1;
        stopTwo = (stopTwo == stations.length - 1) ? 1 : ++stopTwo;
        break;
      case GAME_D:       // 9
        stopOffset = -1;
        stopTwo = (stopTwo <= 1) ? stations.length - 1: --stopTwo;
        break;
      }
      last_minute = -1; // force full paint
      this.repaint();
    }

    public void paint(Graphics g) {
      GoodTimes goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      if (!menuPoppedUp) Toolbar.drawMenuIcon(g, 18, height - 20);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);

      // Set inital state
      if (from.equals("")) setStops(goodtimes.get(GoodTimes.AM_PM));
      from = stations[stopOne];
      dest = stations[stopTwo];
      second = goodtimes.second();
      currentMinutes = goodtimes.currentMinutes();
      // Load some page defaults
      labels = tripLabels(from, dest);
      data = service.routes(from, dest, goodtimes.dotw());
      if (data.length == 0) stopOffset = 0;
      int index = 0;
      while (stopOffset == -1) {
        if (currentMinutes > data[data.length - 1][CaltrainService.DEPART]) {
          stopOffset = 0;
        } else if (data[index][CaltrainService.DEPART] >= currentMinutes) {
          stopOffset = index;
        }
        index++;
      }
      g.setColor(WHITE);
      specialFont.letters(g, labels[0], (width / 2) - (specialFont.lettersWidth(labels[0]) / 2), 30);
      specialFont.letters(g, labels[1], (width / 2) - (specialFont.lettersWidth(labels[1]) / 2), 52);

      selectionMinutes = (data.length < 1) ? 0 : data[stopOffset][CaltrainService.DEPART];
      betweenMinutes = selectionMinutes - currentMinutes;
      if (data.length < 1) {
        g.setColor(CYAN);
        blurb = (second % 2 == 0) ? "NO TRAINS" : "";
      } else if (betweenMinutes < 0) {
        g.setColor(GRAY);
        blurb = "";
      } else if (betweenMinutes < 1) {
        g.setColor(YELLOW);
        blurb = (second % 2 == 0) ? "DEPARTING" : "";
      } else {
        g.setColor(GREEN);
        blurb = GoodTimes.countdown(betweenMinutes, second);
      }

      int optionLeading = 29;
      int startPosition = 83;
      specialFont.letters(g, blurb, (width / 2) - (specialFont.lettersWidth(blurb) / 2), startPosition + 2);
      if (data.length > 0) {
        g.drawRoundRect(0, startPosition + 26, width - 1, optionLeading, 9, 9);
        selectedTrain = data[stopOffset][CaltrainService.TRAIN];
        if (menuPoppedUp) {
          selectAction = "Select";
        } else {
          selectAction = CaltrainTrip.type(selectedTrain);
        }
        g.setColor(WHITE);
        g.drawString(selectAction, width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);
      } else {
        selectedTrain = -1;
      }
      // nothing to recanculate unless minutes changes or an action
      if (last_minute != goodtimes.minute()) {

        int baseline = startPosition + 20;
        int gutter = 8;
        int trip_width = largeFont.stringWidth("#321");
        int stopOne_width = smallFont.stringWidth(" pm");
        int time_width = specialFont.numbersWidth("12:22");
        int arrive_align = width - padding - stopOne_width;
        int depart_align = arrive_align - gutter - time_width - stopOne_width;
        int maxWindow = (data.length < stopWindow) ? data.length : stopOffset + stopWindow;
        int minWindow = (data.length < stopWindow) ? 0 : stopOffset;
        for (int i = minWindow; i < maxWindow; i++) {
          if ((i > minWindow) && menuPoppedUp) continue;
          int n = (i >= data.length) ? i - data.length : i;
          betweenMinutes = data[n][CaltrainService.DEPART] - currentMinutes;
          baseline += optionLeading;
          int position = baseline - SpecialFont.numbersBaseline;
          g.setColor((betweenMinutes < 0) ? CYAN : WHITE);

          g.setFont(largeFont);
          String train = Twine.join("", "#", data[n][CaltrainService.TRAIN]);
          g.drawString(train, padding, baseline, Graphics.LEFT | Graphics.BASELINE);

          g.setFont(smallFont);
          String[] partOne = GoodTimes.partTime(data[n][CaltrainService.DEPART]);
          specialFont.numbers(g, partOne[0], depart_align - specialFont.numbersWidth(partOne[0]), position);
          g.drawString(partOne[1], depart_align + 3, baseline, Graphics.LEFT | Graphics.BASELINE);

          g.setFont(smallFont);
          String[] partTwo = GoodTimes.partTime(data[n][CaltrainService.ARRIVE]);
          specialFont.numbers(g, partTwo[0], arrive_align - specialFont.numbersWidth(partTwo[0]), position);
          g.drawString(partTwo[1], arrive_align + 3, baseline, Graphics.LEFT | Graphics.BASELINE);
        }

        // Popup Menu
        if (menuPoppedUp) {
          int menuPadding = 6;
          int menuLeading = 20;
          int cbarHeight = 38;
          int keyWidth = 21;
          int menuWidth = (menuPadding * 2) + largeFont.stringWidth("Depart Station _ _ [_][_]");
          int menuHeight = (menuPadding * 2) + (menuLeading * menuItems.length) - 2;
          int menuTop = height - cbarHeight - menuHeight;
          int menuLeft = (width - menuWidth) / 2;
          g.setColor(DARK);
          g.fillRect(menuLeft, menuTop, menuWidth, menuHeight);
          g.setColor(WHITE);
          g.drawRect(menuLeft, menuTop, menuWidth - 1, menuHeight - 1);
          int topLine = menuTop + menuPadding;
          for (int i = 0; i < menuItems.length; i++) {
            if (menuSelection == i) {
              g.setColor(BLACK);
              g.fillRect(menuLeft + 2, topLine, menuWidth - 4, menuLeading);
            }
            g.setFont(largeFont);
            g.setColor((menuSelection == i) ? YELLOW : WHITE);
            g.drawString(menuItems[i], menuLeft + menuPadding + 1, topLine, Graphics.LEFT | Graphics.TOP);
            g.setFont(largeFont);
            for (int n = 0; n < menuHints[i].length; n++) {
              //int keyLeft = menuLeft + menuWidth - ((n + 1) * (keyWidth + menuPadding));
              int keyLeft = menuLeft + menuWidth - (((n - 2) * -1) * (keyWidth + menuPadding));
              if (menuSelection == i) {
                if (menuHints[i].length > 1) {
                  if (n == 0) {
                    if (subSelect == menuSelection) {
                      g.setColor(GRAY);
                    } else {
                      g.setColor(YELLOW);
                    }
                  } else {
                    if (subSelect == menuSelection) {
                      g.setColor(YELLOW);
                    } else {
                      g.setColor(GRAY);
                    }
                  }
                } else {
                  g.setColor(YELLOW);
                }
              } else {
                g.setColor(LITE);
              }
              g.fillRoundRect(keyLeft, topLine, keyWidth, menuLeading - 4, 7, 7);
              g.setColor(BLACK);
              g.drawRoundRect(keyLeft, topLine, keyWidth - 1, menuLeading - 5, 7, 7);
              g.setColor(BLACK);
              String keyLabel = String.valueOf(menuHints[i][n]);
              g.drawString(keyLabel, keyLeft + (keyWidth / 2), topLine, Graphics.HCENTER | Graphics.TOP);
            }
            topLine += menuLeading;
          }
        }
        last_minute = goodtimes.minute();
      }
    }
  }
}
