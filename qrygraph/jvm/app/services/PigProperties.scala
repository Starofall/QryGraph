package services

import java.util.Properties

import models.Tables.GlobalSetting

/**
  * Created by info on 28.09.2016.
  */
object PigProperties {

  def generateProperties(settings: GlobalSetting):Properties={
    val props = new Properties()
    props.setProperty("pig.use.overriden.hadoop.configs", "true")
    props.setProperty("dfs.namenode.datanode.registration.ip-hostname-check", "false")
    props.setProperty("dfs.client.use.datanode.hostname", "true")
    props.setProperty("fs.defaultFS", settings.fsDefaultName)
    props.setProperty("mapreduce.jobtracker.http.address", settings.mapredJobTracker)
    //    props.setProperty("hadoop.tmp.dir", settings.qrygraphFolder + "/tmp")
    //    System.setProperty("hadoop.home.dir", new File(".").getAbsolutePath.dropRight(1)+"windows")
    props
  }

}
