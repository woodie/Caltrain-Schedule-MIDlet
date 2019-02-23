import java.util.Calendar;
import java.util.Date;

/**
 * A utility to simplify working with the Calendar.
 */
public class GoodTimes{

  Calendar calendar;

  public GoodTimes() {
     calendar = Calendar.getInstance();
  }

  public GoodTimes(long epoch) {
    calendar = Calendar.getInstance();
    Date date = new Date(epoch);
    calendar.setTime(date);
  }

  public int get(int n) {
    return calendar.get(n);
  }

  public static final int AM = Calendar.AM;
  public static final int PM = Calendar.PM;
  public static final int AM_PM = Calendar.AM_PM;

  public static final String daysOfWeek[] = {
      "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

  public static final String monthsOfYear[] = {
      "January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December"};

  public String dayOfTheWeek() {
    return GoodTimes.daysOfWeek[dotw()];
  }

  public String dateString() {
    StringBuffer buf = new StringBuffer(20);
    buf.append(GoodTimes.monthsOfYear[calendar.get(Calendar.MONTH)]);
    buf.append(" ");
    buf.append(calendar.get(Calendar.DAY_OF_MONTH));
    buf.append(", ");
    buf.append(calendar.get(Calendar.YEAR));
    return buf.toString();
  }

  public static String countdown(int minutes, int second) {
    StringBuffer buf = new StringBuffer(20);
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
    StringBuffer buf = new StringBuffer(10);
    buf.append(hour);
    buf.append(min < 10 ? ":0" : ":");
    buf.append(min);
    if (ampm.length() > 0) {
      buf.append(" ");
      buf.append(ampm);
    }
    return buf.toString();
  }

  public static String timeOfday(int hour, int min) {
    return timeOfday(hour, min, "");
  }

  public static String fullTime(int minutes) {
    int hour = minutes / 60;
    String ampm = (hour > 11 && hour < 24) ? "pm" : "am";
    if (hour > 12) hour -= 12;
    if (hour > 12) hour -= 12;
    return timeOfday(hour, minutes % 60, ampm);
  }

  public static String[] partTime(int minutes) {
    String[] out = new String[2];
    int hour = minutes / 60;
    String ampm = (hour > 11 && hour < 24) ? "pm" : "am";
    if (hour > 12) hour -= 12;
    if (hour > 12) hour -= 12;
    out[0] = timeOfday(hour, minutes % 60, "");
    out[1] = ampm;
    return out;
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
