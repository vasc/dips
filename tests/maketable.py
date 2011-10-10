import glob
import yaml
import simplejson
import itertools
from jinja2 import Template

def sum_key(i, l):
	values = [l(x) for x in i]
	return sum(values)

def avg(i, l):
	total = sum_key(i, l)
	return 1.0*total/len(i)

def groupby(values, k):
	l = lambda x: x[k]
	s = sorted(list(values), key=l)
	return itertools.groupby(s, l)

def make_values(values):
	r = {}
	r['eps'] = {}
	r['eps']['total'] = "%d" % avg(values, lambda x: x['processed.events.per.second.total'])
	r['eps']['pi'] = "%d" % avg(values, lambda x: x['processed.events.per.second.per.instance'])
	r['amd'] = {}
	r['amd']['global'] = "%0.3f" % (avg(values, lambda x: x['average.delay.global'])/1000)
	r['amd']['local'] = "%0.3f" % (avg(values, lambda x: x['average.delay.local'])/1000)
	r['amd']['remote'] = "%0.3f" % (avg(values, lambda x: x['average.delay.remote'])/1000)
	r['it'] = {}
	r['it']['total'] = "%0.3f" % (avg(values, lambda x: x['idle.time.total'])/1000000000)
	r['it']['pi'] = "%0.3f" % (avg(values, lambda x: x['idle.time.per.instance'])/1000000000)
	return r



def main():
	values = []
	for j in glob.glob("results/*.json"):
		with open(j) as f:
			r = simplejson.loads(f.read())
			values.extend(r)
	
	instances = []

	for instype_k, g in groupby(values, 'instance.type'):
		instype = {}
		values=list(g)
		instype['name'] = instype_k
		instype['size'] = len(values)
		instype['children'] = []
		instances.append(instype)
		for inscount_k, g in groupby(values, 'instance.count'):
			inscount = {}
			values=list(g)
			inscount['name'] = inscount_k
			inscount['size'] = len(values)
			inscount['children'] = []
			instype['children'].append(inscount)
			for degree_k, g in groupby(values, 'infection.degree'):
				degree = {}
				values=list(g)
				degree['name'] = degree_k
				degree['size'] = len(values)
				degree['children'] = []
				inscount['children'].append(degree)
				for netsize_k, g in groupby(values, 'network.size'):
					netsize = {}
					values=list(g)
					netsize['name'] = netsize_k
					netsize['size'] = lambda: len(netsize)
					netsize['children'] = []
					degree['children'].append(netsize)
					for bundle_k, g in groupby(values, 'bundle.size'):
						bundle = {}
						values=list(g)
						bundle['name'] = bundle_k
						values = make_values(values)
						bundle['size'] = 1
						bundle['children'] = [values]
						netsize['children'].append(bundle)
	
	#print yaml.dump(instance)
	with open('performance_table.tex') as f:
		template = Template(f.read())
	
	with open('performance.table.gen.tex', 'w') as f:
		result = template.render(instances=instances)
		f.write(result)


if __name__ == '__main__':
	main()
