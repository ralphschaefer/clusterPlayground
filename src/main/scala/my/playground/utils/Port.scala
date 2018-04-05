package my.playground.utils

import java.net.ServerSocket

object Port {

  def findFree(from:Int = 8080, count:Int = 100):Option[Int] = {

    def isLocalPortFree(port:Int):Boolean = {
      try
      {
        new ServerSocket(port).close()
        true
      } catch {
        case _:Exception =>
          false
      }
    }

    for (port <- from to from+count )
      if (isLocalPortFree(port))
        return Some(port)
    None

  }

}
