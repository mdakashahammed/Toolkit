package de.proteinevolution.jobs

import akka.stream.QueueOfferResult

package object background {

  final case class ProcessorUnavailable(name: String)
      extends Exception(s"Processor $name cannot accept requests at this time!")

  final case class JobProcessorError(result: QueueOfferResult)
      extends Exception(s"QueueOfferResult $result was not expected!")

}
