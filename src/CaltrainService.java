import java.util.Calendar;
import java.util.Hashtable;

/**
* A utility to simplify working with CaltrainServiceData.
*/
public class CaltrainService {

  public Hashtable northStops;
  public Hashtable southStops;
  public Hashtable tripLookup;
  public static final int SOUTH = 0;
  public static final int NORTH = 1;
  public static final int WEEKEND = 0;
  public static final int SUNDAY = Calendar.SUNDAY;
  public static final int SATURDAY = Calendar.SATURDAY;
  public static final int WEEKDAY = 8;
  public static final int TRAIN = 0;
  public static final int DEPART = 1;
  public static final int ARRIVE = 2;
  public static final int DIRECTION = 0;
  public static final int SCHEDULE = 1;
  public static final int TRIP_IDX = 2;
  public static final int saturday_trip_ids[] = {421,443,442,444}; // Saturday Only

  public CaltrainService() {
    this.northStops = mapStops(NORTH);
    this.southStops = mapStops(SOUTH);
    //this.tripLookup = mapTrips();
  }

 /**
  * Station name maps to index of column with stop times
  * @param direction the northbound and southbound schedules
  * @return Hashmap of Station Name keys
  */
  private Hashtable mapStops(int direction) {
    Hashtable out = new Hashtable();
    String stops[] = (direction == NORTH) ? CaltrainServiceData.north_stops : CaltrainServiceData.south_stops;
    for (int i = 1; i < stops.length; i++) {
      out.put(stops[i], new Integer(i));
    }
    return out;
  }

 /**
  * Location of trip information; we may not need to initialize this
  * because direction and schedule can be determined from the trip ID.
  */
  private Hashtable mapTrips() {
    Hashtable out = new Hashtable();
    for (int direction = NORTH; direction <= SOUTH; direction++) {
      for (int schedule = SATURDAY; schedule <= WEEKDAY; schedule++) {
        int trips[][] = CaltrainService.select(direction, schedule);
        for (int idx = 1; idx < trips.length; idx++) {
          out.put(String.valueOf(trips[idx][TRAIN]), new int[] {direction, schedule, idx});
        }
      }
    }
    return out;
  }

 /**
  * Station name maps to index of column with stop times
  * @param trip is trip ID.
  * @param direction is NORTH or SOUTH.
  * @param schedule is WEEKDAY or WEEKEND.
  * @return array of Station stop times.
  */
  public static int[] tripStops(int trip, int direction, int schedule) {
    int trips[][] = CaltrainService.select(direction, schedule);
    for (int i = 1; i < trips.length; i++) {
      if (trips[i][TRAIN] == trip) return trips[i];
    }
    return new int[0];
  }

 /**
  * Map the stops for provided direction
  * @param direction is NORTH or SOUTH
  * @return stop name string mapping to schedule columns.
  */
  public Hashtable stops(int direction) {
    return (direction == NORTH) ? this.northStops : this.southStops;
  }

 /**
  * Determine the direction given two stops
  * @param departStop the departing stop name string
  * @param arriveStop the arriving stop name string
  * @return the direction of this trip: NORTH or SOUTH
  */
  public int direction(String departStop, String arriveStop) {
    int depart = ((Integer)southStops.get(departStop)).intValue();
    int arrive = ((Integer)southStops.get(arriveStop)).intValue();
    return (depart < arrive) ? SOUTH : NORTH;
  }

 /**
  * Return the schedule routes
  * @param trains the train IDs
  * @param departStop the departing stop name string
  * @param arriveStop the arriving stop name string
  * @param dotw a Calendar day-of-the-week
  * @return a two dementional array or ints
  */
  public int[][] routes(String departStop, String arriveStop, int dotw) {
    int schedule = schedule(dotw);
    int direction = direction(departStop, arriveStop);
    int[] trains = times(null, direction, schedule);
    int[] departTimes = times(departStop, direction, schedule);
    int[] arriveTimes = times(arriveStop, direction, schedule);
    int[] skip = (dotw == SUNDAY) ? CaltrainService.saturday_trip_ids : new int[0];
    return merge(trains, departTimes, arriveTimes, skip);
  }

 /**
  * Convert day-of-the-week into a schedule
  * @param dotw the Calendar day-of-the-week
  * @return the schedule
  */
  public static int schedule(int dotw) {
    return ((dotw == SATURDAY) || (dotw == SATURDAY)) ? dotw : WEEKDAY;
  }

 /**
  * Merge two stop into a subset of the schedule
  * @param trains the train IDs
  * @param departStop the departing stop name string
  * @param arriveStop the arriving stop name string
  * @param skip over these trips
  * @return a two dementional array or ints
  */
  public int[][] merge(int[] trains, int[] departTimes, int[] arriveTimes, int[] skip) {
    int[][] tmp = new int[trains.length][3];
    int count = 0;
    for (int i = 0; i < trains.length; i++) {
      for (int x = 0; x < skip.length; x++) {
        if (trains[i] == x) continue;
      }
      if ((departTimes[i] != -1) && (arriveTimes[i] != -1)) {
        tmp[count][TRAIN] = trains[i];
        tmp[count][DEPART] = departTimes[i];
        tmp[count][ARRIVE] = arriveTimes[i];
        count++;
      }
    }
    int[][] out = new int[count][3];
    for (int i = 0; i < count; i++) {
      for (int n = 0; n < 3; n++) {
        if ((i > 0) && (out[i - 1][DEPART] > tmp[i][DEPART])) {
          out[i][n] = out[i - 1][n];
          out[i - 1][n] = tmp[i][n];
        } else {
          out[i][n] = tmp[i][n];
        }
      }
    }
    return out;
  }

 /**
  * For direction and day-of-the-week: train times (or IDs)
  * @param stop the Stop name (or null for IDs)
  * @param direction is NORTH or SOUTH
  * @param schedule is WEEKDAY, SATURDAY or SUNDAY
  * @return the stop times (or IDs)
  */
  public int[] times(String stop, int direction, int schedule) {
    int[][] source = CaltrainService.select(direction, schedule);
    int[] times = new int[source.length - 1]; // offset for stop_id header
    int column = (null == stop) ? 0 : ((Integer)stops(direction).get(stop)).intValue();
    for (int i = 0; i < times.length; i++) {
      times[i] = source[i + 1][column];       // skip the stop_id header
    }
    return times;
  }

 /**
  * Select a schedule for direction and day-of-the-week.
  * @param direction is NORTH or SOUTH
  * @param schedule is WEEKDAY, SATURDAY or SUNDAY
  * @return a two dementional array or ints
  */
  public static int[][] select(int direction, int schedule) {
    if (direction == NORTH) {
      return (schedule == WEEKDAY) ? CaltrainServiceData.north_weekday : CaltrainServiceData.north_weekend;
    } else {
      return (schedule == WEEKDAY) ? CaltrainServiceData.south_weekday : CaltrainServiceData.south_weekend;
    }
  }

}
