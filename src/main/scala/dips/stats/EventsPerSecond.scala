package dips.stats

import peersim.core.CommonState
import dips.util.Logger

class EventsPerSecond(prefix:String) extends Stats(prefix) {
  val initial_time = 0L
  var last_time = initial_time
  clear()
  
  override def execute():Boolean = {
    if(last_time == 0){ return false }
    val current_time = System.nanoTime
    val events = CommonState.getTime
    val averageEventProssecingDuration = (current_time - initial_time) / (CommonState.getTime+1)
    val lastEventDuration = current_time - last_time
    add(lastEventDuration)
    last_time = System.nanoTime
    
    
    //Logger.log.debug("Event time: " + lastEventDuration)
    false
  }
}