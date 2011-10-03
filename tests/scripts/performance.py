import sys
import simplejson
import redis

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
			'average.delay.local'
	]

	while True:
		host = sys.stdin.readline()
		if not host: break

		simulation = {}
		r = redis.Redis(host=host.strip('\n'), port=6379, db=0)
		
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

	print simplejson.dumps(simulations)
	print simplejson.dumps(results)


if __name__ == '__main__':
	main()