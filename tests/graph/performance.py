from matplotlib.pyplot import *
import glob
import simplejson
import itertools
import yaml

from scikits.timeseries.lib.moving_funcs import mov_average_expw



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



def make_memory_graph(ma=False):
	clf()
	values = load("memory.average*.json")
	for test in values:
		instance_count = len(test['instances'])
		t = map(lambda x: x['used'], test['instances'])

		def sum_s(*args):
			return reduce(lambda x, y: x+int(y), args, 0)

		m = min(map(len, t))
		for i in t:
			del i[m:]

		test['results'] = map(sum_s, *t)
		print len(test['instances'][0]['used'])

		test['x'] = range(0, instance_count*len(test['results'])*1000, instance_count*1000)
		
		linestyles = ['-','--','-.',':']	


	for i in range(0, len(values)):
		x = values[i]['x']
		y = values[i]['results']

		if ma:
			y = mov_average_expw(y, 2000)

		gb = 1.0 * 1024**3
		y = map(lambda x: x/gb, y)

		x = map(lambda x: 1.0*x/1000000, x)


		r = plot(x, y, label="%s instances" % (len(values[i]['instances'])), color='k', linestyle=linestyles[i])
		
	legend(loc='upper left')

def load(pattern):
	values = []
	for j in glob.glob("../results/" + pattern):
		with open(j) as f:
			r = simplejson.loads(f.read())
			values.extend(r)
	return values

def load_results(keys):
	values = load("performance.infection*.json")
	return groupby(values, keys)

def percentage_of_local_events(x):
	processed_events_local = reduce(lambda x, y: x+int(y['processed.events.local']), x['instances'], 0)

	return 100.0*processed_events_local/x['processed.events.total']

def amd_make_graph(instance_type, degree):
	clf()
	results = load_results(['instance.type', 'infection.degree', 'bundle.size'])
	results = results[instance_type]




	amd1 = results[degree]
	for bs in amd1.keys():
		r = [0]*2
		r[0] = mean( [x['average.delay.remote'] for x in amd1[bs] if not x['instance.count'] == 1] )
		r[1] = mean( [x['average.delay.local'] for x in amd1[bs] if not x['instance.count'] == 1] )
		amd1[bs] = r


	linestyles = ['-','--','-.',':']	

	x = sorted(map(int, amd1.keys()))
	y = map(lambda x: amd1[str(x)][0], x)

	r = plot(x, y, label="remote", color='k', linestyle=linestyles[0])


	x = sorted(map(int, amd1.keys()))
	y = map(lambda x: amd1[str(x)][1], x)

	r = plot(x, y, label="local", color='k', linestyle=linestyles[1])

	xscale('log')
		
	legend()

	#print yaml.dump(results)


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
	grid(True)
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.total.c1.medium.mb1000.d3.eps')

	figure(2)
	make_graph('processed.events.per.second.per.instance', 'c1.medium', '1000', '3')
	grid(True)
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.per.instance.c1.medium.mb1000.d3.eps')

	figure(3)
	make_graph('processed.events.per.second.total', 'c1.medium', '1000', '1')
	grid(True)
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.total.c1.medium.mb1000.d1.eps')

	figure(4)
	make_graph('processed.events.per.second.per.instance', 'c1.medium', '1000', '1')
	grid(True)
	xlabel('Simulated network size')
	ylabel('Processed events per second')
	matplotlib.pyplot.savefig('infection.processed.events.per.second.per.instance.c1.medium.mb1000.d1.eps')

	figure(5)
	make_graph(percentage_of_local_events, 'c1.medium', '1000', '1')
	grid(True)
	ylim(0, 110)
	xlabel('Simulated network size')
	ylabel('Percentage of local events processed (%)')
	matplotlib.pyplot.savefig('infection.percentage.of.local.events.c1.medium.mb1000.d1.eps')

	figure(6)
	make_graph(percentage_of_local_events, 'c1.medium', '1000', '3')
	grid(True)
	ylim(0, 110)
	xlabel('Simulated network size')
	ylabel('Percentage of local events processed (%)')
	matplotlib.pyplot.savefig('infection.percentage.of.local.events.c1.medium.mb1000.d3.eps')

	figure(7)
	make_memory_graph()
	grid(True)
	xlabel('Number of simulated nodes ($10^6$)')
	ylabel('Memory used (in gigabytes)')
	matplotlib.pyplot.savefig('memory.eps')		

	figure(8)
	make_memory_graph(True)
	grid(True)
	xlabel('Number of simulated nodes ($10^6$)')
	ylabel('Memory used (in gigabytes)')
	matplotlib.pyplot.savefig('memory.ma.eps')	

	
	figure(9)
	amd_make_graph('m1.small', '1')
	grid(True)
	xlabel('Message Bundle size')
	ylabel('Average message delay (ms)')
	matplotlib.pyplot.savefig('amd.d1.eps')	

	figure(10)
	amd_make_graph('m1.small', '3')
	grid(True)
	xlabel('Message Bundle size')
	ylabel('Average message delay (ms)')
	matplotlib.pyplot.savefig('amd.d3.eps')	



if __name__ == "__main__":
	main()