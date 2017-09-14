package controllers.api.assessment

import models.assessment.PartialAnswer
import play.api.libs.json.Json

/**
  * Partial API model for assessment form answer.
  */
case class ApiPartialFormAnswer(
  formId: Long,
  answers: Seq[ApiFormAnswer.ElementAnswer],
  isAnonymous: Boolean,
  isSkipped: Boolean
) {
  def toModel = PartialAnswer(
    formId,
    isAnonymous,
    answers.map(_.toModel).toSet,
    isSkipped
  )
}

object ApiPartialFormAnswer {
  implicit val formReads = Json.reads[ApiPartialFormAnswer]
}
