import javax.microedition.lcdui.Graphics;

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

}
