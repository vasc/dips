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

def main(simulator):
	results = {}

	prefix = 'average:%s:' % simulator 
	keys = ['variance']

	host = 'localhost'

	r = redis.Redis(host=host, port=6379, db=0)
	
	for key in keys:
		list_size = r.llen(prefix+key)
		results[key] = list(reversed(r.lrange(prefix+key, 0, list_size)))
	
	results['test.type'] = 'aptitude.variance'
	results['simulation.name'] = 'average'

	results['simulator'] = simulator

	filename = ('results/' +
				results['test.type'] + '.' +
				results['simulation.name'] + '.' +
				simulator + '.' +
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
    main(sys.argv[1])