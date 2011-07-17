set terminal png 
set output "avg_mem_per_nodes.png"
set title "Memória média usada por processo com 2G de memória máxima na JVM"
set ylabel "Memória média [MiB]"
set xlabel "Número de nós"
set style data histogram
set style histogram clustered gap 1
set style fill solid border -1
set key off
set grid ytics nomytics 
set xtics nomirror rotate by -45
plot "gnuplot.data/avg_mem_per_nodes.data" using 2:xticlabels(1) 