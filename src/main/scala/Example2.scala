import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.io.Source

object Example2 {

  // Controller Messages
  trait ControllerCommand
  case class StartCounting(fileName: String) extends ControllerCommand
  case class ControllerWordCountReply(count: Int) extends ControllerCommand

  // Worker Message
  case class WorkerWordCountTask(text: String, replyTo: ActorRef[ControllerCommand])

  object WordCounterController {
    def apply(totalWordCount: Int = 0, totalWorkerCount: Int = 1): Behavior[ControllerCommand] = Behaviors.receive { (context, message) =>
      message match {
        case StartCounting(fileName) =>
          val textFile = Source.fromResource(fileName)
          var workerIndex = totalWordCount
          for (line <- textFile.getLines()) {
            val counterWorker = context.spawn(WordCounterWorker(), s"worker-$workerIndex")
            counterWorker ! WorkerWordCountTask(line, context.self)
            workerIndex = workerIndex + 1
          }
          apply(totalWordCount, workerIndex + 1)

        case ControllerWordCountReply(count) =>
          val newTotalCount = totalWordCount + count
          println(s"Total Count: $newTotalCount")
          apply(newTotalCount, totalWorkerCount)

        case _ =>
          println("No such message defined")
          Behaviors.same
      }
    }
  }

  object WordCounterWorker {
    def apply(): Behavior[WorkerWordCountTask] = Behaviors.receive { (context, message) =>
      message match {
        case WorkerWordCountTask(text, replyTo) =>
          println(s"OS THREAD [${Thread.currentThread().getName}] : Akka Actor Thread [${context.self.path.name}]")
          val wordCount = text.split(" ").length
          replyTo ! ControllerWordCountReply(wordCount)
          Behaviors.same
        case _ =>
          println("No such message defined")
          Behaviors.same
      }
    }
  }

  object UserGuardian {
    def apply(): Behavior[String] = Behaviors.setup {context =>
      val controller = context.spawn(WordCounterController(), "controller-actor")

      controller ! StartCounting("big.txt")

      Behaviors.empty
    }
  }

  def main(args: Array[String]): Unit = {
    ActorSystem(UserGuardian(), "actor-system")
  }

}
