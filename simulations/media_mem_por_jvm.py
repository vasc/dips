import re
import sys

outfile='gnuplot.data/avg_mem_per_nodes.data'

if __name__ == '__main__':
    out = open(outfile, 'w')
    out.write("# Nodes      Avg\n")
    pat = re.compile('control.mo: max=(?P<max>[0-9]+), total=(?P<total>[0-9]+), free=(?P<free>[0-9]+)')
    fpat = re.compile('(?P<nodes>[0-9]+)')
    for f in sys.argv[1:]:
        fm = fpat.match(f)
        if not fm:
            print 'Unable to parse number of nodes for file ' + f
            exit(1)
        nodes = fm.group('nodes')
        count = 0
        s = 0
        for line in open(f):
            m = pat.match(line)
            if m:
                count += 1
                s += int(m.group('total')) -int( m.group('free'))
            
        out.write(nodes)
        out.write("\t\t")
        out.write(str((s / count) / (1024 * 1024)))
        out.write("\n")
