package com.test.jobs

import org.specs2.mutable.Specification

class LogAnalysisAverageTimeTest extends Specification {

  "Data should be extracted correctly" should {
    "extract sessionId correctly" in {
      val processSessionId = (str: String) => str.take(str.indexOf('_'))
      val stringToExtract: String = "session1_event3"
      val data = "session1"
      data must_== processSessionId(stringToExtract)
    }
  }

}
