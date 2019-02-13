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
  private MenuCanvas menuCanvas = null;
  private MainCanvas mainCanvas = null;
  private TripCanvas tripCanvas = null;
  private int selectedTrain = -1;
  private int stopOffset = -1;
  private int currentMinutes = -1;
  protected CaltrainService service = new CaltrainService();

  public NextCaltrain() {
    display = Display.getDisplay(this);
    mainCanvas = new MainCanvas(this);
    menuCanvas = new MenuCanvas(this);
    tripCanvas = new TripCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(mainCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  public void itemStateChanged(Item item) {}


  class MenuCanvas extends Canvas {
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
    private final int BLACK = 0x000000;
    private final int WHITE = 0xFFFFFF;
    private SpecialFont specialFont = new SpecialFont();

    public MenuCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
    }

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT2")) {
        display.setCurrent(mainCanvas);
      }
      this.repaint();
    }

    public void paint(Graphics g) {
      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);

      g.setColor(WHITE);
      Toolbar.drawBackIcon(g, width - 18, height - 20);
      int fw = specialFont.lettersWidth("MENU");
      specialFont.letters(g, "MENU", (width / 2) - (fw / 2), height / 2);
    }

  }

  class TripCanvas extends Canvas {
    private SpecialFont specialFont = new SpecialFont();
    private Font largeFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private int width;
    private int height;
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
    private final String CHOPPED = "S. San Francisco";

    public TripCanvas(NextCaltrain parent) {
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      String currentMenu = null;
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
        if (i > offset) g.fillRect(indent - 19, spacing - 8, 2, 14);
        g.setColor(BLACK);
        g.fillArc(indent - 24, spacing + 2, 11, 11, 0, 360);
        g.setColor(WHITE);
        g.drawArc(indent - 24, spacing + 2, 11, 11, 0, 360);
        spacing += 26;
      }
    }
  }

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
    private int last_minute = -1;
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
    private final int GRAY= 0x666666;

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

    public void keyPressed(int keyCode){
      pressed.addElement(getKeyName(keyCode));

      if (getKeyName(keyCode).equals("SOFT1")) {
        display.setCurrent(menuCanvas);
      } else if (getKeyName(keyCode).equals("SOFT2")) {
        try {
          destroyApp(true);
          notifyDestroyed();
        } catch (MIDletStateChangeException e) {
          e.printStackTrace();
        }
      }

      switch(getGameAction(keyCode)) {

      case Canvas.FIRE:
        if (data.length > 0) display.setCurrent(tripCanvas);
        break;
      case Canvas.UP:
        stopOffset = (stopOffset == 0) ? data.length - 1 : --stopOffset;
        break;
      case Canvas.DOWN:
        stopOffset = (stopOffset == data.length - 1) ? 0 : ++stopOffset;
        break;
      case GAME_A:
        stopOffset = -1;
        stopOne = (stopOne == stations.length - 1) ? 1 : ++stopOne;
        break;
      case GAME_B:
        stopOffset = -1;
        stopOne = (stopOne <= 1) ? stations.length - 1: --stopOne;
        break;
      case Canvas.LEFT:
        stopOffset = -1;
        stopTwo = (stopTwo == stations.length - 1) ? 1 : ++stopTwo;
        break;
      case Canvas.RIGHT:
        stopOffset = -1;
        stopTwo = (stopTwo <= 1) ? stations.length - 1: --stopTwo;
        break;
      case GAME_C:
        stopOffset = -1;
        break;
      case GAME_D:
        stopOffset = -1;
        setStops(SWAP);
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
      Toolbar.drawMenuIcon(g, 18, height - 20);
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
        String tripType = CaltrainTrip.type(selectedTrain);
        g.setColor(WHITE);
        g.drawString(tripType, width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);
      } else {
        selectedTrain = -1;
      }
      // some repaint call can end here
      int baseline = 100;
      int gutter = 8;
      int trip_width = largeFont.stringWidth("#321");
      int stopOne_width = smallFont.stringWidth(" pm");
      int time_width = specialFont.numbersWidth("12:22");
      int arrive_align = width - padding - stopOne_width;
      int depart_align = arrive_align - gutter - time_width - stopOne_width;
      int maxWindow = (data.length < stopWindow) ? data.length : stopOffset + stopWindow;
      int minWindow = (data.length < stopWindow) ? 0 : stopOffset;
      for (int i = minWindow; i < maxWindow; i++) {
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
      last_minute = goodtimes.minute();
    }

  }

}
