public class CaltrainTrip {

  public int trip;
  public int direction;
  public int schedule;
  public int[] times;
  public String[] stops;
  public static final int NORTH = 0;
  public static final int SOUTH = 1;
  public static final int WEEKEND = 0;
  public static final int WEEKDAY = 8;
  public static final int saturday_trip_ids[] = {421,443,442,444}; // Saturday Only

  public CaltrainTrip(int trip, int direction, int schedule, int[] times, String[] stops) {
    this.trip = trip;
    this.direction = direction;
    this.schedule = schedule;
    this.stops = stops;
    this.times = times;
  }

  public String description() {
    if (trip > 800) {
      return "Baby Bullet";
    } else if (schedule == WEEKDAY) {
      if (trip > 300) {
        return "Baby Bullet";
      } else if (trip > 100) {
        return "Limited";
      }
    }
    return "Local";
  }

  public String label() {
    StringBuffer buf = new StringBuffer(20);
    buf.append("#");
    buf.append(trip);
    buf.append(" ");
    buf.append(direction());
    return buf.toString();
  }

  public String direction() {
    return (direction == NORTH) ? "Northbound" : "Southbound";
  }

  public String schedule() {
    if (schedule == WEEKDAY) {
      return "Weekday";
    } else {
      for (int x = 0; x < CaltrainTrip.saturday_trip_ids.length; x++) {
        if (trip == CaltrainTrip.saturday_trip_ids[x]) return "Saturday";
      }
      return "Weekend";
    }
  }

}
