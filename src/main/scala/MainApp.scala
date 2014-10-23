import com.twitter.scalding.{Hdfs, Local, Args, Mode}
import org.apache.hadoop.conf.Configuration

/**
 * Created by bo on 23/10/14.
 */
object MainApp{

  def main(args:Array[String])
  {
    val hadoopConfig=new Configuration
    val mode=if (args(2)=="local") Local(strictSources = true) else Hdfs(strict = true,hadoopConfig) // work both for local or cluster
    val inputPath=args(0)
    val outPath=args(1)
    val scaldingArgs: String ="--in "+inputPath+" --out "+outPath //build the parameters
    val argments=Mode.putMode(mode,Args(scaldingArgs))
  }
}
