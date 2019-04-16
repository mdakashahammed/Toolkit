package de.proteinevolution.jobs.services

// could be in the jobhashservice as well
// could also be renamed into job validation service

import scala.concurrent.Future


class JobStatusService {

  def limitReached: Future[Boolean] = {
      // can be integrated in the start message ...
    // or not?
  }



}
