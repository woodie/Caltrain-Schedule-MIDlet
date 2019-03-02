import javax.microedition.lcdui.Graphics;

/**
 * A utility to provide buttons.
 */
class Buttons {

  private static final int keyWidth = 14;
  private static final int keyHeight = 14;

  public static void drawDown(Graphics g, int cx, int cy, int align) {
    if (align == Graphics.RIGHT) cx -= keyWidth;
    g.drawRoundRect(cx, cy, keyWidth, keyHeight, 7, 7);
    g.drawLine(cx + 3, cy + 6, cx + 7, cy + 10);
    g.drawLine(cx + 8, cy + 9, cx + 11, cy + 6);
  }

  public static void drawUp(Graphics g, int cx, int cy, int align) {
    if (align == Graphics.RIGHT) cx -= keyWidth;
    g.drawRoundRect(cx, cy, keyWidth, keyHeight, 7, 7);
    g.drawLine(cx + 3, cy + 8, cx + 7, cy + 4);
    g.drawLine(cx + 8, cy + 5, cx + 11, cy + 8);
  }

  public static void drawLeft(Graphics g, int cx, int cy, int align) {
    if (align == Graphics.RIGHT) cx -= keyWidth;
    g.drawRoundRect(cx, cy, keyWidth, keyHeight, 7, 7);
    g.drawLine(cx + 8, cy + 3, cx + 4, cy + 7);
    g.drawLine(cx + 5, cy + 8, cx + 8, cy + 11);
  }

  public static void drawRight(Graphics g, int cx, int cy, int align) {
    if (align == Graphics.RIGHT) cx -= keyWidth;
    g.drawRoundRect(cx, cy, keyWidth, keyHeight, 7, 7);
    g.drawLine(cx + 6, cy + 3, cx + 10, cy + 7);
    g.drawLine(cx + 9, cy + 8, cx + 6, cy + 11);
  }

}
