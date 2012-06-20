#!/usr/bin/python
import cherrypy

class Resource(object):

    def __init__(self, content):
        self.content = content

    exposed = True

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
    def test(self,x,y):
        berlin=geo.xyz(52.518611,13.408056)
        munich=geo.xyz(48.137222,11.575556)
        txt=""
        return "Hello there TEst"

        #return "Dist:" + str(geo.distance(berlin,munich))
        #return str(x)+"----"+str(y)

    @cherrypy.expose
    def index(self):
        return "Hellow World!"


class ResourceIndex(Resource):
    def to_html(self):
        html_item = lambda (name,value): '<div><a href="{value}">{name}</a></div>'.format(**vars)
        items = map(html_item, self.content.items())
        items = ''.join(items)
        return '<html>{items}</html>'.format(**vars)

class Root(object):
    pass

root = Root()

root.sidewinder = Resource({'color': 'red', 'weight': 176, 'type': 'stable'})
root.teebird = Resource({'color': 'green', 'weight': 173, 'type': 'overstable'})
root.blowfly = Resource({'color': 'purple', 'weight': 169, 'type': 'putter'})
root.resource_index = ResourceIndex({'sidewinder': 'sidewinder', 'teebird': 'teebird', 'blowfly': 'blowfly'})

conf = {
    'global': {
        'server.socket_host': '0.0.0.0',
        'server.socket_port': 8000,
    },
    '/': {
        'request.dispatch': cherrypy.dispatch.MethodDispatcher(),
    }
}

cherrypy.quickstart(root, '/', conf)
