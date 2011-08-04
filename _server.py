#!/usr/bin/env python
# -*- coding: utf-8 -*-

import fapws._evwsgi as evwsgi
from fapws import base
import _make as m
from fapws.contrib import views

def start():
    evwsgi.start("127.0.0.1", "80")
    evwsgi.set_base_module(base)
    
    def make(environ, start_response):
        start_response('200 OK', [('Content-Type','text/html; charset=utf-8')])
        yield str(m.make())
    
    stylesheets = views.Staticfile('stylesheets')
    evwsgi.wsgi_cb(('/stylesheets', stylesheets))

    javascripts = views.Staticfile('javascripts')
    evwsgi.wsgi_cb(('/javascripts', javascripts))

    images = views.Staticfile('images')
    evwsgi.wsgi_cb(('/images', images))

    fonts = views.Staticfile('fonts')
    evwsgi.wsgi_cb(('/fonts', fonts))

    evwsgi.wsgi_cb(("/", make))
    evwsgi.set_debug(0)    
    evwsgi.run()
    

if __name__=="__main__":
    start()
