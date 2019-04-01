#!/usr/bin/env python

import csv
import os
import time
import subprocess
from collections import OrderedDict

xstr = lambda s: s or '-1'

def main():
  #fetch_schedule_data()
  trips = parse_trip_data()
  stops = parse_station_data()
  times = parse_schedule_data(trips, stops)
  write_schedule_data(times, stops)

def fetch_schedule_data_denied():
  BASE = 'https://openmobilitydata-data.s3-us-west-1.amazonaws.com/public/feeds/caltrain/122/20190131/original'
  basedir = os.getcwd()
  subprocess.call(['mkdir', '-p', 'CT-GTFS'])
  os.chdir('CT-GTFS')
  subprocess.call(['curl', '-O', '%s/trips.txt' % BASE])
  subprocess.call(['curl', '-O', '%s/stops.txt' % BASE])
  subprocess.call(['curl', '-O', '%s/stop_times.txt' % BASE])
  os.chdir(basedir)

def fetch_schedule_data():
  source = 'http://www.caltrain.com/Assets/GTFS/caltrain/CT-GTFS.zip'
  basedir = os.getcwd()
  subprocess.call(['mkdir', '-p', 'downloads'])
  os.chdir('downloads')
  subprocess.call(['rm', 'CT-GTFS.zip'])
  subprocess.call(['curl', '-O', source])
  os.chdir(basedir)
  subprocess.call(['mkdir', '-p', 'CT-GTFS'])
  os.chdir('CT-GTFS')
  subprocess.call(['unzip', '-o', '../downloads/CT-GTFS.zip'])
  os.chdir(basedir)

def parse_trip_data():
  _trips = {}
  with open('CT-GTFS/trips.txt', 'rb') as tripsFile:
    tripsReader = csv.reader(tripsFile)
    header = next(tripsReader, None)
    trip_id_x = header.index('trip_id')
    trip_name_x = header.index('trip_short_name')
    for row in tripsReader:
      trip_id = row[trip_id_x]
      trip_name = row[trip_name_x]
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

def parse_schedule_data(trips, stops):
  _times = {'weekday':{'north':OrderedDict(), 'south':OrderedDict()},
            'weekend':{'north':OrderedDict(), 'south':OrderedDict()}}
  with open('CT-GTFS/stop_times.txt', 'rb') as timesFile:
    timesReader = csv.reader(timesFile)
    header = next(timesReader, None)
    trip_id_x = header.index('trip_id')
    stop_id_x = header.index('stop_id')
    departure_x = header.index('departure_time')
    sortedLines = sorted(timesReader, key=lambda row: row[departure_x])
    for row in sortedLines:
      trip_num = trips[row[trip_id_x]]
      if (len(trip_num) > 4):
        continue # skip special times HERE
      trip_id = int(trip_num)
      stop_id = int(row[stop_id_x])
      hour = int(row[departure_x][0:-6])
      minute = int(row[departure_x][-5:-3])
      departure = str(hour * 60 + minute)
      direction = 'north' if (stop_id % 2 == 1) else 'south'
      schedule = 'weekday' if (trip_id < 400) else 'weekend'
      if (trip_id < 800 and trip_id > 500):
        continue # skip special times
      if (trip_id not in _times[schedule][direction]):
        _times[schedule][direction][trip_id] = [None] * len(stops[direction])
      _times[schedule][direction][trip_id][stops[direction].index(stop_id)] = departure
  return _times

def write_schedule_data(times, stops):
  with open('src/CaltrainServiceData.java', 'w') as f:
    f.write("public class CaltrainServiceData {\n")
    stat = os.stat('CT-GTFS/stop_times.txt')
    creation = 0
    try:
      creation = long(stat.st_birthtime * 1000)
    except AttributeError:
      creation = long(stat.st_mtime * 1000)
    f.write("\n  public static final long schedule_date = %dL;\n" % creation)
    for direction in ['north', 'south']:
      f.write("\n  public static final String %s_stops[] = {" % (direction))
      f.write('\n      "')
      labels = ['']
      for stop_id in stops[direction]:
        labels.append(stops['labels'][stop_id])
      f.write('","'.join(labels))
      f.write('"};\n')
      for schedule in ['weekday', 'weekend']:
        f.write("\n  public static final int %s_%s[][] = {" % (direction, schedule))
        f.write('\n      {')
        header = ['0']
        for stop_id in stops[direction]:
          header.append(str(stop_id))
        f.write(','.join(header))
        for trip_id in times[schedule][direction]:
          f.write('},\n      {')
          f.write(','.join(map(xstr,[str(trip_id)] + times[schedule][direction][trip_id])))
        f.write('}};\n')
    f.write('\n}\n')


if __name__ == "__main__":
    main()
