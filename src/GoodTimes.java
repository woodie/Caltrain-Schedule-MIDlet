import java.util.Calendar;

public class GoodTimes{

  Calendar calendar;

  public GoodTimes() {
     calendar = Calendar.getInstance();
  }

  public int get(int n) {
    return calendar.get(n);
  }

  public static final int AM = Calendar.AM;
  public static final int PM = Calendar.PM;
  public static final int AM_PM = Calendar.AM_PM;

  public static final String daysOfWeek[] = {
      "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

  public String dayOfTheWeek() {
    return GoodTimes.daysOfWeek[dotw()];
  }

  public static String countdown(int minutes, int second) {
    StringBuffer buf = new StringBuffer(99);
    buf.append("in ");
    if (minutes > 59) {
      buf.append(minutes / 60);
      buf.append(" hr ");
      buf.append(minutes % 60);
      buf.append(" min");
    } else {
      buf.append(minutes);
      buf.append(" min ");
      buf.append(60 - second);
      buf.append(" sec");
    }
    return buf.toString();
  }

  public static String timeOfday(int hour, int min, String ampm) {
    StringBuffer buf = new StringBuffer(99);
    buf.append(String.valueOf(hour));
    buf.append((min < 10 ? ":0" : ":"));
    buf.append(String.valueOf(min));
    if (ampm.length() > 0) {
      buf.append(" ");
      buf.append(ampm);
    }
    return buf.toString();
  }

  public static String timeOfday(int hour, int min) {
    return timeOfday(hour, min, "");
  }

  public String timeOfday(boolean withAmPm) {
    return GoodTimes.timeOfday(hour(), minute(), (withAmPm) ? ampm() : "");
  }

  public String ampm() {
    return (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "am" : "pm";
  }

  public int hr24() {
   return  get(Calendar.HOUR_OF_DAY);
  }

  public int hour() {
    int hr =  get(Calendar.HOUR);
    return (hr < 1) ? hr + 12: hr;
  }

  public int minute() {
    return get(Calendar.MINUTE);
  }

  public int second() {
    return get(Calendar.SECOND);
  }

  public int dotw() {
    return calendar.get(Calendar.DAY_OF_WEEK);
  }

  public int currentMinutes() {
    return hr24() * 60 + minute();
  }

}
