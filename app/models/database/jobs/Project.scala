package models.database.jobs

/**
  * Created by snam on 30.03.17.
  *
  */
import java.time.{ZonedDateTime, Instant, ZoneId}
import play.api.libs.json._
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID }
import reactivemongo.play.json._

case class Project(mainID: BSONObjectID, // Unique Project ID
                   name: String,
                   ownerID: BSONObjectID,
                   jobIDs: List[String],
                   content: String,
                   dateModified: Option[ZonedDateTime],
                   dateCreated: Option[ZonedDateTime]) // Creation time of the project

object Project {
  // Constants for the JSON object identifiers
  val IDDB         = "_id" //              ID in MongoDB
  val OWNERID      = "ownerID"
  val DATEMODIFIED = "dateModified"
  val DATECREATED  = "dateCreated" //              created on field
  val NAME         = "name"
  val CONTENT      = "content"
  val JOBIDS       = "jobIDs" //              ID for the project

  implicit object JsonReader extends Reads[Project] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[Project] = json match {
      case obj: JsObject =>
        try {
          Project
          val mainID       = (obj \ IDDB).asOpt[String]
          val name         = (obj \ NAME).asOpt[String]
          val ownerID      = (obj \ OWNERID).asOpt[String]
          val jobIDs       = (obj \ JOBIDS).asOpt[List[String]]
          val content      = (obj \ CONTENT).asOpt[String]
          val dateModified = (obj \ DATEMODIFIED).asOpt[String]
          val dateCreated  = (obj \ DATECREATED).asOpt[String]

          JsSuccess(
            Project(
              mainID = BSONObjectID.generate(),
              name = "",
              ownerID = BSONObjectID.generate(),
              jobIDs = Nil,
              content = "",
              dateModified = Some(new ZonedDateTime()),
              dateCreated = Some(new ZonedDateTime())
            )
          )
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object ProjectWrites extends Writes[Project] {
    def writes(project: Project): JsObject = Json.obj(
      IDDB         -> project.mainID,
      NAME         -> project.name,
      JOBIDS       -> project.jobIDs,
      CONTENT      -> project.content,
      DATEMODIFIED -> project.dateModified,
      DATECREATED  -> BSONDateTime(project.dateCreated.fold(-1L)(_.toInstant.toEpochMilli))
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Project] {
    def read(bson: BSONDocument): Project = {
      Project(
        mainID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        name = bson.getAs[String](NAME).getOrElse(""),
        ownerID = bson.getAs[BSONObjectID](OWNERID).getOrElse(BSONObjectID.generate()),
        jobIDs = bson.getAs[List[String]](JOBIDS).getOrElse(Nil),
        content = bson.getAs[String](CONTENT).getOrElse(""),
        dateModified = bson.getAs[BSONDateTime](DATEMODIFIED).map(dt => ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.value), ZoneId.systemDefault())),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.value), ZoneId.systemDefault()))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Project] {
    def write(project: Project): BSONDocument = BSONDocument(
      IDDB         -> project.mainID,
      NAME         -> project.name,
      OWNERID      -> project.ownerID,
      JOBIDS       -> project.jobIDs,
      CONTENT      -> project.content,
      DATEMODIFIED -> BSONDateTime(project.dateModified.fold(-1L)(_.toInstant.toEpochMilli)),
      DATECREATED  -> BSONDateTime(project.dateCreated.fold(-1L)(_.toInstant.toEpochMilli))
    )
  }
}
