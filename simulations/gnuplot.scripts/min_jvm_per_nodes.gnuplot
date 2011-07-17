set terminal png 
set output "min_jvm_per_nodes.png"
set title "Memória mínima usada por número de nós simulados"
set ylabel "Memória JVM [MiB]"
set xlabel "Número de nós"
set style data histogram
set style histogram clustered gap 1
set style fill solid border -1
set key off
set grid ytics nomytics 
set xtics nomirror rotate by -45
plot "gnuplot.data/min_jvm_per_nodes.data" using 2:xticlabels(1) 