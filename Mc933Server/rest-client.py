#!/usr/bin/python

import csv
import simplejson as json
from restful_lib import Connection
#conn = Connection("http://mc933.lab.ic.unicamp.br:8010")
conn = Connection("http://mc933.lab.ic.unicamp.br:8010/getBusesPositions")
#response = conn.request_get("/getPosition")
response = conn.request_get("")

buses = json.loads(response["body"])

for i in buses:
    response = conn.request_get(str(i))
    obj = json.loads(response["body"])
    writer = csv.writer(open('points.csv', 'ab')) #, delimiter=' ', quotechar='|', quoting=csv.QUOTE_MINIMAL
    #writer.writerow(['Horario', 'Latitude', 'Longitude'])
    writer.writerow([datetime.strftime(datetime.now(), "%d/%m/%Y %H:%M:%S"), str(lat), str(lon)])
    return "Point (" + str(lat) + ',' + str(lon) + ") saved at " + datetime.strftime(datetime.now(), "%d/%m/%Y %H:%M:%S")
    #print response["body"] + "\n"
#coordenada = json.loads(response["body"])

conn.request_put("/sidewinder", {'color': 'blue'}, headers={'content-type':'application/json', 'accept':'application/json'})
