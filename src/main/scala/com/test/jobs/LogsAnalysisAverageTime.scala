package com.test.jobs

import com.twitter.scalding.{Tsv, Args, Csv, Job}


class LogsAnalysisAverageTime(args: Args) extends Job(args) {


  object Schema extends Enumeration {
    val timestamp, userId, action, actionData = Value // arbitrary number of fields
  }

  import Schema._

  val inputSource = Csv(args("in"), fields = Schema, skipHeader = true)
  inputSource.read.map(actionData -> 'sessionid) {
    line: String => {
      val processSessionId = (str: String) => str.take(str.indexOf('_'))
      val sessionId = processSessionId(line)
      sessionId
    }
  }.project('sessionid, userId, timestamp).groupBy('sessionid, userId) {
    _.reduce(timestamp -> 'durationTime) { (p1: Long, p2: Long) => {
      p2 - p1
    }
    }
  }.project('sessionid, 'durationTime)
    .groupBy('sessionid) {
    _.average('durationTime -> 'averageduration)
  }.project('sessionid, 'averageduration).write(new Tsv(args("out"))) //TODO I know this is wrong but I do not know how to find the gap only in scalding code
}
