import akka.actor.Actor._

package

/**
 * User: amir
 */

object App {

  def main (args:Array[String]) = {
    val actors = for { name <- List("Amir", "Björn", "Anders", "Uzi")} yield actorOf(new EPFD(name, 3, 1)).start()
    actors.foreach { registry.addListener(_) }
  }

}