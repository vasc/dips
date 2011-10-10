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

def main():
	results = load_results(['instance.type', 'bundle.size', 'infection.degree', 'instance.count','network.size'])
	results = results['c1.medium']['1000']['1']
	for count in results:
		for size in results[count]:
			print results[count][size]
			results[count][size] = mean(map(lambda x: x['processed.events.per.second.per.instance'], results[count][size]))

	if '8' in results: del results['8']

	linestyles = ['-','--','-.',':']	


	for i in range(0, len(results)):
		count = results.keys()[i] 
		r = results[count]
		x = sorted(map(int, r.keys()))
		y = map(lambda x: r[str(x)], x)


		r = plot(x, y, label="%s instances" % count, color='k', linestyle=linestyles[i])
		
	legend()
	show()

	print yaml.dump(results)

if __name__ == "__main__":
	main()