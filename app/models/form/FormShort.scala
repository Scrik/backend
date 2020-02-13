/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.form

/**
  * Short form model, without elements.
  */
case class FormShort(
  id: Long,
  name: String,
  kind: Form.Kind,
  showInAggregation: Boolean,
  machineName: String
) {

  /**
    * Returns full form model.
    *
    * @param elements form elements
    */
  def withElements(elements: Seq[Form.Element]) = Form(
    id,
    name,
    elements,
    kind,
    showInAggregation,
    machineName
  )
}
