from matplotlib.pyplot import *
import glob
import simplejson
import itertools
import yaml



def mean(numberList):
    if len(numberList) == 0:
        return float('nan')
 
    floatNums = [float(x) for x in numberList]
    return sum(floatNums) / len(numberList)

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
	for j in glob.glob("../results/*.json"):
		with open(j) as f:
			r = simplejson.loads(f.read())
			values.extend(r)
	
	return groupby(values, keys)

def percentage_of_local_events(x):
	processed_events_local = reduce(lambda x, y: x+int(y['processed.events.local']), x['instances'], 0)

	return 100.0*processed_events_local/x['processed.events.total']

def make_graph(metric, instance_type, bundle_size, degree):
	clf()
	results = load_results(['instance.type', 'bundle.size', 'infection.degree', 'instance.count','network.size'])
	results = results[instance_type][bundle_size][degree]
	for count in results:
		for size in results[count]:
			#print results[count][size]
			if hasattr(metric, '__call__'):
				results[count][size] = mean(map(lambda x: metric(x), results[count][size]))
			else:
				results[count][size] = mean(map(lambda x: x[metric], results[count][size]))

	if '8' in results: del results['8']

	linestyles = ['-','--','-.',':']	


	for i in range(0, len(results)):
		count = results.keys()[i] 
		r = results[count]
		x = sorted(map(int, r.keys()))
		y = map(lambda x: r[str(x)], x)


		r = plot(x, y, label="%s instances" % count, color='k', linestyle=linestyles[i])
		
	legend()

	#print yaml.dump(results)


def main():
	figure(1)
	make_graph('processed.events.per.second.total', 'c1.medium', '1000', '3')
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.total.c1.medium.mb1000.d3.eps')

	figure(2)
	make_graph('processed.events.per.second.per.instance', 'c1.medium', '1000', '3')
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.per.instance.c1.medium.mb1000.d3.eps')

	figure(3)
	make_graph('processed.events.per.second.total', 'c1.medium', '1000', '1')
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.total.c1.medium.mb1000.d1.eps')

	figure(4)
	make_graph('processed.events.per.second.per.instance', 'c1.medium', '1000', '1')
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.per.instance.c1.medium.mb1000.d1.eps')

	figure(5)
	make_graph(percentage_of_local_events, 'c1.medium', '1000', '1')
	ylim(0, 110)
	xlabel('Simulated network size')
	ylabel('Percentage of local events processed (%)')
	matplotlib.pyplot.savefig('infection.percentage.of.local.events.c1.medium.mb1000.d1.eps')

	figure(6)
	make_graph(percentage_of_local_events, 'c1.medium', '1000', '3')
	ylim(0, 110)
	xlabel('Simulated network size')
	ylabel('Percentage of local events processed (%)')
	matplotlib.pyplot.savefig('infection.percentage.of.local.events.c1.medium.mb1000.d3.eps')
		

if __name__ == "__main__":
	main()