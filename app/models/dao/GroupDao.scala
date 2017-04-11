package models.dao

import javax.inject.{Inject, Singleton}

import models.ListWithTotal
import models.group.Group
import org.davidbild.tristate.Tristate
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.listmeta.ListMeta

import scala.async.Async._
import scala.concurrent.Future

trait GroupComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class GroupTable(tag: Tag) extends Table[Group](tag, "orgstructure") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def parentId = column[Option[Long]]("parent_id")
    def name = column[String]("name")

    def * = (id, parentId, name) <> ((Group.apply _).tupled, Group.unapply)
  }

  val Groups = TableQuery[GroupTable]
}

/**
  * Group DAO.
  */
@Singleton
class GroupDao @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
) extends HasDatabaseConfigProvider[JdbcProfile]
  with GroupComponent
  with DaoHelper {

  import driver.api._

  /**
    * Creates group.
    *
    * @param group group model
    * @return group model with ID
    */
  def create(group: Group): Future[Group] = async {
    val id = await(db.run(Groups.returning(Groups.map(_.id)) += group))
    group.copy(id = id)
  }

  /**
    * Returns list of groups filtered by given criteria.
    *
    * @param id       group ID
    * @param parentId group parent ID
    */
  def getList(
    id: Option[Long] = None,
    parentId: Tristate[Long] = Tristate.Unspecified
  )(implicit meta: ListMeta = ListMeta.default): Future[ListWithTotal[Group]] = {
    val query = Groups
      .applyFilter { x =>
        Seq(
          id.map(x.id === _),
          parentId match {
            case Tristate.Present(pid) => Some(x.parentId.fold(false: Rep[Boolean])(_ === pid))
            case Tristate.Absent => Some(x.parentId.isEmpty)
            case Tristate.Unspecified => None
          }
        )
      }

    runListQuery(query) {
      group => {
        case 'id => group.id
        case 'name => group.name
      }
    }
  }

  /**
    * Returns group by ID.
    *
    * @param id group ID
    * @return either some group or none
    */
  def findById(id: Long): Future[Option[Group]] = async {
    await(getList(id = Some(id))).data.headOption
  }

  /**
    * Updates group.
    *
    * @param group group model
    * @return updated group
    */
  def update(group: Group): Future[Group] = async {
    await(db.run(Groups.filter(_.id === group.id).update(group)))
    group
  }

  /**
    * Removes group.
    *
    * @param id group ID
    * @return number of rows affected
    */
  def delete(id: Long): Future[Int] = db.run {
    Groups.filter(_.id === id).delete
  }

  /**
    * Returns group children IDs.
    *
    * @param id group ID
    */
  def findChildrenIds(id: Long): Future[Seq[Long]] = {
    val sql =
      sql"""
         WITH RECURSIVE T(id) AS (
           SELECT id FROM orgstructure g WHERE id = $id
           UNION ALL
           SELECT gc.id FROM T JOIN orgstructure gc ON gc.parent_id = T.id
         )
         SELECT  id
         FROM    T
         WHERE T.id <> $id
         """.as[Long]

    db.run(sql)
  }
}
