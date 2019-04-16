package de.proteinevolution.jobs.models

import de.proteinevolution.common.models.database.jobs.JobState
import reactivemongo.bson.BSONObjectID

sealed trait JobWorkerEvent

object JobWorkerEvent {

  case class PrepareJob(
      job: Job,
      params: Map[String, String],
      startJob: Boolean = false,
      isInternalJob: Boolean = false
  ) extends JobWorkerEvent

  case class CheckJobHashes(jobID: String) extends JobWorkerEvent

  // Messages the Job Actor to start a job
  case class StartJob(jobID: String) extends JobWorkerEvent

  // Messages the jobActor accepts from outside
  case class PushJob(job: Job) extends JobWorkerEvent // move to the message package

  // Message for the Websocket Actor to send a ClearJob Message
  case class ClearJob(jobID: String, deleted: Boolean = false) extends JobWorkerEvent

  // User Actor starts watching
  case class AddToWatchlist(jobID: String, userID: BSONObjectID) extends JobWorkerEvent

  // checks if user has submitted max number of jobs
  // of jobs within a given time
  case class CheckIPHash(jobID: String) extends JobWorkerEvent

  // UserActor Stops Watching this Job
  case class RemoveFromWatchlist(jobID: String, userID: BSONObjectID) extends JobWorkerEvent

  // JobActor is requested to Delete the job
  case class Delete(jobID: String, userID: Option[BSONObjectID] = None) extends JobWorkerEvent

  // Job Controller receives a job state change from the SGE or from any other valid source
  case class JobStateChanged(jobID: String, jobState: JobState) extends JobWorkerEvent

  case class SetSGEID(jobID: String, sgeID: String) extends JobWorkerEvent

  // show browser notification
  case class ShowNotification(notificationType: String, tag: String, title: String, body: String) extends JobWorkerEvent

  // Job Controller receives push message to update the log
  case class UpdateLog(jobID: String) extends JobWorkerEvent

  // forward filewatching task to ws actor

  case class WatchLogFile(job: Job) extends JobWorkerEvent

}
