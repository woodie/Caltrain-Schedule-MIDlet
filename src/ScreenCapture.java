import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import gfx.PNG;

// https://stackoverflow.com/questions/8076878/in-j2me-how-to-save-image-in-phone-memory-for-s40

public class ScreenCapture {
  private Image shadowImage = null;
  private Image encodeImage = null;

  public ScreenCapture(int width, int height) {
    shadowImage = Image.createImage(width, height);
  }

  public Graphics getGraphics() {
    return shadowImage.getGraphics();
  }

  public void writePNG(String filename) {
    try {
      byte[][] rgba = convertIntArrayToByteArrays(shadowImage);
      byte[] encodeImage = PNG.toPNG(shadowImage.getWidth(),
          shadowImage.getHeight(), rgba[0], rgba[1], rgba[2], rgba[3]);
      String fileURL = "file://localhost/TFCard/Photos/" + filename + ".png";
      FileConnection fileConn = null;
      DataOutputStream dos = null;
      fileConn = (FileConnection) Connector.open(fileURL, Connector.READ_WRITE);
      if (!fileConn.exists()) {
        fileConn.create();
      }
      dos = new DataOutputStream(fileConn.openOutputStream());
      dos.write(encodeImage);
      dos.flush();
      dos.close();
      fileConn.close();
    } catch (IOException e) {
      System.out.println("IOException: " + e);
    }
  }

  public byte[][] convertIntArrayToByteArrays(Image img) {
    int[] pixels = new int[img.getWidth() * img.getHeight()];
    img.getRGB(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

    // separate channels
    byte[] red = new byte[pixels.length];
    byte[] green = new byte[pixels.length];
    byte[] blue = new byte[pixels.length];
    byte[] alpha = new byte[pixels.length];

    for (int i = 0; i < pixels.length; i++) {
      int argb = pixels[i];
      //binary operations to separate the channels
      //alpha is the left most byte of the int (0xAARRGGBB)
      alpha[i] = (byte) (argb >> 24);
      red[i] = (byte) (argb >> 16);
      green[i] = (byte) (argb >> 8);
      blue[i] = (byte) (argb);
    }
    return new byte[][]{alpha, red, green, blue};
  }

}
