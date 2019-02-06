public class CaltrainStop {

  public String label;
  public int north_index;
  public int south_index;

  public CaltrainStop(String label) {
    this.label = label;
    this.north_index = get_north_index(label);
    this.south_index = get_south_index(label);
  }

  private int get_north_index(String label) {
    for (int i = 1; i < CaltrainServieData.north_stops.length; i++) {
      if (CaltrainServieData.north_stops[i].equals(label)) return i;
    }
    return -1;
  }

  private int get_south_index(String label) {
    for (int i = 1; i < CaltrainServieData.south_stops.length; i++) {
      if (CaltrainServieData.south_stops[i].equals(label)) return i;
    }
    return -1;
  }

}
