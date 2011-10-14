import akka.actor.Actor.registry
import akka.actor.{ActorUnregistered, ActorRegistered, Scheduler, Actor}
import akka.event.EventHandler
import com.eaio.uuid.UUID
import java.util.concurrent.TimeUnit

/**
 * @author Amir Moulavi
 */

class EPFD(name:String, var period: Long, delta: Int) extends Actor {

  private var alive:Set[UUID] = Set()
  private var suspected:Set[UUID] = Set()

  private var actors:Set[UUID] = registry.actorsFor[EPFD].map{_.uuid}.toSet - self.uuid

  EventHandler.notify(name + " Started")

  Scheduler.scheduleOnce( () => {sendOutHeartbeats}, period, TimeUnit.SECONDS)

  def receive = {
    case Heartbeat(uuid) =>
      add(uuid)
      self reply Alive(self.uuid, name)
    case Alive(uuid, n) =>
      alive += uuid
      EventHandler.notify(name + ": " + n + " is alive!")
    case event: ActorRegistered => add(event.actor.uuid)
    case event: ActorUnregistered => actors -= event.actor.uuid
  }

  def sendOutHeartbeats {
    if (alive.intersect(suspected).size != 0 )
      period += delta
    actors.foreach { p =>
      if (!alive(p) && !suspected(p)) suspect(p)
      else if (alive(p) && suspected(p)) unsuspect(p)
      registry.actorFor(p) ! Heartbeat(self.uuid)
    }
    alive = Set()
    Scheduler.scheduleOnce( () => {sendOutHeartbeats}, period, TimeUnit.SECONDS)
  }

  def add(uuid:UUID) = {
    if (uuid != self.uuid)
      actors += uuid
  }

  def suspect(uuid:UUID) = {
    suspected += uuid
  }

  def unsuspect(uuid:UUID) = {
    suspected -= uuid
  }

}

sealed trait EPFDMessages
case class Alive(uuid:UUID, name:String) extends EPFDMessages
case class Heartbeat(uuid:UUID) extends EPFDMessages
