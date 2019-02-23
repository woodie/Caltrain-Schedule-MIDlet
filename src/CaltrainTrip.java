public class CaltrainTrip {

  public int trip;
  public int direction;
  public int schedule;
  public String[] stops;
  public int[] times;
  public static final int SOUTH = CaltrainService.SOUTH; // EVEN
  public static final int NORTH = CaltrainService.NORTH; // ODD
  public static final int WEEKDAY = CaltrainService.WEEKDAY; // < 400
  public static final int WEEKEND = CaltrainService.WEEKEND; // > 400
  public static final int SATURDAY = CaltrainService.SATURDAY;
  public static final int SUNDAY= CaltrainService.SUNDAY;

 /**
  * A train with times for each station stop.
  * @param trip the trip ID
  * @return a list of service stops.
  */
  public CaltrainTrip(int trip) {
    this.trip = trip;
    this.direction = (trip % 2 == SOUTH) ? SOUTH : NORTH;
    this.schedule = (trip < 400) ? WEEKDAY: WEEKEND;
    setService();
  }

 /**
  * Set the time and station name for a trip ID.
  */
  private void setService() {
    int[] mins = CaltrainService.tripStops(trip, direction, schedule);
    String[] strs = (this.direction == NORTH) ? CaltrainServiceData.north_stops : CaltrainServiceData.south_stops;
    // determine size
    int getSize = 0;
    for (int i = 1; i < mins.length; i++) {
      if (mins[i] != -1) getSize++;
    }
    this.times = new int[getSize];
    this.stops = new String[getSize];
    // populate instance
    int setSize = 0;
    for (int i = 1; i < mins.length; i++) {
      if (mins[i] == -1) continue;
      this.times[setSize] = mins[i];
      this.stops[setSize] = strs[i];
      setSize++;
    }
  }

  public static String type(int trip) {
    if (trip > 800) {
      return "Baby Bullet";
    } else if (trip > 400) {
      return "Local";
    } else if (trip > 300) {
      return "Baby Bullet";
    } else if (trip > 200) {
      return "Limited";
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

  public String description() {
    StringBuffer buf = new StringBuffer(20);
    buf.append(CaltrainTrip.type(trip));
    buf.append("  /  ");
    buf.append(schedule());
    return buf.toString();
  }

  public String direction() {
    return (direction == NORTH) ? "Northbound" : "Southbound";
  }

  public String schedule() {
    if (schedule == WEEKDAY) {
      return "Weekday";
    } else {
      for (int x = 0; x < CaltrainService.saturday_trip_ids.length; x++) {
        if (trip == CaltrainService.saturday_trip_ids[x]) return "Saturday";
      }
      return "Weekend";
    }
  }

}
