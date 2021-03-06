from jinja2 import Template

def main():
	with open('infection.degree.nodes.events.bundle.sim') as f:
		template = Template(f.read())

	for msgb in [1, 100, 1000, 10000]:
		for degree in [1, 2, 3, 5, 8]:
			for nodes in [40000, 80000, 160000, 250000, 500000, 1000000, 1500000, 2000000]:
				for events in [100000]:
					filename = "infection.%s.%07d.nodes.%05d.bundle.sim" % (degree, nodes, msgb)
					result = template.render(degree=degree, nodes=nodes, events=(events+1), msgb=msgb)
					with open(filename, 'w') as f:
						f.write(result)
						print "written: %s" % filename

if __name__ == '__main__':
	main()