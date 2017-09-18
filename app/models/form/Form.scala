package models.form

import models.NamedEntity
import models.form.element.ElementKind

/**
  * Form template model.
  *
  * @param id                DB ID
  * @param name              form name
  * @param elements          form elements
  * @param kind              kind of the form
  * @param showInAggregation if form showed in aggregated report
  * @param machineName       machine name
  */
case class Form(
  id: Long,
  name: String,
  elements: Seq[Form.Element],
  kind: Form.Kind,
  showInAggregation: Boolean,
  machineName: String
) {

  /**
    * Returns short form.
    */
  def toShort = FormShort(id, name, kind, showInAggregation, machineName)
}

object Form {
  val nameSingular = "form"

  /**
    * Form element.
    *
    * @param id           DB ID
    * @param kind         type of element(textbox, radio, ...)
    * @param caption      caption
    * @param required     is element required
    * @param values       list of element values
    * @param competencies element competencies
    */
  case class Element(
    id: Long,
    kind: ElementKind,
    caption: String,
    required: Boolean,
    values: Seq[ElementValue],
    competencies: Seq[ElementCompetence]
  )

  /**
    * Form element value.
    *
    * @param id      DB ID
    * @param caption caption
    */
  case class ElementValue(
    id: Long,
    caption: String
  )

  /**
    * Form kind.
    */
  sealed trait Kind
  object Kind {

    /**
      * Active form used as template. Active forms can be listed, edited, deleted.
      */
    case object Active extends Kind

    /**
      * Freezed form. Freezed form is a copy of active form assigned to event.
      */
    case object Freezed extends Kind
  }

  /**
    * Element competence.
    */
  case class ElementCompetence(
    competence: NamedEntity,
    factor: Double
  )
}
