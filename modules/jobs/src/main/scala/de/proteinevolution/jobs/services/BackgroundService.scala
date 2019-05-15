package de.proteinevolution.jobs.services

import akka.actor.ActorSystem
import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import javax.inject.{ Inject, Singleton }
import play.api.cache.{ NamedCache, SyncCacheApi }

import scala.concurrent.{ ExecutionContext, Future }


// TODO move to the UPDATESERVICE
// find out about the way TEL is called and used
// after that we can replace tel with a much more concise
// version, which is called tel2 -- but first get rid of the job actor

@Singleton
final class BackgroundService @Inject()(
    jobDao: JobDao,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi
)(implicit ec: ExecutionContext, system: ActorSystem)
    extends ToolkitTypes {

  def setSGE(jobID: String, sgeID: String): Future[Option[Job]] = {
    jobDao.modifyJob(bdoc(Job.JOBID -> jobID), bdoc("$set" -> bdoc(Job.SGEID -> sgeID)))
  }

}
