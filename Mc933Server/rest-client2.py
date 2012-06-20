#!/usr/bin/python
# -*- coding: utf-8 -*-

import requests
import sys

baseURI = 'http://mc933.lab.ic.unicamp.br:8017/onibus/'
headers = {'content-type': 'application/json'}
response = requests.get(baseURI, headers=headers)

if (response.headers.get('content-type', '') != 'application/json'):
    print 'A resposta não veio no formato JSON! :-('
    sys.exit(1)

answer = response.json

print '%d ônibus disponíveis:' % len(answer)

for elementKey in answer:
    print '* Lendo posição do ônibus:', elementKey
    response = requests.get(baseURI + str(answer[elementKey]), headers=headers)
    onibus = response.json
    print 'Placa %s: lat: %f, long: %f. Leitura: %s' % (onibus['licensePlate'], onibus['latitude'], onibus['longitude'], onibus['systemDatetime'])

