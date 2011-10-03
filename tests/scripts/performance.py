import sys
import simplejson
import redis

def main():
	simulations = []

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
		simulations.append(simulation)
	print simplejson.dumps(simulations)


if __name__ == '__main__':
	main()