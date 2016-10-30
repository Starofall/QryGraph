package services

import java.util.Properties

import models.Tables.GlobalSetting

/** simplifies pigProperty generation */
object PigProperties {

  /** generates properties for Hadoop connection based on the GlobalSetting */
  def generateProperties(settings: GlobalSetting): Properties = {
    val props = new Properties()
    props.setProperty("pig.use.overriden.hadoop.configs", "true")
    props.setProperty("dfs.namenode.datanode.registration.ip-hostname-check", "false")
    props.setProperty("dfs.client.use.datanode.hostname", "true")
    props.setProperty("fs.defaultFS", settings.fsDefaultName)
    props.setProperty("mapreduce.jobtracker.http.address", settings.mapredJobTracker)
    props
  }

}
