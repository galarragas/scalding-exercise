package com.test.main

import com.test.jobs.{LogAnalysisAverageActions, LogsAnalysisAverageTime}
import com.twitter.scalding.{Args, Hdfs, Local, Mode}
import org.apache.hadoop.conf.Configuration

/**
 * Created by bo on 23/10/14.
 */
object MainApp{

  def main(args:Array[String])
  {
    //from scalding 0.11 there is no need to use the Tool.run, this is the perfect way to get args and run the job.
    //The single job can be easily triggered and some OO concepts like cake pattern can be used in this
    args.map(println)
    val hadoopConfig = new Configuration
    val mode = if (args(0) == "local") Local(strictSources = true) else Hdfs(strict = true, hadoopConfig) // work both for local or cluster
  val inputPath = args(1)
    val outPathAverageAction = args(2)
    val outPathAverageAverageTime = args(3)
    val scaldingArgsAverageAction: String = "--in " + inputPath + " --out " + outPathAverageAction //build the parameters
  val scaldingArgsAverageTime: String = "--in " + inputPath + " --out " + outPathAverageAverageTime //build the parameters
  val argmentsAverageAction = Mode.putMode(mode, Args(scaldingArgsAverageAction))
    val argmentsAverageTime = Mode.putMode(mode, Args(scaldingArgsAverageTime))
    val jobAverageAction = new LogAnalysisAverageActions(argmentsAverageAction)
    val jobAverageTime = new LogsAnalysisAverageTime(argmentsAverageTime)
    val flowAvarageAction = jobAverageAction.buildFlow
    flowAvarageAction.complete()
    val flowAvarageTime = jobAverageTime.buildFlow
    flowAvarageTime.complete()

  }
}
