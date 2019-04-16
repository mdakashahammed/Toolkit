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
    Future.successful(())
  }

  def start(jobID: String): Future[Unit] = {
    Future.successful(())
  }

}
