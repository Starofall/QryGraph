package util

import java.io.{OutputStream, PrintStream}

/**
  * as Pig sometimes want to write things to System.out we would get a spammed log
  * so this helper allows to disable and enable System.Out
  */
object SystemOutHelper {
  /** the original System.out from the JVM */
  private val originalStream = System.out
  private val originalErrStream = System.err
  /** a dummyWrite without any functionality */
  private val dummyStream = new PrintStream(new OutputStream() {
    def write(b: Int) = {}
  })

  /** disables system.out */
  def disableSystemOut(): Unit = {
    System.setOut(dummyStream)
    System.setErr(dummyStream)
  }

  /** enables system.out */
  def enableSystemOut(): Unit = {
    System.setOut(originalStream)
    System.setErr(originalErrStream)
  }
}
