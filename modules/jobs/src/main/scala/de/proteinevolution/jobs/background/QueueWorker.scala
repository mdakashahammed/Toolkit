package de.proteinevolution.jobs.background

import akka.stream.scaladsl.{ Keep, Sink }
import akka.stream.{ ActorAttributes, Materializer, Supervision }
import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.models.JobWorkerEvent._
import de.proteinevolution.jobs.services.{ BackgroundService, JobStartService, JobUpdateService }
import javax.inject.{ Inject, Singleton }
import play.api.Logging

@Singleton
final class QueueWorker @Inject()(
    jobWorkerQueue: JobWorkerQueue,
    constantsV2: ConstantsV2,
    service: BackgroundService,
    startService: JobStartService,
    updateService: JobUpdateService
)(implicit mat: Materializer)
    extends ToolkitTypes
    with Logging {

  // TODO task is to keep track of a job during its execution cycle
  // where might this be important?
  // eigentlich sind alle operationen entweder datenbank operationen
  // oder notifications
  // manchmal wird polling verlangt.
  // dazu muss geschaut werden, was gepollt werden muss, also müssen jobs evtl getracked werden

  // how to message the correct user then?

  private val currentJobs = ??? // Holds the supervised jobs? not sure about this, rather put it in a GraphStage

  jobWorkerQueue.source
    .mapAsyncUnordered(constantsV2.nJobActors) {
      case PrepareJob(job, params, startJob, isInternalJob) =>
        startService.prepare(job, params, startJob, isInternalJob)
      case StartJob(jobID)          => startService.start(jobID)

      case PushJob(job)             =>
      case ClearJob(jobID, deleted) =>
      case SetSGEID(jobID, sgeID)   => service.setSGE(jobID, sgeID) // TODO mover into updateservice
    }
    .withAttributes(ActorAttributes.supervisionStrategy { t =>
      logger.error("Queue Worker crashed", t)
      Supervision.Resume
    })
    .toMat(Sink.ignore)(Keep.left)
    .run()

}
