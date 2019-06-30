#!/usr/bin/env python

import csv

def main():
  trips = parse_trip_data()
  stops = parse_station_data()
  check = parse_schedule()
  patch_schedule_data(trips, stops, check)

def parse_trip_data():
  _trips = {}
  with open('CT-GTFS/trips.txt', 'rb') as tripsFile:
    tripsReader = csv.reader(tripsFile)
    header = next(tripsReader, None)
    trip_id_x = header.index('trip_id')
    service_id_x = header.index('service_id')
    try:
      trip_name_x = header.index('trip_short_name')
    except:
      trip_name_x = trip_id_x
    for row in tripsReader:
      trip_id = row[trip_id_x]
      trip_name = row[trip_name_x]
      if "_none_" not in row[service_id_x]:
        _trips[trip_id] = trip_name
  return _trips

def parse_station_data():
  _stops = {'north':[], 'south':[], 'labels':{}}
  extra = ['Caltrain', 'Station']
  with open('CT-GTFS/stops.txt', 'rb') as stopsFile:
    stopsReader = csv.reader(stopsFile)
    header = next(stopsReader, None)
    stop_id_x = header.index('stop_id')
    stop_name_x = header.index('stop_name')
    for row in stopsReader:
      stop_id = int(row[stop_id_x])
      if (stop_id > 70400):
        continue # skip fake stops
      stop_name = ' '.join(i for i in row[stop_name_x].split() if i not in extra)
      stop_name = stop_name.replace("South San", "So San")
      _stops['labels'][stop_id] = stop_name
      if (stop_id % 2 == 1):
        _stops['north'].insert(0, stop_id)
      else:
        _stops['south'].append(stop_id)
  return _stops

def parse_schedule():
  _check = {}
  for direction in ['north', 'south']:
    for schedule in ['weekday', 'weekend']:
      with open('data/%s_%s.csv' % (schedule, direction), 'rb') as dataFile:
        dataReader = csv.reader(dataFile)
        header = next(dataReader, None)
        for n in range(1, len(header)):
          trip_id = int(header[n])
          _check[trip_id] = {}
        for row in dataReader:
          stop = row[0]
          for n in range(1, len(row)):
            trip_id = int(header[n])
            _check[trip_id][stop] = row[n]
  return _check

def patch_schedule_data(trips, stops, check):
  missing = set()
  with open('CT-GTFS/stop_times.txt', 'rb') as timesFile:
    timesReader = csv.reader(timesFile)
    header = next(timesReader, None)
    trip_id_x = header.index('trip_id')
    stop_id_x = header.index('stop_id')
    arrival_x = header.index('arrival_time')
    departure_x = header.index('departure_time')
    for row in timesReader:
      try:
        trip_num = trips[row[trip_id_x]]
        trip_id = int(trip_num)
      except:
        continue
      if trip_id not in check:
        missing.add(trip_id)
        continue
      stop_id = int(row[stop_id_x])
      stop_label = stops['labels'][stop_id]
      arrival = row[arrival_x]
      departure = row[departure_x]
      direction = 'north' if (stop_id % 2 == 1) else 'south'
      checktime = check[trip_id][stop_label]
      if departure != checktime:
        print "%s %s: %s -> %s" % (trip_id, stop_label, departure, checktime)
  if len(missing) > 0:
    print "Missing Trip IDs: %s" % sorted(missing)


if __name__ == "__main__":
    main()
