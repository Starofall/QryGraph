package util

import java.io._
import java.net.URI
import java.security.PrivilegedAction

import models.Tables
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.SaslRpcServer.AuthMethod
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.util.Progressable

/**
  * Created by info on 03.10.2016.
  */
object HDFS {


  def writeFile(globalSetting: Tables.GlobalSetting,fileName: String, uploadedFile: File) = {
    val fis = new FileInputStream(uploadedFile)
    writeStream(globalSetting,fileName,fis)
  }

  def writeStream(globalSetting: Tables.GlobalSetting,fileName: String, inputStream: InputStream) = {
    val hdfsUrl = globalSetting.fsDefaultName //"hdfs://localhost:9001"
    val qryGraphFolder =  globalSetting.qrygraphFolder //"/user/qrygraph/"

    UserGroupInformation.createRemoteUser(globalSetting.hadoopUser, AuthMethod.SIMPLE).doAs(new PrivilegedAction[Unit] {
      override def run(): Unit = {
        val configuration = new Configuration()
        configuration.set("dfs.client.use.datanode.hostname", "true")
        val hdfs = FileSystem.get(new URI(hdfsUrl), configuration)

        //check if our folder exists and create it if needed
        val qryfolder = new Path(hdfsUrl + qryGraphFolder)
        if (!hdfs.exists(qryfolder)) {
          hdfs.mkdirs(qryfolder)
        }

        val file = new Path(hdfsUrl + "/"  + qryGraphFolder + "/" + fileName)
        if (hdfs.exists(file)) {
          hdfs.delete(file, true)
        }

        val os = hdfs.create(file,
          new Progressable() {
            def progress() {
              println("...working...")
            }
          })

        // in
        val in = new BufferedReader(new InputStreamReader(inputStream))
        // out
        val out = new BufferedWriter(new OutputStreamWriter(os))

        var aLine = in.readLine()
        while (aLine != null) {
          //Process each line and add output to Dest.txt file
          out.write(aLine)
          out.newLine()
          aLine = in.readLine()
        }
        // do not forget to close the buffer reader
        in.close()
        // close buffer writer
        out.close()
        hdfs.close()
      }
    })
  }
}
