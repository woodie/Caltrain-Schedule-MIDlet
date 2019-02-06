public class CaltrainServie {

  public Hashtable northStops;
  public Hashtable southStops;
  public static final int NORTH = 0;
  public static final int SOUTH = 1;
  public static final int WEEKDAY = 0;
  public static final int SATURDAY = 1;
  public static final int SUNDAY = 2;

  public CaltrainServie() {
    this.northStops = mapStops(NORTH);
    this.southStops = mapStops(SOUTH);
  }

  public Hashtable stops(int direction) {
    return (direction == NORTH) ? this.northStops : this.southStops;
  }
  
 /**
  * For direction and day-of-the-week: train times (or IDs)
  * @param stop the Station name (or null for IDS)
  * @param direction is NORTH or SOUTH
  * @param schedule is WEEKDAY, SATURDAY or SUNDAY
  * @return the station times (or IDs)
  */
  public int[] times(String stop, int direction, int schedule) {
    int[][] source = select(direction, schedule);
    int[] times = new int[source.length - 1];
    int column = (null == String) ? 0 : ((Integer)stops(direction).get(stop)).intValue();
    for (int i = 0; i < times.lenght; i++) {
      times[i] = source[i + 1][column]; // Note:  source contans stop_id header
    }
    return times
  }

 /**
  * Select a schedule for direction and day-of-the-week.
  * @param direction is NORTH or SOUTH
  * @param schedule is WEEKDAY, SATURDAY or SUNDAY
  * @return a two dementional array or ints
  */
  public int[][] select(int direction, int schedule) {
    if (direction == NORTH) {
      return (schedule == WEEKDAY) ? CaltrainServie.north_weekday : CaltrainServie.north_weekend;
    } else { 
      return (schedule == WEEKDAY) ? CaltrainServie.south_weekday : CaltrainServie.south_weekend;
    }
  }

 /**
  * Map the stops for provided direction
  * @param direction is NORTH or SOUTH
  * @return stop name string mapping to schedule columns.
  */
  private Hashtable mapStops(int direction) {
    Hashtable out = new Hashtable();
    int stops[] = (direction == NORTH) ? CaltrainServieData.north_stops : CaltrainServieData.south_stops
    for (int i = 1; i < stops.length; i++) {
      out.put(stops[i], new Integer(i));
    }
    return out;
  }

}
