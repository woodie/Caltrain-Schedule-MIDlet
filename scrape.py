#!/usr/bin/env python

import csv
import os
import subprocess
from bs4 import BeautifulSoup

def main():
  fetch_schedule_data()
  parse_schedule_data('weekday','north')
  parse_schedule_data('weekday','south')
  parse_schedule_data('weekend','north')
  parse_schedule_data('weekend','south')

def fetch_schedule_data():
  weekday_url = 'http://www.caltrain.com/schedules/weekdaytimetable.html'
  weekend_url = 'http://www.caltrain.com/schedules/weekend-timetable/Weekend_Timetable_-_Effective_April_1__2019.html'
  basedir = os.getcwd()
  subprocess.call(['mkdir', '-p', 'data'])
  os.chdir('data')
  subprocess.call(['curl', '-o', 'weekday.htm', weekday_url])
  subprocess.call(['curl', '-o', 'weekend.htm', weekend_url])
  os.chdir(basedir)

def parse_schedule_data(schedule, direction):
  with open('data/%s.htm' % schedule) as f:
    soup = BeautifulSoup(f, 'html.parser')
  tbl = soup.select_one("table.%sB_TT" % direction[0].upper())
  header = ['']
  for tr in tbl.select('tr'):
    valid = tr.select('th')
    if len(valid) > 9:
      for th in  tr.select('th'):
        train_id = th.text.replace("*","")
        if len(train_id) < 4:
          header.append(train_id)
  rows = []
  for tr in tbl.select('tr'):
    valid = tr.select('th')
    if len(valid) < 2:
      continue
    zone = valid[0].text.strip()
    if len(zone) == 1: # busses don't have a zone
      row = []
      row.append(_parse_stop(valid[1].text))
      times = tr.select('th')
      for td in tr.select('td'):
        row.append(_parse_time(td.text))
      rows.append(row[0:len(header)])
  with open('data/%s_%s.csv' % (schedule, direction), mode='w') as out_file:
    csv_writer = csv.writer(out_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    csv_writer.writerow(header)
    for row in rows:
      csv_writer.writerow(row)

def _parse_stop(text):
  text = text.replace('Departs ', '').replace('Arrives ', '')
  text = text.replace("So. San", "So San")
  text = text.replace("SJ D", "San Jose D")
  return text.replace(u'\xa0', u' ')

def _parse_time(text):
  if ':' not in text:
    return ''
  h, m = text.strip().split(':')
  if 'p' in m and int(h) < 12:
    h = str(int(h) + 12)
  elif 'a' in m and int(h) == 12:
    h = str(int(h) + 12)
  elif 'a' in m and int(h) < 3:
    h = str(int(h) + 24)
  if len(h) < 2:
    h = ('0%s' % h)
  return '%s:%s:00' % (h, m[0:2])


if __name__ == "__main__":
    main()
