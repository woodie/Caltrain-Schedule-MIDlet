import javax.microedition.lcdui.Graphics;

/**
 * A utility to provide Nokia 33010 toolbar icons.
 */
class Toolbar {

 /**
  * Draw the menu icon from a center point.
  * @param g the Graphics on which the icon should be painted.
  * @param cx the horizontal center starting point
  * @param cy the vertical center starting point
  * @return three horizontal bars within 20 x 16 footprint.
  */
  public static void drawMenuIcon(Graphics g, int cx, int cy) {
    g.drawRect(cx - 10, cy - 8, 19, 1);
    g.drawRect(cx - 10, cy - 1, 19, 1);
    g.drawRect(cx - 10, cy + 6, 19, 1);
  }

 /**
  * Draw the back icon from a center point.
  * @param g the Graphics on which the icon should be painted.
  * @param cx the horizontal center starting point
  * @param cy the vertical center starting point
  * @return left pointing arrow within 20 x 18 footprint.
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
  * Draw the swap icon from a center point.
  * @param g the Graphics on which the icon should be painted.
  * @param cx the horizontal center starting point
  * @param cy the vertical center starting point
  * @return up and down arrows within 24 x 18 footprint.
  */
  public static void drawSwapIcon(Graphics g, int cx, int cy)  {
    g.drawLine(cx - 12, cy - 4, cx - 9, cy - 7);
    g.drawLine(cx - 12, cy - 3, cx - 9, cy - 6);
    g.drawLine(cx - 11, cy - 3, cx - 9, cy - 5);
    g.drawRect(cx - 8, cy - 8, 1, 15);
    g.drawLine(cx - 6, cy - 7, cx - 3, cy - 4);
    g.drawLine(cx - 6, cy - 6, cx - 3, cy - 3);
    g.drawLine(cx - 6, cy - 5, cx - 4, cy - 3);

    g.drawLine(cx + 2, cy + 2, cx + 5, cy + 5);
    g.drawLine(cx + 2, cy + 3, cx + 5, cy + 6);
    g.drawLine(cx + 3, cy + 2, cx + 5, cy + 4);
    g.drawRect(cx + 6, cy - 8, 1, 15);
    g.drawLine(cx + 8, cy + 4, cx + 10, cy + 2);
    g.drawLine(cx + 8, cy + 5, cx + 11, cy + 2);
    g.drawLine(cx + 8, cy + 6, cx + 11, cy + 3);
  }

}
