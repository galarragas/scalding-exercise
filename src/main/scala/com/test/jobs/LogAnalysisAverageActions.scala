package com.test.jobs


import com.twitter.scalding.{Tsv, Csv, Args, Job}


class LogAnalysisAverageActions(args: Args) extends Job(args) {

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
  }.project('sessionid, action).groupBy('sessionid, action) {
    _.size('numberofactions)
  }.project('sessionid, action, 'numberofactions).groupBy('sessionid) {
    _.average('numberofactions)
  }.write(new Tsv(args("out")))

}
