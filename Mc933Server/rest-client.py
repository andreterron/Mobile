#!/usr/bin/python

import time
import csv
import simplejson as json
from restful_lib import Connection
#conn = Connection("http://mc933.lab.ic.unicamp.br:8010")
while 1:
    conn = Connection("http://mc933.lab.ic.unicamp.br:8010/getBusesPositions")
    #response = conn.request_get("/getPosition")
    response = conn.request_get("")
    
    buses = json.loads(response["body"])
    try:
        f = open('positions.csv')
        read = csv.reader(f)
                            
                #for row in read:
                #    txt+= str(row) + "</br>"
                #return txt)
        # 0 - systemDatetime
        # 1 - moduleDatetime
        # 2 - licensePlate
        # 3 - latitude
        # 4 - longitude
        # 5 - altitude
        # 6 - speed
        # 7 - ignition
        for bus in buses:
            bus['found'] = False
            for row in read:
                if row[0] == bus['systemDatetime'] and row[2] == bus['licensePlate']:
                    bus['found'] = True
                    break;
        
        f.close()
    except:
        for bus in buses:
            bus['found'] = False
        
    
    f = open('positions.csv', 'ab')
    writer = csv.writer(f)
    for b in buses:
        if not b['found']:
            writer.writerow([b['systemDatetime'],
                             b['moduleDatetime'],
                             b['licensePlate'],
                             b['latitude'],
                             b['longitude'],
                             b['altitude'],
                             b['speed'],
                             b['ignition']])
            print "New point added\n"
        else:
            print "This point already exists\n"
    f.close()
    time.sleep(62)


conn.request_put("/sidewinder", {'color': 'blue'}, headers={'content-type':'application/json', 'accept':'application/json'})
