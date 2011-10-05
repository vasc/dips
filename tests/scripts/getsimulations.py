import sys
import glob
import simplejson
import itertools
import re
import random

def main(i):
	values = []
	for j in glob.glob("results/*.json"):
		with open(j) as f:
			r = simplejson.loads(f.read())
			values.extend(r)
	
	hierark = {}

	l = lambda x: x['simulation.name']
	g = sorted(values, key=l)
	for simname, g in itertools.groupby(g, l):
		hierark[simname] = {}
		l = lambda x: x['infection.degree']
		g = sorted(g, key=l)
		for degree, g in itertools.groupby(g, l):
			hierark[simname][degree] = {}
			l = lambda x: x['network.size']
			g = sorted(g, key=l)
			for networksize, g in itertools.groupby(g, l):
				networksize=str(networksize)
				hierark[simname][degree][networksize] = {}
				l = lambda x: x['instances'][0]['processed.events']
				g = sorted(g, key=l)
				for processedevents, g in itertools.groupby(g, l):
					processedevents=str(processedevents)
					hierark[simname][degree][networksize][processedevents] = {}
					l = lambda x: x['bundle.size']
					g = sorted(g, key=l)
					for bundle, g in itertools.groupby(g, l):
						bundle=str(bundle)
						hierark[simname][degree][networksize][processedevents][bundle] = {}
						l = lambda x: x['routing.method']
						g = sorted(g, key=l)
						for routing, g in itertools.groupby(g, l):
							hierark[simname][degree][networksize][processedevents][bundle][routing] = {}
							l = lambda x: x['instance.count']
							g = sorted(g, key=l)
							for instances, g in itertools.groupby(g, l):
								instances=str(instances)
								hierark[simname][degree][networksize][processedevents][bundle][routing][instances] = list(g)
								#print simname, networksize, processedevents, bundle, routing, instances, len(list(g))


	sims = {}
	for sim in glob.glob("simulations/*.sim"):
		sims[sim] = 0
		m = re.match(r"simulations/(\w+)\.(\d+)\.(\d+)\.nodes\.(\d+)\.events\.(\d+)\.bundle\.sim", sim)

		groups = []
		groups.append(m.group(1))
		groups.append(str(int(m.group(2))))
		groups.append(str(int(m.group(3))))
		groups.append(str(int(m.group(4))))
		groups.append(str(int(m.group(5))))


		v = hierark
		for g in groups:
			if g in v: v = v[g]
			else: 
				break

		
		if i == '--list':
			for m in v:
				sims[sim] = {}
				for j in v[m]:
					sims[sim][j] = len(v['round.robin'][j])
		else:
			if not 'round.robin' in v: continue
			if i in v['round.robin']: sims[sim] = len(v['round.robin'][i])

	

	s = sorted(sims.keys(), key=lambda x: sims[x])

	if i == '--list':
		counts = {}

		for sim in s:
			for j in sims[sim]:
				if not j in counts: counts[j] = 0
				counts[j] += 1#sims[sim][j]
				#print j, ":", sim, sims[sim][j]
		for k, v in counts.items():
			print k, "%0.1f%%" % (1.0*v/len(sims)*100)
	else:
		lowest = min(sims.values())
		possible = [sim for sim in sims.keys() if sims[sim] == lowest]

		config = random.choice(possible)
		print config



if __name__ == "__main__":
	main(sys.argv[1])