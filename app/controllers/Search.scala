package controllers

import play.Logger
import models.Constants
import models.database.Job
import play.api.cache._
import play.api.libs.json.Json
import javax.inject.{Singleton, Inject}
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future


@Singleton
final class Search @Inject() (
          @NamedCache("userCache") implicit val userCache        : CacheApi,
                               val reactiveMongoApi : ReactiveMongoApi,
                               val jobDao           : JobDAO)
                           extends Controller with Constants
                                              with ReactiveMongoComponents
                                              with UserSessions {

  

  def ac(queryString : String) = Action.async{ implicit request =>
    jobDao.jobIDtermSuggester(queryString).map { richSearchResponse =>
      val jobIDEntries = richSearchResponse.suggestion("jobID")
      if (jobIDEntries.size > 0) {

        val resp = jobIDEntries.entry(queryString).optionsText.toList

        Ok(Json.toJson(resp))

      } else {

        Ok

      }
    }
  }

  def autoComplete(queryString : String) = Action.async{ implicit request =>
    // Grab Job ID auto completions
    val jobIDSuggestions = jobDao.jobIDtermSuggester(queryString).map(_.suggestion("jobID").entry(queryString).optionsText.toList)
    jobIDSuggestions.map(ids => Logger.info("Found Strings: " + ids.toString()))

    // Search for jobIDs in ES
    val searchHits = jobIDSuggestions.flatMap(jobIDSuggestions => jobDao.getJobIDs(jobIDSuggestions)).map(_.getHits)
    searchHits.map(ids => Logger.info("Found Hits: " + ids.totalHits()))
    // Grab main IDs from the hits
    val mainIDStrings = searchHits.map(_.hits().toList.map(_.id()))
    // Convert to BSON mainIDs
    val futureMainIDs = mainIDStrings.map(_.map(mainIDString => BSONObjectID.parse(mainIDString).get))
    futureMainIDs.map(ids => Logger.info("mainID: " + ids.toString()))

    // Grab Job Objects from the Database
    futureMainIDs.map(mainIDs => findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs)))).flatMap{ jobs =>
      jobs.map(joblist => Logger.info("Final Result: " + joblist.toString()))
      jobs.map(_.map(_.cleaned())).map(jobJs => Ok(Json.toJson(jobJs)))
    }
  }


  def elasticSearch(userID : BSONObjectID, queryString : String) = {
    jobDao.fuzzySearchJobID(queryString).flatMap { richSearchResponse =>
      if (richSearchResponse.totalHits > 0) {
        val jobIDEntries = richSearchResponse.getHits.getHits
        val mainIDs      = jobIDEntries.toList.map(hit => BSONObjectID.parse(hit.id).get)

        // Collect the list of jobs
        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in"-> mainIDs)))
      } else {
        Future.successful(List.empty[Job])
      }
    }
  }


  def getJob(queryString : String) = Action.async { implicit request =>
    // Retrieve the user from the cache or the DB
    getUser.flatMap { user =>
      elasticSearch(user.userID, queryString).map{ jobList =>
        Ok(Json.obj("jobs" -> jobList.map(_.cleaned())))
      }
    }
  }


  def checkJobID(jobID : String) = Action.async{
    jobDao.existsJobID(jobID).map{ richSearchResponse =>
      Ok(Json.obj("exists" -> {richSearchResponse.getHits.getTotalHits > 0}))
    }
  }


}
