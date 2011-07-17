import re
import argparse


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert data given by test_script.bash into gnuplot data.')
    parser.add_argument('-s', '--step', metavar='N', type=int, nargs=1, help='number of iterations done by MemoryObserver.', default=100)
    parser.add_argument('data', metavar="FILE", help='file name containing the data.', type=str)
    
    args = parser.parse_args()

    #control.mo: max=119341056, total=73924608, free=56851912
    pat = re.compile('control.mo: max=(?P<max>[0-9]+), total=(?P<total>[0-9]+), free=(?P<free>[0-9]+)')
    
    current_step = 0
    for line in open(args.data):
        m = pat.match(line)
        if m:
            print current_step, (int(m.group('total')) - int(m.group('free'))) / (1024*1024)
            current_step = current_step + args.step
