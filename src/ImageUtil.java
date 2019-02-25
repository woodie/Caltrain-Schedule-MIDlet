/*
 * https://stackoverflow.com/questions/8076878/in-j2me-how-to-save-image-in-phone-memory-for-s40
 *
 * toPNG(int,int,byte[],byte[],byte[],byte[])
 *
 * the byte arrays are in order: alpha, red, green and... blue.
 * The width and height are pretty straightforward:+getWidth():int and +getHeight():int
 * from the Image object(as you done)and others are earned by ** convertIntArrayToByteArrays**:
 *
 * The first ints are the width and height of the image,
 *
 * byte[][] rgba = convertIntArrayToByteArrays(galleryImage);
 * byte[] encodeImage = toPNG(galleryImage.getWidth(),galleryImage.getHeight(),
 *                            rgba[0] ,rgba[1] ,rgba[2],rgba[3]);   
 * Now you can save encodeImage in file by fileconnection.
 */

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ImageUtil {

 /*
  * Gets the channels of the image passed as parameter.
  * @param img Image
  * @return matrix of byte array representing the channels:
  * [0] alpha channel, [1] red channel, [2] green channel, [3] blue channel
  */
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
