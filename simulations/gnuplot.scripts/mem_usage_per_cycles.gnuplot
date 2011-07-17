set terminal png 
set output "mem_usage_per_cycles.png"
set title "Memória usada por eventos"
set ylabel "Memória usada [MiB]"
set xlabel "Número de eventos"
set grid ytics nomytics 
set xtics nomirror rotate by -45
set xrange [0:30000]
set key autotitle column
plot "gnuplot.data/mem_usage_per_cycles.data" using 1:2 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:3 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:4 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:5 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:6 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:7 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:8 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:9 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:10 with lines,\
     "gnuplot.data/mem_usage_per_cycles.data" using 1:11 with lines