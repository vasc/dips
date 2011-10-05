#!/usr/bin/python

import sys
import simplejson
import redis
import os.path
import yaml

def sum_key(i, l):
	values = [l(x) for x in i]
	return sum(values)

def avg(i, l):
	total = sum_key(i, l)
	return 1.0*total/len(i)

def main():
	simulations = []
	results = {}

	prefix = 'infection:performance:'
	keys = ['processed.events',
			'processed.events.local',
			'processed.events.remote',
			'initial.time',
			'final.time',
			'idle.time',
			'average.delay',
			'average.delay.remote',
			'average.delay.local',
			'nodes.count',
			'bundle.size',
			'routing.method',
			'infection.degree'
	]

	while True:
		host = sys.stdin.readline().strip('\n')
		if not host: break

		simulation = {}
		r = redis.Redis(host=host, port=6379, db=0)
		
		for key in keys:
			simulation[key] = r.get(prefix+key)

		simulation['duration'] = long(simulation['final.time']) - long(simulation['initial.time'])
		simulation['events.per.second'] = int(simulation['processed.events'])/(simulation['duration']/1000.0)
		
		simulations.append(simulation)

	
	results['processed.events.total'] = sum_key(simulations, lambda x: int(x['processed.events']))
	results['duration.total'] = max(long(x['final.time']) for x in simulations) - min(long(x['initial.time']) for x in simulations)
	results['processed.events.per.second.total'] = results['processed.events.total'] / (results['duration.total']/1000.0)
	results['processed.events.per.second.per.instance'] = avg(simulations, lambda x: x['events.per.second'])
	results['average.delay.global'] = avg(simulations, lambda x: float(x['average.delay']))
	results['average.delay.local'] = avg(simulations, lambda x: float(x['average.delay.local']))
	results['average.delay.remote'] = avg(simulations, lambda x: float(x['average.delay.remote']))
	results['idle.time.total'] = sum_key(simulations, lambda x: long(x['idle.time']))
	results['idle.time.per.instance'] = avg(simulations, lambda x: long(x['idle.time']))
	
	results['network.size'] = sum_key(simulations, lambda x: int(x['nodes.count']))
	results['routing.method'] = simulations[0]['routing.method']
	results['bundle.size'] = int(simulations[0]['bundle.size'])
	results['test.type'] = 'performance'
	results['simulation.name'] = 'infection'
	results['instance.count'] = len(simulations)
	results['infection.degree'] = simulations[0]['infection.degree']

	results['instances'] = simulations

	filename = ('results/' +
				results['test.type'] + '.' +
				results['simulation.name'] + '.' +
				results['infection.degree'] + '.' + 'degree' +
				("%07d" % results['network.size']) + '.nodes.' +
				("%06d" % (results['processed.events.total']/results['instance.count'])) + '.events' +
				("%05d" % results['bundle.size']) + '.bundle.' +
				results['routing.method'] + '.' +
				("%s" % results['instance.count']) + '.instances.' +
				'json')

	if os.path.exists(filename):
		with open(filename) as f:
			experiences = simplejson.loads(f.read())
	else:
		experiences = []

	experiences.append(results)

	with open(filename, 'w') as f:
		f.write(simplejson.dumps(experiences))

	with open(filename[:-4]+'yaml', 'w') as f:
		f.write(yaml.dump(experiences))

	
	#print simplejson.dumps(simulations)
	#print simplejson.dumps(results)


if __name__ == '__main__':
	main()