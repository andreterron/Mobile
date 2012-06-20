
import os
import xml.dom.minidom
from xml.dom.minidom import Node

os.chdir("I:\mc933\mc933")
doc = xml.dom.minidom.parse("bus_stops.kml")

for node in doc.getElementsByTagName("Placemark"):
    names= node.getElementsByTagName("name")
    print names[0].childNodes[0].data
    points = node.getElementsByTagName("Point")
    coord = points[0].getElementsByTagName("coordinates")
    print coord[0].childNodes[0].data
    #print coord

print "done"
