package de.proteinevolution.jobs.background

import akka.stream.QueueOfferResult._
import akka.stream._
import akka.stream.scaladsl.{ Keep, Sink, Source }
import de.proteinevolution.jobs.models.JobWorkerEvent
import javax.inject.{ Inject, Singleton }
import play.api.Logging
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal

@Singleton
final class JobWorkerQueue @Inject()(applicationLifecycle: ApplicationLifecycle)(
    implicit mat: Materializer,
    executionContext: ExecutionContext
) extends Logging {

  private[this] val (queue, pub) = Source
    .queue[JobWorkerEvent](10000, OverflowStrategy.backpressure)
    .withAttributes(ActorAttributes.supervisionStrategy { t =>
      logger.error("JobWorkerQueue crashed", t)
      Supervision.Resume
    })
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()

  applicationLifecycle.addStopHook(() => {
    queue.complete()
    queue.watchCompletion().recover {
      case NonFatal(e) => logger.error("error during shutdown", e)
    }
  })

  private[background] val source: Source[JobWorkerEvent, _] = Source.fromPublisher(pub)

  def publish(event: JobWorkerEvent): Future[Unit] = {
    queue.offer(event).flatMap {
      case Enqueued =>
        Future.successful(())
      case Failure(t) =>
        Future.failed(t)
      case Dropped =>
        Future.failed(ProcessorUnavailable(this.getClass.getName))
      case other =>
        Future.failed(JobProcessorError(other))
    }
  }

}
