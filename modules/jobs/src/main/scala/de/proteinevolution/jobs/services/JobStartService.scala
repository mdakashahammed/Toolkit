package de.proteinevolution.jobs.services

import javax.inject.{ Inject, Singleton }
import de.proteinevolution.jobs.models.Job

import scala.concurrent.Future

@Singleton
class JobStartService @Inject()() {

  // start, prepare, configuration, (param validation?)

  // obviously a job can be prepared
  def prepare(
      job: Job,
      params: Map[String, String],
      startJob: Boolean,
      isInternalJob: Boolean
  ): Future[Unit] = {

    val extendedParams = params + ("jobid" -> job.jobID)

    import de.proteinevolution.tel.execution.ExecutionContext.FileAlreadyExists

    try {

    } catch {
      case FileAlreadyExists(_) =>
    }

    Future.successful(())
  }

  def start(jobID: String): Future[Unit] = {
    Future.successful(())
  }

}


// das ganze preparedjob ist unnötig,
// Schritte:
// Params validieren, hier wird nur der Ordner angelegt

// Starten, Prepared

/*
case PrepareJob(job, params, startJob, isInternalJob) =>

      // jobid will also be available as parameter
      val extendedParams = params + ("jobid" -> job.jobID)

      // Add job to the current jobs
      currentJobs = currentJobs.updated(job.jobID, job)

      try {

        // Establish execution context for the new Job
        val executionContext = ExecutionContext(constants.jobPath / job.jobID)
        currentExecutionContexts = currentExecutionContexts.updated(job.jobID, executionContext)

        // Create a log for this job
        currentJobLogs =
          currentJobLogs.updated(job.jobID,
                                 JobEventLog(jobID = job.jobID,
                                             toolName = job.tool,
                                             internalJob = isInternalJob,
                                             events = List(JobEvent(job.status, Some(ZonedDateTime.now)))))

        // Get new runscript instance from the runscript manager
        val runscript: Runscript = runscriptManager(job.tool).withEnvironment(env)

        // Validate the Parameters right away
        val validParameters = validatedParameters(job, runscript, extendedParams)

        // adds the params of the disabled controls from formData, sets value of those to "false"
        validParameters.filterNot(pv => extendedParams.contains(pv._1)).foreach { pv =>
          extendedParams.+(pv._1 -> "false")
        }

        // Serialize the JobParameters to the JobDirectory
        // Store the extended Parameters in the working directory for faster reloading
        executionContext.writeParams(extendedParams)

        if (isComplete(validParameters)) {
          // When the user wants to force the job to start without job hash check, then this will jump right to prepared
          if (startJob) {
            self ! CheckIPHash(job.jobID)
          } else {
            log.info("JobID " + job.jobID + " will now be hashed.")
            self ! CheckJobHashes(job.jobID)
          }
        } else {
          // TODO Implement Me. This specifies what the JobActor should do
          // TODO when not all parameters have been specified or when they are invalid
          log.error("[JobActor.PrepareJob] The job " + job.jobID + " has invalid or missing parameters.")
        }
      } catch {
        case FileAlreadyExists(_) =>
          log.error(
            "[JobActor.PrepareJob] The directory for job " + job.jobID + " already exists\n" +
            "[JobActor.PrepareJob] Stopping job since it can not be retrieved by user."
          )
          self ! JobStateChanged(job.jobID, Error)
      }

 */
