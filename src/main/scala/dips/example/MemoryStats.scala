package dips.example

import dips.stats.Stats
import peersim.reports.MemoryObserver

class MemoryStats(override val prefix:String) extends MemoryObserver(prefix) with Stats {
	var first = true
  
	override def execute() = {
	  if(first){
	    first = false
	    clear("memory:used")
	  }
	  
	  super.execute()
	  this.add("memory:used", MemoryObserver.r.totalMemory - MemoryObserver.r.freeMemory)
	  
	  false
	}
}