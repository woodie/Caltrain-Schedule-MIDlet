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

  protected final String appTitle = "Next Caltrain";
  protected Preferences preferences = new Preferences();
  protected CaltrainService service = new CaltrainService();
  private GoodTimes goodtimes;
  private String[] stations = CaltrainServiceData.south_stops;
  private int width = 240;
  private int height = 320;
  private Display display = null;
  private InfoCanvas infoCanvas = null;
  private UserCanvas userCanvas = null;
  private TripCanvas tripCanvas = null;
  private MainCanvas mainCanvas = null;
  private String timeOfday;
  private final int padding = 4;
  private boolean swapped = false;
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
  private final int cbarHeight = 38;
  private final int BLACK = 0x000000;
  private final int WHITE = 0xFFFFFF;
  private final int GREEN = 0x00FF00;
  private final int RED = 0xFF0000;
  private final int SALMON = 0xFF8888;
  private final int YELLOW = 0xFFFF00;
  private final int CYAN = 0x00AAFF;
  private final int GR86 = 0xDDDDDD;
  private final int GR80 = 0xCCCCCC;
  private final int GR40 = 0x666666;
  private final int GR26 = 0x444444;
  private final int GR20 = 0x333333;
  private final int DKBL = 0x000055;
  private final int SWOP = -1;
  private Font smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
  private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
  private SpecialFont specialFont = new SpecialFont();
  private final String LONGEST = "So San Francisco";
  private boolean toggle;

  public NextCaltrain() {
    display = Display.getDisplay(this);
    infoCanvas = new InfoCanvas(this);
    userCanvas = new UserCanvas(this);
    tripCanvas = new TripCanvas(this);
    mainCanvas = new MainCanvas(this);
    stopAM = preferences.stationStops[0];
    stopPM = preferences.stationStops[1];
  }

  private void setStops(int swap) {
    stopAM = ((stopAM > 0) && (stopAM < stations.length)) ? stopAM : Preferences.defaults[0];
    stopPM = ((stopPM > 0) && (stopPM < stations.length)) ? stopPM : Preferences.defaults[1];
    if (swap != GoodTimes.AM) {
      int tmp = stopAM;
      stopAM = stopPM;
      stopPM = tmp;
    }
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(mainCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  public void itemStateChanged(Item item) {}


/*
 * Info Canvas
 */
  class InfoCanvas extends Canvas {

    public InfoCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
    }

    public void keyPressed(int keyCode){
      if (getKeyName(keyCode).equals("SOFT2")) {
        display.setCurrent(mainCanvas);
      }
    }

    public void paint(Graphics g) {
      GoodTimes ut = new GoodTimes(CaltrainServiceData.schedule_date);
      String updatedAt = ut.dateString();
      goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      currentMinutes = goodtimes.currentMinutes();
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(appTitle, padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);

      int position = 90;
      specialFont.letters(g, appTitle, (width / 2) - (specialFont.lettersWidth(appTitle) / 2), position);
      String[] lines = {"(c) 2019 John Woodell", "",
          Twine.join(" ", "Version", getAppProperty("MIDlet-Version"), "for J2ME"),
          getAppProperty("MIDlet-Info-URL"), "", "Schedule effective:", updatedAt};
      for (int i = 0; i < lines.length; i++) {
        position += 20;
        g.drawString(lines[i], width / 2, position, Graphics.HCENTER | Graphics.TOP);
      }
    }
  }

/*
 * User Canvas
 */
  class UserCanvas extends Canvas {
    private String[] labels;
    private boolean menuPoppedUp = false;

    public UserCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
    }

    public void savePreferences(int[] defults) {
      preferences.deleteRecStore();
      preferences.openRecStore();
      preferences.saveStops(defults);
      preferences.loadStops();
      preferences.closeRecStore();
    }

    public void keyPressed(int keyCode){
      if (getKeyName(keyCode).equals("SOFT1")) {
          stopOffset = -1;
          setStops(SWOP);
      } else if (getKeyName(keyCode).equals("SOFT2")) {
        stopOffset = -1;
        display.setCurrent(mainCanvas);
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        if (!noChange) {
          int newDefults[] = {stopAM, stopPM};
          savePreferences(newDefults);
          stopOffset = -1;
          display.setCurrent(mainCanvas);
        }
        break;
      case Canvas.UP:    // 2
        stopAM = (stopAM <= 1) ? stations.length - 1: --stopAM;
        break;
      case Canvas.DOWN:  // 8
        stopAM = (stopAM == stations.length - 1) ? 1 : ++stopAM;
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
      this.repaint();
    }

    public void paint(Graphics g) {
      goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      g.setColor(WHITE);
      Toolbar.drawSwapIcon(g, 18, height - 20);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(appTitle, padding, padding, Graphics.LEFT | Graphics.TOP);
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
      g.setColor(noChange ? GR80 : WHITE);
      g.drawString("Morning", width / 2, section2 + 14, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, from, (width / 2) - (specialFont.lettersWidth(from) / 2), section2 + 34);
      g.drawString("Evening", width / 2, section2 + 56, Graphics.HCENTER| Graphics.TOP);
      specialFont.letters(g, dest, (width / 2) - (specialFont.lettersWidth(dest) / 2), section2 + 76);

      int keyWidth = 14;
      int keyHeight = 14;

      leftmost = padding * 2 + 2;
      g.setColor(GR26);
      g.drawRoundRect(leftmost, section2 + 35, keyWidth, keyHeight, 7, 7);
      g.setColor(GR86);
      g.drawLine(leftmost + 3, section2 + 35 + 6, leftmost + 7, section2 + 35 + 10);
      g.drawLine(leftmost + 8, section2 + 35 + 9, leftmost + 11, section2 + 35 + 6);

      leftmost = width - keyWidth - leftmost;
      g.setColor(GR26);
      g.drawRoundRect(leftmost, section2 + 35, keyWidth, keyHeight, 7, 7);
      g.setColor(GR86);
      g.drawLine(leftmost + 3, section2 + 35 + 8, leftmost + 7, section2 + 35 + 4);
      g.drawLine(leftmost + 8, section2 + 35 + 5, leftmost + 11, section2 + 35 + 8);

      leftmost = padding * 2 + 2;
      g.setColor(GR26);
      g.drawRoundRect(leftmost, section2 + 78, keyWidth, keyHeight, 7, 7);
      g.setColor(GR86);
      g.drawLine(leftmost + 8, section2 + 78 + 3, leftmost + 4, section2 + 78 + 7);
      g.drawLine(leftmost + 5, section2 + 78 + 8, leftmost + 8, section2 + 78 + 11);

      leftmost = width - keyWidth - leftmost;
      g.setColor(GR26);
      g.drawRoundRect(leftmost, section2 + 78, keyWidth, keyHeight, 7, 7);
      g.setColor(GR86);
      g.drawLine(leftmost + 6, section2 + 78 + 3, leftmost + 10, section2 + 78 + 7);
      g.drawLine(leftmost + 9, section2 + 78 + 8, leftmost + 6, section2 + 78 + 11);

      g.setColor(WHITE);
      selectAction = noChange ? "" : "Update";
      g.drawString(selectAction, width / 2, height - padding + 2, Graphics.HCENTER | Graphics.BOTTOM);
    }
  }


/*
 * Trip Canvas
 */
  class TripCanvas extends Canvas {
    private final int NONE = -1;
    private int offset = 0;
    private int window = 8;
    private int[] times;
    private String[] stops;

    public TripCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      String currentMenu = null;
    }

    public void keyRepeated(int keyCode){
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

    public void keyPressed(int keyCode){

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
      goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      currentMinutes = goodtimes.currentMinutes();
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(appTitle, padding, padding, Graphics.LEFT | Graphics.TOP);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);

      CaltrainTrip thisTrip = new CaltrainTrip(selectedTrain);
      String label = thisTrip.label();
      specialFont.letters(g, label, (width / 2) - (specialFont.lettersWidth(label) / 2), 30);
      g.drawString(thisTrip.description(), width / 2, 52, Graphics.HCENTER | Graphics.TOP);

      times = thisTrip.times;
      stops = thisTrip.stops;
      int spacing = 80;
      if  (times.length > window) {
        int maxScroll = height - spacing - cbarHeight;
        g.setColor(GR20);
        g.fillRect(width - 10, spacing, 10, maxScroll);
        int chunk = maxScroll / times.length;
        int slider = (chunk * window) + (maxScroll % times.length);
        g.setColor(DKBL);
        g.fillRect(width - 10, chunk * offset + spacing, 10, slider);
      }
      int indent = width - largeFont.stringWidth(LONGEST);
      g.setFont(largeFont);
      int maxWindow = (times.length < window) ? times.length : offset + window;
      int minWindow = (times.length < window) ? 0 : offset;
      for (int i = minWindow; i < maxWindow; i++) {
        boolean selectedStop = (from.equals(stops[i]) || dest.equals(stops[i]));
        g.setColor((times[i] - currentMinutes < 0) ? CYAN : WHITE);
        g.drawString(GoodTimes.fullTime(times[i]), indent - 35, spacing, Graphics.RIGHT | Graphics.TOP);
        g.drawString(stops[i], indent, spacing, Graphics.LEFT | Graphics.TOP);
        g.setColor((times[i] - currentMinutes < 0) ? CYAN : RED);
        if (i > offset) g.fillRect(indent - 19, spacing - 12, 2, 14);
        g.setColor(selectedStop ? RED : BLACK);
        g.fillArc(indent - 24, spacing + 2, 12, 12, 0, 360);
        g.setColor(selectedStop ? SALMON : GR40);
        g.drawArc(indent - 24, spacing + 2, 11, 11, 0, 360);
        g.fillArc(indent - 21, spacing + 5, 3, 3, 0, 360);
        spacing += 26;
      }
    }
  }

/*
 * Main Canvas
 */
  class MainCanvas extends Canvas {
    private final long SECOND = 1000;
    private NextCaltrain parent = null;
    private TimerTask updateTask;
    private Timer timer;
    private String[] labels;
    private String blurb = "";
    private int second;
    private int stopWindow = 6;
    private int betweenMinutes = -1;
    private int selectionMinutes = -1;
    private boolean fullRepaint = false;
    private boolean menuPoppedUp = false;
    private int menuSelection = 0;
    private int subSelect = -1;
    private String[] menuItems = {"Set default stations", "Swap stations", "Set origin",
                                  "Set destination", "Swap schedules", "About", "Exit"};
    private int[][] menuHints = {{},{4},{1,3},{7,9},{6},{},{}};

    public MainCanvas(NextCaltrain parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
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
          if (fullRepaint) {
            repaint();
            fullRepaint = false;
          } else {
            // paint the clock
            repaint(width - largeFont.stringWidth(timeOfday) - padding,
                padding, largeFont.stringWidth(timeOfday), largeFont.getHeight());
            // paint countdown message ... 85 down, 18 tall
            repaint(0, 85, width, 18);
          }
        }
      };
      timer.schedule(updateTask, SECOND / 2, SECOND / 2);
    }

    protected void stopFrameTimer() {
      timer.cancel();
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

    public void keyRepeated(int keyCode){
      switch(getGameAction(keyCode)) {

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
      }
      this.repaint();
    }

    public void keyPressed(int keyCode){
      if (getKeyName(keyCode).equals("SOFT1")) {
        if (!menuPoppedUp) menuPoppedUp = true;
      } else if (getKeyName(keyCode).equals("SOFT2")) {
        if (menuPoppedUp) {
          menuSelection = 0;
          subSelect = -1;
          menuPoppedUp = false;
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
            swapped = (swapped) ? false : true;
          } else if (menuSelection == 5) {
            display.setCurrent(infoCanvas);
          } else if (menuSelection == 6) {
            bailout();
          } else if (menuSelection == 0) {
            GoodTimes gt = new GoodTimes();
            if (gt.get(GoodTimes.AM_PM) == GoodTimes.PM) setStops(SWOP);
            display.setCurrent(userCanvas);
          } else if (menuSelection == 1) {
            setStops(SWOP);
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
          setStops(SWOP);
        }
        break;
      case Canvas.RIGHT: // 6
        if (menuPoppedUp) {
          menuSelect(true);
        } else {
          swapped = (swapped) ? false : true;
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
      this.repaint();
    }

    public void paint(Graphics g) {
      goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      if (!menuPoppedUp) Toolbar.drawMenuIcon(g, 18, height - 20);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      g.drawString(appTitle, padding, padding, Graphics.LEFT | Graphics.TOP);

      // Set inital state
      if (from.equals("")) setStops(goodtimes.get(GoodTimes.AM_PM));
      from = stations[stopAM];
      dest = stations[stopPM];
      second = goodtimes.second();
      currentMinutes = goodtimes.currentMinutes();
      // Load some page defaults
      labels = tripLabels(from, dest);
      int dotw = goodtimes.dotw();
      data = service.routes(from, dest, dotw, swapped);
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

      toggle = (toggle) ? false : true;

      if (swapped) {
        g.setColor(CYAN);
        boolean weekday = (CaltrainService.schedule(dotw, true) == CaltrainService.WEEKDAY);
        blurb = (weekday) ? "Weekday Schedule" : "Weekend Schedule";
      } else if (data.length < 1) {
        g.setColor(CYAN);
        blurb = (toggle) ? "NO TRAINS" : "";
      } else if (betweenMinutes < 0) {
        g.setColor(GR40);
        blurb = "";
      } else if (betweenMinutes < 1) {
        g.setColor(YELLOW);
        blurb = (toggle) ? "DEPARTING" : "";
      } else {
        g.setColor(GREEN);
        blurb = GoodTimes.countdown(betweenMinutes, second);
      }
      fullRepaint = ((second > 58) || (second < 1)) ? true : false;

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
      int baseline = startPosition + 20;
      int gutter = 8;
      int trip_width = largeFont.stringWidth("#321");
      int stopAM_width = smallFont.stringWidth(" pm");
      int time_width = specialFont.numbersWidth("12:22");
      int arrive_align = width - padding - stopAM_width;
      int depart_align = arrive_align - gutter - time_width - stopAM_width;
      int maxWindow = (data.length < stopWindow) ? stopOffset + data.length : stopOffset + stopWindow;
      for (int i = stopOffset; i < maxWindow; i++) {
        if ((i > stopOffset) && menuPoppedUp) continue;
        int n = (i >= data.length) ? i - data.length : i;
        betweenMinutes = data[n][CaltrainService.DEPART] - currentMinutes;
        baseline += optionLeading;
        int position = baseline - SpecialFont.numbersBaseline;
        g.setColor((betweenMinutes < 0) ? CYAN : WHITE);

        g.setFont(largeFont);
        String train = Twine.join("", "#", data[n][CaltrainService.TRAIN]);
        g.drawString(train, padding, position + 5, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        String[] partOne = GoodTimes.partTime(data[n][CaltrainService.DEPART]);
        specialFont.numbers(g, partOne[0], depart_align - specialFont.numbersWidth(partOne[0]), position);
        g.drawString(partOne[1], depart_align + 3, position + 8, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        String[] partTwo = GoodTimes.partTime(data[n][CaltrainService.ARRIVE]);
        specialFont.numbers(g, partTwo[0], arrive_align - specialFont.numbersWidth(partTwo[0]), position);
        g.drawString(partTwo[1], arrive_align + 3, position + 8, Graphics.LEFT | Graphics.TOP);
      }

      // Popup Menu
      if (menuPoppedUp) {
        int menuPadding = 6;
        int menuLeading = 20;
        int keyWidth = 19;
        int menuWidth = (menuPadding * 2) + largeFont.stringWidth("Set destination __ [_][_]");
        int menuHeight = (menuPadding * 2) + (menuLeading * (menuItems.length - 1)) - 2;
        int menuTop = height - cbarHeight - menuHeight;
        int menuLeft = (width - menuWidth) / 2;
        g.setColor(GR20);
        g.fillRect(menuLeft, menuTop, menuWidth, menuHeight);
        g.setColor(WHITE);
        g.drawRect(menuLeft, menuTop, menuWidth - 1, menuHeight - 1);
        int topLine = menuTop + menuPadding;
        for (int i = 0; i < menuItems.length; i++) {
          if (i == menuItems.length - 1) {
            topLine -= menuLeading;
            menuLeft = menuLeft + (menuWidth / 2);
          }
          if (menuSelection == i) {
            g.setColor(BLACK);
            int selectionWidth = (i < menuItems.length - 2) ? menuWidth - 4 : menuWidth / 2 - 3;
            g.fillRect(menuLeft + 2, topLine - 2, selectionWidth, menuLeading);
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
    }
  }
}
