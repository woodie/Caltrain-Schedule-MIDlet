import java.io.*;
import java.lang.Integer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/*
 * Caltrain Schedule
 */
public class NextCaltrain extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private Command cmd_Exit = null;
  private MainCanvas mainCanvas = null;

  public NextCaltrain() {
    display = Display.getDisplay(this);
    mainCanvas = new MainCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(mainCanvas);
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

  class MainCanvas extends Canvas {
    private NextCaltrain parent = null;
    private String[] stations = CaltrainServieData.south_stops;
    private CaltrainServie service = new CaltrainServie();
    private SpecialFont specialFont = new SpecialFont();
    private Vector pressed = new Vector();
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
    private int stopOffset = -1;
    private int stopWindow = 6;
    private int currentMinutes = -1;
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
        setStops(SWAP);
        break;
      case GAME_D:
        stopOffset = -1;
        setStops(SWAP); // for now
        break;
      }
      last_minute = -1; // force full paint
      this.repaint();
    }

    public void paint(Graphics g) {
      GoodTimes goodtimes = new GoodTimes();
      timeOfday = goodtimes.timeOfday(true);
      // Set inital state
      if (from.equals("")) setStops(goodtimes.get(GoodTimes.AM_PM));
      from = stations[stopOne];
      dest = stations[stopTwo];
      second = goodtimes.second();
      currentMinutes = goodtimes.currentMinutes();

      g.setColor(BLACK);
      g.fillRect(0, 0, width, height);
      g.setColor(WHITE);
      Toolbar.drawMenuIcon(g, 18, height - 20);
      Toolbar.drawBackIcon(g,width - 18, height - 20);
      g.setFont(largeFont);
      g.drawString(timeOfday, width - padding, padding, Graphics.RIGHT | Graphics.TOP);
      g.setColor(WHITE);
      g.setFont(largeFont);
      g.drawString(goodtimes.dayOfTheWeek(), width / 2, height - padding, Graphics.HCENTER | Graphics.BOTTOM);
      // Load some page defaults
      labels = tripLabels(from, dest);
      data = service.routes(from, dest, goodtimes.dotw());
      if (data.length == 0) stopOffset = 0;
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
      specialFont.letters(g, labels[0], (width / 2) - (specialFont.lettersWidth(labels[0]) / 2), 30);
      specialFont.letters(g, labels[1], (width / 2) - (specialFont.lettersWidth(labels[1]) / 2), 52);

      selectionMinutes = (data.length < 1) ? 0 : data[stopOffset][CaltrainServie.DEPART];
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
      int startPosition = 80;
      specialFont.letters(g, blurb, (width / 2) - (specialFont.lettersWidth(blurb) / 2), startPosition + 3);
      if (data.length > 0) {
        g.drawRoundRect(0, startPosition + 27, width - 1, optionLeading, 9, 9);
      }
      // some repaint call can end here
      int position = 88;
      int gutter = 8;
      int trip_width = largeFont.stringWidth("#321");
      int stopOne_width = smallFont.stringWidth(" pm");
      int time_width = specialFont.numbersWidth("12:22");
      int arrive_align = width - padding - stopOne_width;
      int depart_align = arrive_align - gutter - time_width - stopOne_width;
      int maxWindow = (data.length < stopWindow) ? data.length : stopOffset + stopWindow;
      int minWindow = (data.length < stopWindow) ? 0 : stopOffset;
      for (int i = minWindow; i < maxWindow; i++) {
        position += optionLeading;
        int n = (i >= data.length) ? i - data.length : i;
        betweenMinutes = data[n][CaltrainServie.DEPART] - currentMinutes;
        int t_id = data[n][CaltrainServie.TRAIN];
        int d_hr = data[n][CaltrainServie.DEPART] / 60;
        int d_mn = data[n][CaltrainServie.DEPART] % 60;
        String depart_stopOne = "am";
        if (d_hr > 11 && d_hr < 24) depart_stopOne = "pm";
        if (d_hr > 12) d_hr -= 12;
        String depart = GoodTimes.timeOfday(d_hr, d_mn);
        int a_hr = data[n][CaltrainServie.ARRIVE] / 60;
        int a_mn = data[n][CaltrainServie.ARRIVE] % 60;
        String arrive_stopOne = "am";
        if (a_hr > 11 && a_hr < 24) arrive_stopOne = "pm";
        if (a_hr > 24) a_hr -= 24;
        if (a_hr > 12) a_hr -= 12;
        String arrive = GoodTimes.timeOfday(a_hr, a_mn);
        String train = Twine.join("", "#", String.valueOf(t_id));

        g.setFont(largeFont);
        g.setColor((betweenMinutes < 0) ? CYAN : WHITE);
        g.drawString(train, padding, position - 2, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        specialFont.numbers(g, depart, depart_align - specialFont.numbersWidth(depart), position - 6);
        g.drawString(depart_stopOne, depart_align + 3, position, Graphics.LEFT | Graphics.TOP);

        g.setFont(smallFont);
        specialFont.numbers(g, arrive, arrive_align - specialFont.numbersWidth(arrive), position - 6);
        g.drawString(arrive_stopOne, arrive_align + 3, position, Graphics.LEFT | Graphics.TOP);
      }
      last_minute = goodtimes.minute();
    }

  }

}
