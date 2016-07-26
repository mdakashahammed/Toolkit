package models.tel

import java.nio.file.attribute.PosixFilePermission

import better.files.Cmds._
import better.files._
import models.Constants

import scala.sys.process._
import models.Implicits._


/**
  *
  * Created by lzimmermann on 26.05.16.
  */
object TEL extends TELRegex with TELConstants with Constants {


  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")


  // Loads the init file variables
  private val inits =  Process(initFile.pathAsString).!!.split('\n').map { param =>
      val spt = param.split('=')
      spt(0) -> spt(1)
    }.toMap


  //sets the context as environment variable whereas it is important to do this with the source command
  private val setEnv = s"source$initFile"
  Process(setEnv)



  // Returns the context name currently set
  //  TODO the CONTEXT variable can currently only be set in the init script - how do you want to set it?
  private var getContext : String = inits.getOrElse("CONTEXT", "LOCAL")

  //get the context from the environment variable

  private val getEnv = sys.env.get("CONTEXT")
  getContext = getEnv.getOrElse("LOCAL")
  //another solution could be to do directly sth as below without using the init script

  val hostname_cmd = "hostname"
  private val hostname = hostname_cmd.!!

  if (hostname.equals("olt"))
  getContext = "sge"

  else
    getContext = "LOCAL"


  // but this ain't tested yet - of course for production mode this is completely irrelevant



  //-----------------------------------------------------------------------------------------------------
  // Constants (values statically set, available in each runscript)
  //-----------------------------------------------------------------------------------------------------
  private var constants = loadConstants()

  private def loadConstants() = {

    // Get lines from CONSTANTS file, ignore empty lines, Split by key,value separator and make key/value map
    constantsFile
      .lineIterator
      .withoutComment(commentChar)
      .noWSLines
      .map { line =>

        val spt = line.split("=")
        spt(0).trim() -> spt(1).trim()

    }.toMap
  }

  def getConstant(constant : String) = {

    // TODO Check whether file which holds constants has changed, otherwise return old value
    // TODO Currently just returns the loaded constants
    // You probably want to use loadConstants here
    constants(constant)
  }



  //-----------------------------------------------------------------------------------------------------
  // Set params (Parameters whose values can be taken from a predefined set)
  //-----------------------------------------------------------------------------------------------------

  // Keeps a map of all setParams with their respective allowed values, together with the plain text name
  private var setParams : Map[String, Map[String, String]] = loadSetParams()

  // Reloads all the set Params from the scripts in params.d
  private def loadSetParams() = {


   paramsDFile.list.withFilter(_.isRegularFile).map { f =>

      f.name.replaceAll(".sh", "") -> Process(f.pathAsString).!!.split('\n').map { param =>
        val spt = param.split(' ')
        spt(0) -> spt(1)
      }.toMap
    }.toMap
    }


  /**
    * Returns the Array of all values and plain text names of the set params
    *
    * @param param
    */
  def getSetParam(param : String) = {

    // TODO Reload contents of params.d if changed
    // You probably want to use load set params here

    setParams(param)
  }

  //-----------------------------------------------------------------------------------------------------
  // Other stuff
  //-----------------------------------------------------------------------------------------------------

  /**
    * Uses reflection to invoke the Method
    *
    * @param methodname
    * @param t
    * @param content
    */
  def invoke(methodname : String, t : String, content : String): Unit = {

    val clazz = Class.forName("models.tel.TEL_" + t )

    // TODO Implement me

  }



  //-----------------------------------------------------------------------------------------------------
  // Private methods
  //-----------------------------------------------------------------------------------------------------

  /**
    *
    *
    *
    */
  def parseParams(): Unit = {


  }




  //-----------------------------------------------------------------------------------------------------
  // Public methods
  //-----------------------------------------------------------------------------------------------------

  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the executable script for job execution.
    */
  def init(runscript : String, params : Map[String, String], dest : String): String = {

    // Create directories necessary for tool execution
    subdirs.foreach { s => (dest + SEPARATOR + s).toFile.createDirectories() }

    // Write parameters to file
    for((paramName, value) <- params ) {

      // TODO This is a hack and needs to go
      if(! ignore.contains(paramName)) {

        s"$dest${SEPARATOR}params$SEPARATOR$paramName".toFile.write(value)
      }
    }

    //
    val source = s"$runscriptPath$runscript.sh"
    val target = s"$dest$runscript.sh".toFile

    lazy val newLines = source.toFile.lines.map { line =>

      replaceeString.replaceAllIn(line, { matcher =>

          matcher.group("expression").trim() match {

          // Replace constants
          case constantsString(constant) => constants(constant)

          // Replace param String
          case parameterString(paramName, selector) =>

            // Some selectors hard-coded TODO Introduce the extensions of selectors with arbitrary methods
            selector match {
              case "path" => s"params$SEPARATOR$paramName"
              case "content" =>
                s"${dest}params$SEPARATOR$paramName".toFile.contentAsString
            }

          // Should not happen
          case _ => "notImplemented"

        }
      })
    }.toSeq
    target.appendLines(newLines:_*)

    val context = getContext
    if(context == "LOCAL") {

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(target.name)

      // Make the runscript executable
      chmod_+(PosixFilePermission.OWNER_EXECUTE, target)

      target.pathAsString
    } else {

      // Write the context script to the job directory and insert runscript name
      // TODO The context script should also use the extended syntax
      val contextLines = s"$contextPath$context.sh".toFile.lines.map { line =>

        runscriptString.replaceAllIn(line, s"$runscript.sh")
      }.toSeq

      val contextFile = s"$dest$context.sh".toFile

      contextFile.appendLines(contextLines:_*)
      chmod_+(PosixFilePermission.OWNER_EXECUTE, contextFile)

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(contextFile.name)
      contextFile.pathAsString
    }
  }
}

