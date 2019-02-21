import javax.microedition.lcdui.Graphics;

/**
 * A utility to provide Nokia 33010 toolbar icons.
 */
class Toolbar {

 /**
  * Draw the menu icon from a center point.
  */
  public static void drawMenuIcon(Graphics g, int cx, int cy) {
    g.drawRect(cx - 10, cy - 8, 19, 1);
    g.drawRect(cx - 10, cy - 1, 19, 1);
    g.drawRect(cx - 10, cy + 6, 19, 1);
  }

 /**
  * Draw the back icon from a center point.
  */
  public static void drawBackIcon(Graphics g, int cx, int cy)  {
    g.drawLine(cx - 6, cy - 2, cx, cy - 8);
    g.drawLine(cx - 7, cy - 2, cx, cy - 9);
    g.drawLine(cx - 8, cy - 2, cx - 1, cy - 9);
    g.drawRect(cx - 9, cy - 1, 19, 1);
    g.drawLine(cx - 8, cy + 1, cx - 1, cy + 8);
    g.drawLine(cx - 7, cy + 1, cx, cy + 8);
    g.drawLine(cx - 6, cy + 1, cx, cy + 7);
  }

 /**
  * Draw the swop icon from a center point.
  */
  public static void drawSwopIcon(Graphics g, int cx, int cy)  {
    g.drawLine(cx - 12, cy - 5, cx - 9, cy - 8);
    g.drawLine(cx - 12, cy - 4, cx - 9, cy - 7);
    g.drawLine(cx - 11, cy - 4, cx - 9, cy - 6);
    g.drawRect(cx - 8, cy - 9, 1, 17);
    g.drawLine(cx - 6, cy - 8, cx - 3, cy - 5);
    g.drawLine(cx - 6, cy - 7, cx - 3, cy - 4);
    g.drawLine(cx - 6, cy - 6, cx - 4, cy - 4);

    g.drawLine(cx + 2, cy + 3, cx + 5, cy + 6);
    g.drawLine(cx + 2, cy + 4, cx + 5, cy + 7);
    g.drawLine(cx + 3, cy + 3, cx + 5, cy + 5);
    g.drawRect(cx + 6, cy - 9, 1, 17);
    g.drawLine(cx + 8, cy + 5, cx + 10, cy + 3);
    g.drawLine(cx + 8, cy + 6, cx + 11, cy + 3);
    g.drawLine(cx + 8, cy + 7, cx + 11, cy + 4);
  }

}
