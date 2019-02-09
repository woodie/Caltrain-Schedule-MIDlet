#!/usr/bin/env python

import csv
import os
import time
import subprocess
from collections import OrderedDict

xstr = lambda s: s or '-1'

def main():
  fetch_schedule_data()
  stops = parse_station_data()
  trips = parse_schedule_data(stops)
  write_schedule_data(trips, stops)

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

def parse_station_data():
  _stops = {'north':[], 'south':[], 'labels':{}}
  extra = ['Diridon', 'Caltrain', 'Station']
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
      _stops['labels'][stop_id] = stop_name
      if (stop_id % 2 == 1):
        _stops['north'].insert(0, stop_id)
      else:
        _stops['south'].append(stop_id)
  return _stops

#   Weekday           Weekend          Special
#   100: Local        400 Local        500
#   200: Limited
#   300: Baby Bullet  800 Baby Bullet
def parse_schedule_data(stops):
  _trips = {'weekday':{'north':OrderedDict(), 'south':OrderedDict()},
            'weekend':{'north':OrderedDict(), 'south':OrderedDict()}}
  _trips['weekend']['north'][421] = [None] * len(stops['north'])
  with open('CT-GTFS/stop_times.txt', 'rb') as timesFile:
    timesReader = csv.reader(timesFile)
    header = next(timesReader, None)
    trip_id_x = header.index('trip_id')
    stop_id_x = header.index('stop_id')
    departure_x = header.index('departure_time')
    for row in timesReader:
      if (len(row[trip_id_x]) > 4):
        continue # skip special trips
      trip_id = int(row[trip_id_x])
      stop_id = int(row[stop_id_x])
      hour = int(row[departure_x][0:-6])
      minute = int(row[departure_x][-5:-3])
      departure = str(hour * 60 + minute)
      direction = 'north' if (stop_id % 2 == 1) else 'south'
      schedule = 'weekday' if (trip_id < 400) else 'weekend'
      if (trip_id < 800 and trip_id > 500):
        continue # skip special trips
      if (trip_id not in _trips[schedule][direction]):
        _trips[schedule][direction][trip_id] = [None] * len(stops[direction])
      _trips[schedule][direction][trip_id][stops[direction].index(stop_id)] = departure
  return _trips

def write_schedule_data(trips, stops):
  with open('src/CaltrainServieData.java', 'w') as f:
    f.write("public class CaltrainServieData {\n")
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
        for trip_id in trips[schedule][direction]:
          f.write('},\n      {')
          f.write(','.join(map(xstr,[str(trip_id)] + trips[schedule][direction][trip_id])))
        f.write('}};\n')
    f.write('\n}\n')


if __name__ == "__main__":
    main()
