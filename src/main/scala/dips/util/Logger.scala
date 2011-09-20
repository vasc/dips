package dips.util

import org.apache.log4j.BasicConfigurator;

object Logger {
   BasicConfigurator.configure();
   val log = org.apache.log4j.Logger.getRootLogger()
}

trait Logger {
  val log = {
    BasicConfigurator.configure();
    org.apache.log4j.Logger.getLogger(this.getClass)
  }
}