import redis
import matplotlib
matplotlib.use('Agg')
from matplotlib.pylab import *

import numpy
import scikits.statsmodels as sm
import scikits.statsmodels.tools as smt

def make_buckets(r, key, count):
	buckets = []
	l = r.llen(key)-1
	for i in range(0, l / count):
		els = r.lrange(key, i*count, (i+1)*count)
		print els
		val = reduce(lambda x,y: int(y)+x, els, 0)/count
		buckets.append(val)
	return buckets 


def main(host="localhost", port=6379):
	r = redis.Redis(host=host, port=port, db=0)
	l = r.llen("infection:1:test:control.stats")-2
	els = r.lrange("infection:1:test:control.stats", 0, l-1)
	
	list_of_events = []
	for el in els:
		list_of_events.append(int(el)/1000)


	fig = figure(1)
	#ax = fig.add_subplot(111)
	#ma = matplotlib.mlab.movavg(numpy.array(list_of_events), 1000)
	#buckets = make_buckets(r, "infection:1:test:control.stats", 3000)
	#ax.plot(range(0,l), list_of_events)

	ecdf = smt.tools.ECDF(list_of_events)

	x = numpy.linspace(min(list_of_events), 300)
	y = ecdf(x)
	plot(x, 100**y, c='k')
	#hist(list_of_events, bins=100)
	grid(True)
	xlabel('Processing speed ($\mu\ s$)')
	ylabel('Event processing CDF (%)')
	savefig('single_instance_event_duration_cdf.eps')

	#ax.plot(range(0,len(ma)), ma)
		
	#plt.savefig("test.eps")
	#plt.show()



if __name__ == "__main__":
	main()

