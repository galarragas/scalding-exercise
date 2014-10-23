package com.test.jobs

import com.twitter.scalding.{Csv, Job, Args}

class LogsAnalysisAverageTime(args:Args) extends Job(args){

  val inputSource=Csv(args("in"))

}
