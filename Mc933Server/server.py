#!/usr/bin/python
# -*- coding: cp1252 -*-

#AULA 2 Servidor
    #1) Localizacao dos pontos de onibus mais proximos
    #2) Indicação dos próximos ônibus e previsão de chegada dado um ponto
    #3) Opcional> Retornar a listagem de pontos de onibus em uma rota
    #Dica distancia geodesica

import cherrypy
import csv
import geo
import sys
from datetime import datetime, timedelta
import xml.dom.minidom
from xml.dom.minidom import Node
from operator import itemgetter
import os
import simplejson as json
from restful_lib import Connection
#from __future__ import division


class Resource(object):
    
    # Usar este prefixo para retornar um dicionario na forma de JSON
    @cherrypy.tools.json_out()
    def GET(self):
        return self.content

    @cherrypy.tools.json_in()
    def PUT(self, **args):
        print args
        print cherrypy.request.body.read()
        return 'PUT'

    def POST(self):
        print 'post'

    def delete(self):
        print 'delete'
    
    
    
    @cherrypy.expose
    def viewSavedPoints(self):
        txt=""
        reader = csv.reader(open('points.csv'))
        for row in reader:
            txt+= str(row) + "</br>"
        return txt
    
    
    @cherrypy.expose
    def viewSavedPositions(self):
        txt="[systemDatetime, moduleDatetime, licensePlate, latitude, longitude, altitude, speed, ignition]<br/>"
        try:
            reader = csv.reader(open('positions.csv'))
        except:
            return txt
        for row in reader:
            txt+= str(row) + "</br>"
        return txt
    
    @cherrypy.expose
    def savePoint(self, lat=None, lon=None):
        if (lat == None or lon == None):
            return "Erro - Latitude ou Longitude não foram especificadas"
        writer = csv.writer(open('points.csv', 'ab')) #, delimiter=' ', quotechar='|', quoting=csv.QUOTE_MINIMAL
        #writer.writerow(['Horario', 'Latitude', 'Longitude'])
        writer.writerow([datetime.strftime(datetime.now(), "%d/%m/%Y %H:%M:%S"), str(lat), str(lon)])
        return "Point (" + str(lat) + ',' + str(lon) + ") saved at " + datetime.strftime(datetime.now(), "%d/%m/%Y %H:%M:%S")
    
    
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getBusesPositions(self):
        lcord = []
        conn = Connection("http://mc933.lab.ic.unicamp.br:8017/onibus")
        response = conn.request_get("")
        
        buses = json.loads(response["body"])
        
        for i in buses:
            response = conn.request_get(str(i))
            lcord.append(json.loads(response["body"]))
        #conn.request_put("/sidewinder", {'color': 'blue'}, headers={'content-type':'application/json', 'accept':'application/json'})
        return lcord
    
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getPosition(self):
        dados = list(csv.reader(open('circular1.csv')))
        #w = ["Segunda", "Terca", "Quarta", "Quinta", "Sexta", "Sabado", "Domingo"]
        now = datetime.now() #datetime(2012, 5, 29, 15, 0, 0);
        txt = ""
        #txt += now.strftime("%d/%m/%Y %H:%M:%S ")
        #txt += w[now.weekday()] + "<br/><br/>\n"
        a = len(dados) - 1
        b = 0
        while abs(a - b) > 1:
            i = int((a + b) / 2)
            d = datetime.strptime(dados[i][0], "%d/%m/%Y %H:%M:%S")
            if d.day != 18:
                a = i
                continue
            if d.time() <= now.time():
                a = i
            else:
                b = i
        print (dados[a][0],dados[b][0])
        ta = datetime.strptime(dados[a][0], "%d/%m/%Y %H:%M:%S").replace(year=now.year, month=now.month, day=now.day)
        tb = datetime.strptime(dados[b][0], "%d/%m/%Y %H:%M:%S").replace(year=now.year, month=now.month, day=now.day)
        if (abs(ta - now) <= abs(tb - now)):
            txt += dados[a][2] + "," + dados[a][3]
            return [{'lat' : dados[a][3], 'lon' : dados[a][2]}]
        else:
            txt += dados[b][2] + "," + dados[b][3]
            return [{'lat' : dados[b][3], 'lon' : dados[b][2]}]
            
        # return txt
    
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getStopPosition(self, stopid=None):
        #dados = list(csv.reader(open('circular1.csv')))
            
        #__location__ = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(sys.argv[0])))


        if(stopid == None):
            return {};
        
        doc = xml.dom.minidom.parse("bus_stops.kml")

        for node in doc.getElementsByTagName("Placemark"):
            name = node.getElementsByTagName("name")[0].childNodes[0].data
            sid = node.getElementsByTagName("stopid")            
            if ((sid.length > 0 and sid[0].childNodes[0].data == stopid) or stopid == name):
                points = node.getElementsByTagName("Point")
                coord = points[0].getElementsByTagName("coordinates")
                lon = float(coord[0].childNodes[0].data.split(",")[0])
                lat = float(coord[0].childNodes[0].data.split(",")[1])
                return [{'name': name, 'lat': lat, 'lon':lon}]
        return [{'name': '', 'lat': 0.0, 'lon': 0.0}]

    #localhost:8888/getNearestBusStops?lat=;lon=
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getNearestBusStops(self, lat=None, lon=None, radius=1000, limit=10, line=None, via=None):
        #dados = list(csv.reader(open('circular1.csv')))
            
        #__location__ = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(sys.argv[0])))

        if line != None and via != None:
            possible = self.listLinePoints(line, via)
        else:
            possible = None

        if(lat == None or lon == None):
            return "Missing Lat or Lon"
                    
        source = geo.xyz(float(lat), float(lon))
        txt = str(limit) + " Closest Bus Stops in a " + str(radius) + "m radius: <br/>"
        ldist = []
        doc = xml.dom.minidom.parse("bus_stops.kml")

        for node in doc.getElementsByTagName("Placemark"):
            name = node.getElementsByTagName("name")[0].childNodes[0].data
            sid = node.getElementsByTagName("stopid")[0].childNodes[0].data
            points = node.getElementsByTagName("Point")
            coord = points[0].getElementsByTagName("coordinates")
            lon = float(coord[0].childNodes[0].data.split(",")[0])
            lat = float(coord[0].childNodes[0].data.split(",")[1])

            point = geo.xyz(lat, lon)
            dist = float('%.2f' % geo.distance(source, point))
            if(float(dist) < float(radius) and (possible == None or possible.index(sid) != -1)):
                ldist.append({
                              'dist':float(dist),
                              'name':name,
                              'lat':lat,
                              'lon':lon,
                              'sid':sid
                              })
           
       
        if(ldist == []):
            return self.getNearestBusStops(lat, lon, radius + 100, limit)
       
        return sorted(ldist, key=itemgetter('dist'))[0:int(limit)]
    
    


    @cherrypy.expose
    @cherrypy.tools.json_out()
    def listLinePoints(self, line=0, via=0, disabled="false"):
        txt = ""
        lp = []
        doc = xml.dom.minidom.parse("buslines.xml")

        for node in doc.getElementsByTagName("line"):
            
            lineId = node.getElementsByTagName("id")[0].childNodes[0].data
            if(int(lineId) == int(line)):       
                txt += str(node.getElementsByTagName("name")[0].childNodes[0].data) + "<br/>"
                times = node.getElementsByTagName("times")[0]
                runs = times.getElementsByTagName("run")
                for run in runs:
                    subid = run.getElementsByTagName("via")[0].getElementsByTagName("subid")[0].childNodes[0].data
                    dis = run.getElementsByTagName("disabled")[0].childNodes[0].data
                    if(int(via) == int(subid) and str(disabled) == str(dis)):
                        #print "runid:" + str(run.getElementsByTagName("id")[0].childNodes[0].data)
                        break
                stops = run.getElementsByTagName("stop")
                i=0
                for stop in stops:
                    i+=1
                    lp.append({'point'+str(i): stop.getElementsByTagName("point")[0].childNodes[0].data})
       
        return lp
    
    
    @cherrypy.expose
    @cherrypy.tools.json_out() 
    def listRun(self, circular="Circular 1", run=10, runpos='null'):
        lp = []
        doc = xml.dom.minidom.parse("buslines.xml")

        for node in doc.getElementsByTagName("line"):
            lineName = node.getElementsByTagName("name")[0].childNodes[0].data
            if(str(lineName) == str(circular)):
                times = node.getElementsByTagName("times")[0]
                runs = times.getElementsByTagName("run")
                for r_run in runs:
                    run_id = r_run.getElementsByTagName("id")[0].childNodes[0].data
                    if(int(run_id) == int(run)):
                        stops = r_run.getElementsByTagName("stop")                        
                        if(runpos != 'null'):
                            stop = stops[int(runpos)]
                            return{
                                       'pid':  stop.getElementsByTagName("point")[0].childNodes[0].data,
                                       'time': stop.getElementsByTagName("time")[0].childNodes[0].data
                                     }
                            
                        for stop in stops:
                            stopid = stop.getElementsByTagName("point")[0].childNodes[0].data
                            time = stop.getElementsByTagName("time")[0].childNodes[0].data
                            lp.append({
                                       'pid': stopid,
                                       'time':time
                                       })   
                           
        return lp
                            
    
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def routeClosestPoint(self, lat=None, lon=None, line=0, via=0):
        try:
            doc = xml.dom.minidom.parse("Linha" + (line + 1) + ".kml")
        except:
            return []
        
        source = geo.xyz(float(lat), float(lon))
        md = -1
        point = None
        for node in doc.getElementsByTagName("coordinates"):
            pts = node[0].childNodes[0].data.split(" ")
            for p in pts:
                p = p.split(',')
                pt = geo.xyz(float(p[0]), float(p[1]))
                d = geo.distance(source, pt)
                if md == -1 or d < md:
                    point = {'lat': p[0], 'lon': p[1], 'dist': d}
                    md = d
        return point
                
        
             
    #coordinates            
    @cherrypy.expose
    @cherrypy.tools.json_out() 
    def getBusPath(self, line=0, via=0, start=-1, end=-1):
        try:
            doc = xml.dom.minidom.parse("Linha1.kml")
        except:
            return []
        
        linepts = self.listLinePoints(line, via)
        sp = ep = -1 # Point IDs
        sd = ed = -1 # Distancias
        start_pt = end_pt = None
        if start != -1:
            if linepts.index(start) == -1: start = -1
            else:
                pt = self.getStopPosition(start)
                start_pt = geo.xyz(float(pt['lat']), float(pt['lon']))
        if end != -1:
            if linepts.index(end) == -1: end = -1
            else:
                pt = self.getStopPosition(end)
                end_pt = geo.xyz(float(pt['lat']), float(pt['lon']))
        
        plist = []
        for node in doc.getElementsByTagName("coordinates"):
            pts = node.childNodes[0].data.split()
            for i in range(0, len(pts)):
                #plist.append(pts[i])
                #continue
                p = pts[i].split(',')
                pt = geo.xyz(float(p[0]), float(p[1]))
                plist.append({'lat': p[0], 'lon': p[1]})
                if start_pt != None:
                    d = geo.distance(start_pt, pt)
                    if sd == -1 or d < sd:
                        #point = {'lat': p[0], 'lon': p[1], 'dist': sd}
                        sd = d
                        sp = i
                if end_pt != None:
                    d = geo.distance(end_pt, pt)
                    if ed == -1 or d < ed:
                        #point = {'lat': p[0], 'lon': p[1], 'dist': ed}
                        ed = d
                        ep = i
        if (ep == -1 or sp == -1):
            return plist
        elif ep < sp:
            return plist[sp:] + plist[:ep]
        else:
            return plist[sp:ep]
    
    @cherrypy.expose
    def getFullRoute(self, line=0, via=0):
        points = self.listLinePoints(line, via)
        # http://maps.google.com/maps?f=d&hl=en&saddr=-22.819325,-47.065709&daddr=-22.8137976,-47.069422+to:-22.8137471,-47.0682285&ie=UTF8&0&dirflg=w&cad=tm:d&mra=atm
        urlstr = "http://maps.google.com/maps?f=d&hl=pt-br"
        j = 0
        for i in points[0:5]:
            pt = self.getStopPosition(i)
            if pt.lat == 0 and pt.lon == 0:
                continue
            if j == 0:
                urlstr += "&saddr=" + str(pt['lat']) + "," + str(pt['lon'])
            elif j == 1:
                urlstr += "&daddr=" + str(pt['lat']) + "," + str(pt['lon'])
            else:
                urlstr += "+to:" + str(pt['lat']) + "," + str(pt['lon'])
            j += 1
        urlstr += "&ie=UTF8&0&dirflg=w&cad=tm:d&mra=atm&output=kml"
        return urlstr

    
    

    @cherrypy.expose
    @cherrypy.tools.json_out() 
    #s_lat=-22.818342&s_lon=-47.059597&d_lat=-22.824192&d_lon=-47.067738 
    def Point2Point(self, s_lat, s_lon, d_lat, d_lon, radius=500, limit=10, time='',maxres=5):
        
        near_source = self.getNearestBusStops(s_lat, s_lon, radius, limit)
        near_dest = self.getNearestBusStops(d_lat, d_lon, radius, limit)
        
        
        ld = []
        for p_source in near_source:

            for p_dest in near_dest:
              if (time == ""):
                        dtime = datetime.now()
              else:
                 dtime = datetime.strptime(time, "%H:%M:%S")                                      

              ret = self.Stop2Stop(p_source['sid'], p_dest['sid'], time)
     
              if(ret != {}):           
                
                arrival = ret['dest']['time']
                ftime = datetime.strptime(arrival, "%H:%M:%S")
                ftime = ftime + timedelta(seconds=p_dest['dist'] / 2)
                ftime = datetime.strftime(ftime, "%H:%M:%S")
                
                t_departure = datetime.strptime(ret['source']['time'], "%H:%M:%S")
                
                t_departure = t_departure.replace(dtime.year,dtime.month,dtime.day)
                delta = t_departure - dtime
                                #nsecs = delta.total_seconds()
                nsecs = (delta.microseconds + (delta.seconds + delta.days*24*3600)*10**6)/10**6               
                speed = p_source['dist'] / nsecs
                
           
                if(speed > 7):
                    action = "___YOU WONT MAKE IT"
                elif(speed > 3):
                    action = "__RUN!"
                elif(speed > 2):
                    action = "_WALK FAST"
                else:
                    action = "WALK"                   
                
                
                ld.append({
                           'start_time': datetime.strftime(dtime,"%H:%M:%S"),
                           'departure': ret['source']['time'],
                           'arrival':ret['dest']['time'],
                           'source': p_source['sid'],
                           'dest': p_dest['sid'],
                           'circular': ret['dest']['circular'],
                           'dist_source': p_source['dist'],
                           'dist_dest': p_dest['dist'],
                           'final_time': ftime,
                           'action': action,
                           'time': nsecs
                            })  
        
        ld.sort(key = itemgetter('action'))                     
        

        return sorted(ld, key=itemgetter('final_time'))[0:int(maxres)]
    
    @cherrypy.expose
    @cherrypy.tools.json_out()    
    def Stop2Stop(self, source, dest, time=''):
        source_bus = self.getNextBus(source, time)
        dest_bus = self.getNextBus(dest, time)   
        
        etime=''
        stime='' 
        
        for busD in dest_bus:
            for busS in source_bus:
                if(busS["time"] < busD["time"] and busS["line"] == busD["line"] and busS["run"] <= busD["run"]):
                    start_bus = self.getNextBus("1",time,line=busS['line'],via=busS['via'])
                    end_bus = self.getNextBus("26",time,line=busS['line'],via=busS['via'])
                  
                    for ebus in end_bus:
                        if (ebus['run']==busS['run']):
                            etime = ebus['time']
                            break
                    for sbus in start_bus:
                        if(sbus['run']==busD['run']):
                            stime = sbus['time']
                            break                     
                        
                    if(etime<stime):
                        return {
                                'source':busS,
                                'dest':busD
                                }
           
            
        
        return {}

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getLineInfo(self, line=0, via=0):
        info = [{   'name' : 'Circular 1',
                    'via' : [{'name': ''}]
                 },
                {   'name': 'Circular 2',
                    'via' : [{'name': ''}, {'name': 'FEC'}, {'name': 'Museu'}]
                }]
        linfo = {}
        if not (line < len(info) and line >= 0):
            line = 0
        if (via < len(info[line]['via']) and via >= 0):
            via = 0
        linfo['name'] = info[line]['name']
        linfo['via'] = info[line]['via'][via]
        return linfo 
    
    
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getLineVia(self, lname='Circular 1', vname = ''):
        lines = ['Circular 1', 'Circular 2']
        vias = [[''], ['', 'FEC', 'Museu']]
        
        if lname in lines:
            line = lines.index(lname)
        else:
            line = 1
        
        if vname in vias:
            via = vias.index(vname)
        else:
            line = 0
            via = 0
        return {'line': line, 'via': via}


    @cherrypy.expose
    @cherrypy.tools.json_out()
    def getNextBus(self, stopid, time='', past='false', line=None, via=None, limit=10):        
        
        if(line == None):
            tables = [['table_circular1.csv', 0], ['table_circular2.csv', 1]]
        elif(line == 0):
            tables = [['table_circular1.csv', 0]]
        elif(line == 1):
            tables = [['table_circular2.csv', 1]]
              

        buses = []
        txt = ""
        if (past == 'true'): m = -1
        else: m = 1
        if (time == ""):
            time = datetime.now()
        else:
            time = datetime.strptime(time, "%H:%M:%S")
        for fname in tables:
            dados = list(csv.reader(open(fname[0])))
            dh = len(dados)
            for i in range(dh):
                dados[i] = dados[i][0].split(';')
            pt_list = dados[0]
            try:
                x = pt_list.index(stopid)
            except ValueError:
                continue
            for i in range(2, dh):
                if dados[i][x] == '-': continue
                t = datetime.strptime(dados[i][x], "%H:%M").replace(year=time.year, month=time.month, day=time.day)
                #if (m*(t - time) < (t - t)):
                #    t = t.replace(day = t.day + m)
                bline = fname[1]
                bvia = self.getLineVia(self.getLineInfo(bline)['name'], dados[i][1])
                if (m * (t - time) > (t - t)):
                    buses.append([abs(t - time), t, bline, bvia, i - 2])
        buses.sort()
        lret = []
        l = len(buses)
        for i in range(0, limit):
            if i >= l:
                break
            #txt += datetime.strftime(buses[i][1], "%H:%M:%S") + " - " + buses[i][2] + " - run id = " + str(buses[i][3]) + "<br/>"
            lret.append({
                         'time': datetime.strftime(buses[i][1], "%H:%M:%S"),
                         'line' : buses[i][2],
                         'via' : buses[i][3],
                         'circular' : self.getLineInfo(buses[i][2])['name'],
                         'run' : buses[i][4]
                         })
        
        return lret

    @cherrypy.expose
    def test(self, x, y):
        #berlin = geo.xyz(52.518611, 13.408056)
        #munich = geo.xyz(48.137222, 11.575556)
        #txt = ""
        return "Hello there TEst"

        #return "Dist:" + str(geo.distance(berlin,munich))
        #return str(x)+"----"+str(y)

    @cherrypy.expose
    def index(self):
        return "Hellow World!"

    @cherrypy.expose
    def showMap(self):
        point = self.getPosition().split(",")
        txt = """<!DOCTYPE html>

    
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
</style>
<script type="text/javascript"
    src="https://maps.google.com/maps/api/js?sensor=true">
</script>
<script type="text/javascript">
  var map;
function initialize() {
  var myLatlng = new google.maps.LatLng(-22.819008,-47.067726);
  var busLatlng = new google.maps.LatLng(""" + point[1] + ", " + point[0] + """);
  var myOptions = {
    zoom: 14,
    center: myLatlng,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  }
  map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  
  var marker = new google.maps.Marker({
      position: busLatlng, 
      map: map,
      title:"Onibus Circular 1"
  });
  google.maps.event.addListener(marker, 'click', function() {
    map.setCenter(busLatlng);
  });
}
  
function moveToDarwin() {
  var darwin = new google.maps.LatLng(-12.461334, 130.841904);
  map.setCenter(darwin);
}

</script>
</head>
<body onload="initialize()">
  <div id="map_canvas" style="width:100%; height:100%"></div>
</body>
</html>"""
        return txt

    

class Root(object):
    pass

root = Root()
root.resource = Resource()
    
conf = {
    'global': {
        'server.socket_host': 'mc933.lab.ic.unicamp.br', # '127.0.0.1', # 
        'server.socket_port': 8010,
    }
}

os.system("python rest-client.py")

cherrypy.quickstart(Resource(), '', conf)

#cherrypy.config.update({'server.socket_port'     : 8110})

#cherrypy.quickstart(Resource())

#,
    #'/': {
    #    'request.dispatch': cherrypy.dispatch.MethodDispatcher(),
    #}



