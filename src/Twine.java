public class Twine {
 
  public static String join(String de, String e1, String e2, String e3, String e4, String e5) {
    StringBuffer buf = new StringBuffer();
    buf.append(e1); buf.append(de); buf.append(e2);
    if (null != e3) { buf.append(de); buf.append(e3); }
    if (null != e4) { buf.append(de); buf.append(e4); }
    if (null != e5) { buf.append(de); buf.append(e5); }
    return buf.toString();
  }
  
  public static String join(String de, String e1, String e2, String e3, String e4) {
    return join(de, e1, e2, e3, e4, null);
  }

  public static String join(String de, String e1, String e2, String e3) {
    return join(de, e1, e2, e3, null, null);
  }

  public static String join(String de, String e1, String e2) {
    return join(de, e1, e2, null, null, null);
  }

  public static String join(String de, String s, int i) {
    return join(de, s, String.valueOf(i), null, null, null);
  }

}
