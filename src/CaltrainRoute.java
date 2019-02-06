import java.util.Hashtable;
import java.util.Vector;

public class CaltrainRoute {

  CaltrainStop departStop;
  CaltrainStop arriveStop;
  int direction;
  int NORTH = 0;
  int SOUTH = 1;
  int schedule;
  int WEEKDAY = 0;
  int SATURDAY = 1;
  int SUNDAY = 2;
  Vector trips;

  public CaltrainRoute(String departStation, String arriveStation, int schedule) {
    this.departStop = new CaltrainStop(departStation);
    this.arriveStop = new CaltrainStop(arriveStation);
    this.schedule = schedule;
    this.direction = (this.departStop.south_index < this.arriveStop.south_index) ? NORTH : SOUTH;
    this.trips = get_trips(this.departStop, this.arriveStop, this.direction, this.schedule); 
  }

  private Vector get_trips(CaltrainStop departStop, CaltrainStop arriveStop, int direction, int schedule) {
    Vector out = new Vector();
    int departIndex;
    int arriveIndex;
    int data[][];
    if (direction == NORTH) { 
      departIndex = departStop.north_index;
      arriveIndex = arriveStop.north_index;
      data = (schedule == WEEKDAY) ? CaltrainServieData.north_weekday : CaltrainServieData.north_weekend;
    } else {
      departIndex = departStop.south_index;
      arriveIndex = arriveStop.south_index;
      data = (schedule == WEEKDAY) ? CaltrainServieData.south_weekday : CaltrainServieData.south_weekend;
    }
    for (int i = 1; i < data.length; i++) {
      if ((data[i][departIndex] != -1) && (data[i][arriveIndex] != -1)) {
        out.addElement(new CaltrainTrip(data[i][0], data[i][departIndex], data[i][arriveIndex]));
      }
    } 
    return out;
  }

}
