#!/usr/bin/env python

import csv
import os
import subprocess
from collections import OrderedDict 

xstr = lambda s: s or ''

def main():
  fetch_schedule_data()
  stations = parse_station_data()
  trips = parse_schedule_data(stations)
  write_schedule_file('north', 'weekday', trips, stations)
  write_schedule_file('south', 'weekday', trips, stations)
  write_schedule_file('north', 'weekend', trips, stations)
  write_schedule_file('south', 'weekend', trips, stations)

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
  _stations = {'north':[], 'south':[], 'labels':{}}
  extra = ['Diridon', 'Caltrain', 'Station']
  with open('CT-GTFS/stops.txt', 'rb') as stopsFile:
    stopsReader = csv.reader(stopsFile)
    header = next(stopsReader, None)
    stop_id_x = header.index('stop_id')
    stop_name_x = header.index('stop_name')
    for row in stopsReader:
      stop_id = int(row[stop_id_x])
      stop_name = ' '.join(i for i in row[stop_name_x].split() if i not in extra)
      _stations['labels'][stop_id] = stop_name
      if (stop_id % 2 == 1):
        _stations['north'].insert(0, stop_id)
      else:
        _stations['south'].append(stop_id)
  return _stations

def parse_schedule_data(stations):
  _trips = {'weekday':{'north':OrderedDict(), 'south':OrderedDict()},
            'weekend':{'north':OrderedDict(), 'south':OrderedDict()}}
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
      # TODO: Weekend is 400 and  800 range (not 500)
      schedule = 'weekday' if (trip_id < 400) else 'weekend'
      if (trip_id not in _trips[schedule][direction]):
        _trips[schedule][direction][trip_id] = [None] * len(stations[direction])
      _trips[schedule][direction][trip_id][stations[direction].index(stop_id)] = departure
  return _trips

def write_schedule_file(direction, schedule, trips, stations):
  with open('res/caltrain_%s_%s.txt' % (direction, schedule), 'w') as f:
    header = ['Train No.']
    for stop_id in stations[direction]:
      header.append(stations['labels'][stop_id])
    f.write('\t'.join(header))
    f.write('\n')
    for trip_id in trips[schedule][direction]:
      f.write('\t'.join(map(xstr,[str(trip_id)] + trips[schedule][direction][trip_id])))
      f.write('\n')


if __name__ == "__main__":
    main()
