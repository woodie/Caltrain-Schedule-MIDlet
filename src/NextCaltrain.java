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

  protected Preferencs preferences = new Preferencs();
  protected CaltrainService service = new CaltrainService();
  private String[] stations = CaltrainServiceData.south_stops;
  private Vector pressed = new Vector();
  private Display display = null;
  private UserCanvas userCanvas = null;
  private TripCanvas tripCanvas = null;
  private MainCanvas mainCanvas = null;
  private boolean swopped = false;
  private boolean noChange = true;
  private int stopAM;
  private int stopPM;
  private String from = "";
  private String dest = "";
  private int data[][];
  private String keyLabel = "";
  private String selectAction = "";
  private int leftmost;
  private int selectedTrain = -1;
  private int stopOffset = -1;
  private int currentMinutes = -1;
  private int last_minute = -1;
  private final int cbarHeight = 38;
  private final int BLACK = 0x000000;
  private final int WHITE = 0xFFFFFF;
  private final int GREEN = 0x00FF00; // 0x88CC33;
  private final int RED = 0xFF0000;
  private final int YELLOW = 0xFFFF00;
  private final int CYAN = 0x00AAFF;
  private final int GR86 = 0xDDDDDD;
  private final int GR80 = 0xCCCCCC;
  private final int GR50 = 0x333333;
  private final int GR40 = 0x666666;
  private final String SO_LONG = "South San Francisco";
  private final String CHOPPED = "So San Francisco";

  public NextCaltrain() {
    display = Display.getDisplay(this);
    userCanvas = new UserCanvas(this);
    tripCanvas = new TripCanvas(this);
    mainCanvas = new MainCanvas(this);
    stopAM = preferences.stationStops[0];
    stopPM = preferences.stationStops[1];
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
    private SpecialFont specialFont = new SpecialFont();
    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private final int padding = 4;
    private String timeOfday;
    private String[] labels;
    private boolean menuPoppedUp = false;

    public UserCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT2")) {
        stopOffset = -1;
        display.setCurrent(mainCanvas);
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        int newDefults[] = {stopAM, stopPM};
        preferences.openRecStore();
        preferences.saveStops(newDefults); 
        preferences.loadStops();
        preferences.closeRecStore();
        stopOffset = -1;
        display.setCurrent(mainCanvas);
        break;
      case Canvas.UP:    // 2
        stopAM = (stopAM == stations.length - 1) ? 1 : ++stopAM;
        break;
      case Canvas.DOWN:  // 8
        stopAM = (stopAM <= 1) ? stations.length - 1: --stopAM;
        break;
      case Canvas.LEFT:  // 4
        stopPM = (stopPM == stations.length - 1) ? 1 : ++stopPM;
        break;
      case Canvas.RIGHT: // 6
        stopPM = (stopPM <= 1) ? stations.length - 1: --stopPM;
        break;
      case GAME_A:       // 1
        stopAM = (stopAM == stations.length - 1) ? 1 : ++stopAM;
        break;
      case GAME_B:       // 3
        stopAM = (stopAM <= 1) ? stations.length - 1: --stopAM;
        break;
      case GAME_C:       // 7
        stopPM = (stopPM == stations.length - 1) ? 1 : ++stopPM;
        break;
      case GAME_D:       // 9
        stopPM = (stopPM <= 1) ? stations.length - 1: --stopPM;
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
      Toolbar.drawSwopIcon(g, 18, height - 20);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString("Next Caltrain", padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);

      int groupHeight = (height / 2) - cbarHeight - 10; 

      String group1 = "Default stations";
      int section1 = cbarHeight;
      int knockout1 = (width - largeFont.stringWidth(group1)) / 2 - 4;
      g.setColor(CYAN);
      g.drawRoundRect(0, section1, width - 1, groupHeight, 15, 15);
      g.setColor(BLACK);
      g.drawLine(knockout1, section1, width - knockout1, section1);
      g.setColor(CYAN);
      g.drawString(group1, width / 2, section1 - 10, Graphics.HCENTER| Graphics.TOP);

      noChange = ((stations[stopAM] == stations[preferences.stationStops[0]]) &&
                  (stations[stopPM] == stations[preferences.stationStops[1]]));

      from = stations[preferences.stationStops[0]];
      dest = stations[preferences.stationStops[1]];
      from = (from.equals(SO_LONG)) ? CHOPPED : from;
      dest = (dest.equals(SO_LONG)) ? CHOPPED : dest;
      g.setColor(WHITE);
      g.drawString("Morning", width / 2, section1 + 14, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, from, (width / 2) - (specialFont.lettersWidth(from) / 2), section1 + 34);
      g.drawString("Evening", width / 2, section1 + 56, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, dest, (width / 2) - (specialFont.lettersWidth(dest) / 2), section1 + 76);

      String group2 = "Selected stations";
      int section2 = height - cbarHeight - groupHeight;
      int knockout2 = (width - largeFont.stringWidth(group1)) / 2 - 4;
      g.setColor(noChange ? GR80 : GREEN);
      g.drawRoundRect(0, section2, width - 1, groupHeight, 15, 15);
      g.setColor(BLACK);
      g.drawLine(knockout2, section2, width - knockout2, section2);
      g.setColor(noChange ? GR80 : GREEN);
      g.drawString(group2, width / 2, section2 - 10, Graphics.HCENTER| Graphics.TOP);

      from = stations[stopAM];
      dest = stations[stopPM];
      from = (from.equals(SO_LONG)) ? CHOPPED : from;
      dest = (dest.equals(SO_LONG)) ? CHOPPED : dest;
      g.setColor(noChange ? GR80 : WHITE);
      g.drawString("Morning", width / 2, section2 + 14, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, from, (width / 2) - (specialFont.lettersWidth(from) / 2), section2 + 34);
      g.drawString("Evening", width / 2, section2 + 56, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, dest, (width / 2) - (specialFont.lettersWidth(dest) / 2), section2 + 76);

      int keyWidth = 19;
      int keyHeight = 14;

      leftmost = padding * 2;
      keyLabel = "1";
      g.setColor(GR86);
      g.fillRoundRect(leftmost, section2 + 36, keyWidth, keyHeight, 7, 7);
      g.setColor(BLACK);
      g.drawString(keyLabel, leftmost + (keyWidth / 2), section2 + 36, Graphics.HCENTER | Graphics.TOP);

      leftmost = width - keyWidth - leftmost;
      keyLabel = "3";
      g.setColor(GR86);
      g.fillRoundRect(leftmost, section2 + 36, keyWidth, keyHeight, 7, 7);
      g.setColor(BLACK);
      g.drawString(keyLabel, leftmost + (keyWidth / 2), section2 + 36, Graphics.HCENTER | Graphics.TOP);

      leftmost = padding * 2;
      keyLabel = "7";
      g.setColor(GR86);
      g.fillRoundRect(leftmost, section2 + 78, keyWidth, keyHeight, 7, 7);
      g.setColor(BLACK);
      g.drawString(keyLabel, leftmost + (keyWidth / 2), section2 + 78, Graphics.HCENTER | Graphics.TOP);

      leftmost = width - keyWidth - leftmost;
      keyLabel = "9";
      g.setColor(GR86);
      g.fillRoundRect(leftmost, section2 + 78, keyWidth, keyHeight, 7, 7);
      g.setColor(BLACK);
      g.drawString(keyLabel, leftmost + (keyWidth / 2), section2 + 78, Graphics.HCENTER | Graphics.TOP);

      g.setColor(WHITE);
      selectAction = noChange ? "" : "Update";
      g.drawString(selectAction, width / 2, height - padding + 2, Graphics.HCENTER | Graphics.BOTTOM);
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
    private final int padding = 4;
    private final int NONE = -1;
    private String timeOfday;
    private int offset = 0;
    private int window = 8;
    private int[] times;
    private String[] stops;

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
      int spacing = 80;
      int indent = width - largeFont.stringWidth(CHOPPED);
      g.setFont(largeFont);
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
    private SpecialFont specialFont = new SpecialFont();
    private static final int FRAME_DELAY = 40;
    private TimerTask updateTask;
    private Timer timer;
    private String[] labels;
    private String timeOfday;
    private String blurb = "";
    private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private int second;
    private int SWAP = -1;
    private int stopWindow = 6;
    private int betweenMinutes = -1;
    private int selectionMinutes = -1;
    private final int padding = 4;

    private boolean menuPoppedUp = false;
    int menuSelection = 0;
    int subSelect = -1;
    private String[] menuItems = {"Set default stations", "Swop stations", "Shift departure",
                                  "Shift arrival", "Swop schedules", "Exit"};
    private int[][] menuHints = {{},{4},{1,3},{7,9},{6},{}};

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
        int tmp = stopAM;
        stopAM = stopPM;
        stopPM = tmp;
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

    public void bailout() {
      try {
        destroyApp(true);
        notifyDestroyed();
      } catch (MIDletStateChangeException e) {
        e.printStackTrace();
      }
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));
      if (getKeyName(keyCode).equals("SOFT1")) {
        if (!menuPoppedUp) menuPoppedUp = true;
      } else if (getKeyName(keyCode).equals("SOFT2")) {
        if (menuPoppedUp) {
          menuSelection = 0;
          subSelect = -1;
          menuPoppedUp = false;
          last_minute = -1; // force full paint
        } else {
          bailout();
        }
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        if (menuPoppedUp) {
          menuPoppedUp = false;
          if (subSelect == 2) {
            stopOffset = -1;  // 3
            stopAM = (stopAM <= 1) ? stations.length - 1: --stopAM;
          } else if (subSelect == 3) {
            stopOffset = -1;  // 9
            stopPM = (stopPM <= 1) ? stations.length - 1: --stopPM;
          } else if (menuSelection == 2) {
            stopOffset = -1;  // 1
            stopAM = (stopAM == stations.length - 1) ? 1 : ++stopAM;
          } else if (menuSelection == 3) {
            stopOffset = -1;  // 7
            stopPM = (stopPM == stations.length - 1) ? 1 : ++stopPM;
          } else if (menuSelection == 4) {
            stopOffset = -1;  // 6
            swopped = (swopped) ? false : true;
          } else if (menuSelection == 5) {
            bailout();
          } else if (menuSelection == 0) {
            display.setCurrent(userCanvas);
          } else if (menuSelection == 1) {
            setStops(SWAP);
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
        if (menuPoppedUp) {
          menuSelect(false);
        } else {
          menuPoppedUp = false;
          stopOffset = -1;
          setStops(SWAP);
        }
        break;
      case Canvas.RIGHT: // 6
        if (menuPoppedUp) {
          menuSelect(true);
        } else {
          swopped = (swopped) ? false : true;
          menuPoppedUp = false;
          stopOffset = -1;
        }
        break;
      case GAME_A:       // 1
        menuPoppedUp = false;
        stopOffset = -1;
        stopAM = (stopAM == stations.length - 1) ? 1 : ++stopAM;
        break;
      case GAME_B:       // 3
        menuPoppedUp = false;
        stopOffset = -1;
        stopAM = (stopAM <= 1) ? stations.length - 1: --stopAM;
        break;
      case GAME_C:       // 7
        menuPoppedUp = false;
        stopOffset = -1;
        stopPM = (stopPM == stations.length - 1) ? 1 : ++stopPM;
        break;
      case GAME_D:       // 9
        menuPoppedUp = false;
        stopOffset = -1;
        stopPM = (stopPM <= 1) ? stations.length - 1: --stopPM;
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
      from = stations[stopAM];
      dest = stations[stopPM];
      second = goodtimes.second();
      currentMinutes = goodtimes.currentMinutes();
      // Load some page defaults
      labels = tripLabels(from, dest);
      int dotw = goodtimes.dotw();
      data = service.routes(from, dest, dotw, swopped);
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

      if (swopped) {
        g.setColor(CYAN);
        boolean weekday = (CaltrainService.schedule(dotw, true) == CaltrainService.WEEKDAY);
        blurb = (weekday) ? "Weekday Schedule" : "Weekend Schedule";
      } else if (data.length < 1) {
        g.setColor(CYAN);
        blurb = (second % 2 == 0) ? "NO TRAINS" : "";
      } else if (betweenMinutes < 0) {
        g.setColor(GR40);
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
        if (!swopped) g.drawRoundRect(0, startPosition + 26, width - 1, optionLeading, 9, 9);
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
      if ((last_minute != goodtimes.minute()) || menuPoppedUp) {
        int baseline = startPosition + 20;
        int gutter = 8;
        int trip_width = largeFont.stringWidth("#321");
        int stopAM_width = smallFont.stringWidth(" pm");
        int time_width = specialFont.numbersWidth("12:22");
        int arrive_align = width - padding - stopAM_width;
        int depart_align = arrive_align - gutter - time_width - stopAM_width;
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
          g.drawString(train, padding, baseline + 6, Graphics.LEFT | Graphics.BOTTOM);

          g.setFont(smallFont);
          String[] partOne = GoodTimes.partTime(data[n][CaltrainService.DEPART]);
          specialFont.numbers(g, partOne[0], depart_align - specialFont.numbersWidth(partOne[0]), position);
          g.drawString(partOne[1], depart_align + 3, baseline + 5, Graphics.LEFT | Graphics.BOTTOM);

          g.setFont(smallFont);
          String[] partTwo = GoodTimes.partTime(data[n][CaltrainService.ARRIVE]);
          specialFont.numbers(g, partTwo[0], arrive_align - specialFont.numbersWidth(partTwo[0]), position);
          g.drawString(partTwo[1], arrive_align + 3, baseline + 5, Graphics.LEFT | Graphics.BOTTOM);
        }

        // Popup Menu
        if (menuPoppedUp) {
          int menuPadding = 6;
          int menuLeading = 20;
          int keyWidth = 19;
          int menuWidth = (menuPadding * 2) + largeFont.stringWidth("Shift departure___ [_][_]");
          int menuHeight = (menuPadding * 2) + (menuLeading * menuItems.length) - 2;
          int menuTop = height - cbarHeight - menuHeight;
          int menuLeft = (width - menuWidth) / 2;
          g.setColor(GR50);
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
              int keyLeft = menuLeft + menuWidth - (((n - menuHints[i].length) * -1) * (keyWidth + menuPadding));
              if (menuSelection == i) {
                if (menuHints[i].length > 1) {
                  if (n == 0) {
                    if (subSelect == menuSelection) {
                      g.setColor(GR40);
                    } else {
                      g.setColor(YELLOW);
                    }
                  } else {
                    if (subSelect == menuSelection) {
                      g.setColor(YELLOW);
                    } else {
                      g.setColor(GR40);
                    }
                  }
                } else {
                  g.setColor(YELLOW);
                }
              } else {
                g.setColor(GR86);
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
