import javax.microedition.io.*;
import javax.microedition.rms.*;

public class Preferencs {
  private RecordStore rs = null;
  private static final int LENGTH = 4;
  private static final String REC_STORE = "Stops";
  private static final int[] defaults = {25,1};
  public int[] stationStops = new int[2];

  public Preferencs() {
    openRecStore();
    loadStops();
    if (stationStops[1] < 1) {
      saveStops(defaults);
      loadStops();
    }
    closeRecStore();
  }

  public byte[] toByteArray(int value) {
    return new byte[] { (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value };
  }

  public int fromByteArray(byte[] bytes) {
    return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
  }

  public void openRecStore() {
    try {
      rs = RecordStore.openRecordStore(REC_STORE, true );
    } catch (Exception e) {}
  }

  public void closeRecStore() {
    try {
      rs.closeRecordStore();
    } catch (Exception e) {}
  }

  public void deleteRecStore() {
    if (RecordStore.listRecordStores() != null) {
      try {
        RecordStore.deleteRecordStore(REC_STORE);
      } catch (Exception e) {}
    }
  }

  public void saveStops(int[] values) {
    deleteRecStore();
    for (int i = 0; i < values.length; i++) {
      byte[] rec = toByteArray(values[i]);
      try {
        rs.addRecord(rec, 0, rec.length);
      } catch (Exception e) {}
    }
  }

  public void loadStops(){
    try{
      byte[] recData = new byte[LENGTH]; 
      for(int i = 1; i <= rs.getNumRecords(); i++){
        rs.getRecord(i, recData, 0); 
        stationStops[i - 1] = fromByteArray(recData);
      }   
    } catch (Exception e){}
  }

}
