import sys
import glob
import simplejson
import itertools
import re
import random
import yaml
import argparse as a

def groupby(values, keys):
	if len(keys) == 0:
		return list(values)
	else:
		r = {}
		key = keys[0]
		l = lambda x: x[key]
		svalues = sorted(values, key=l)
		for key_val, gvalues in itertools.groupby(svalues, l):
			r[str(key_val)] = groupby(gvalues, keys[1:])
		return r

def load_results(keys):
	values = []
	for j in glob.glob("results/*.json"):
		with open(j) as f:
			r = simplejson.loads(f.read())
			values.extend(r)
	
	return hierark = groupby(values, keys)





def main(instance_count, instance_type, list):
	hierark = load_results([ 'instance.type', 
								'instance.count',
								'routing.method',
								'simulation.name',
								'infection.degree',
								'network.size', 
								'bundle.size'])

	
	hierark = hierark[instance_type]
	hierark = hierark[str(instance_count)]
	hierark = hierark['round.robin']

	sims = {}
	for sim in glob.glob("simulations/*.sim"):
		values = hierark
		sims[sim] = 0
		m = re.match(r"simulations/(\w+)\.(\d+)\.(\d+)\.nodes\.(\d+)\.bundle\.sim", sim)

		groups = []
		groups.append(m.group(1))
		groups.append(str(int(m.group(2))))
		groups.append(str(int(m.group(3))))
		groups.append(str(int(m.group(4))))

		if m.group(1) in values:
			values = values[m.group(1)]
			if str(int(m.group(2))) in values:
				values = values[str(int(m.group(2)))]
				if str(int(m.group(3))) in values:
					values = values[str(int(m.group(3)))]
					if str(int(m.group(4))) in values:
						values = values[str(int(m.group(4)))]

						sims[sim] = len(values)
	
	ssims = sorted(sims.keys(), key=lambda x: sims[x])

	if list:
		for sim in reversed(ssims): print sim, sims[sim]
		print

		print "%.2f%% coverage" % (100.0 * sum(1 for sim in sims.keys() if sims[sim] > 0) / len(sims.keys()))

	else:
		lowest = min(sims.values())
		possible = [sim for sim in sims.keys() if sims[sim] == lowest]

		print random.choice(possible)

	return



if __name__ == "__main__":
	ap = a.ArgumentParser(description="get next simulation")
	ap.add_argument('-l', action='store_true')
	ap.add_argument('-t', default='m1.small')
	ap.add_argument('instance_count', type=int)

	arguments = ap.parse_args()

	main(arguments.instance_count, arguments.t, arguments.l)