import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/*
    Actor creation and simple communication example
 */

object Example1 {

  object ActorA {
    def apply(): Behavior[String] = Behaviors.receive {(context, message) =>
      println(s"Actor A Path: ${context.self.path}")
      println(s"Actor A received Message: $message")
      Behaviors.same
    }
  }

  object ActorB {
    def apply(): Behavior[String] = Behaviors.receive {(context, message) =>
      println(s"Actor B Path: ${context.self.path}")
      println(s"Actor B received Message: $message")
      Behaviors.same
    }
  }

  object UserGuardian {
    def apply(): Behavior[String] = Behaviors.setup {context =>
      println(s"User Guardian Path: ${context.self.path}")
      val actorA = context.spawn(ActorA(), "actor-a")
      val actorB = context.spawn(ActorB(), "actor-b")

      actorA ! "Hello My Friend"
      actorB ! "Hello My Friend"

      Behaviors.empty
    }
  }

  def main(args: Array[String]): Unit = {
    ActorSystem(UserGuardian(), "actor-system")
  }

}

