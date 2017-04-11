package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.test.FakeEnvironment
import controllers.api.Response
import controllers.api.group.{ApiGroup, ApiPartialGroup}
import models.ListWithTotal
import models.group.Group
import models.user.User
import org.davidbild.tristate.Tristate
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.GroupService
import silhouette.DefaultEnv
import testutils.generator.{GroupGenerator, TristateGenerator}
import utils.errors.NotFoundError
import utils.listmeta.ListMeta

/**
  * Test for groups controller.
  */
class GroupControllerTest extends BaseControllerTest with GroupGenerator with TristateGenerator {

  private case class TestFixture(
    silhouette: Silhouette[DefaultEnv],
    groupServiceMock: GroupService,
    controller: GroupController
  )

  private def getFixture(environment: FakeEnvironment[DefaultEnv]) = {
    val silhouette = getSilhouette(environment)
    val groupServiceMock = mock[GroupService]
    val controller = new GroupController(silhouette, groupServiceMock)
    TestFixture(silhouette, groupServiceMock, controller)
  }

  private val admin = User(1, None, None, User.Role.Admin, User.Status.Approved)

  "GET /groups/id" should {
    "return not found if group not found" in {
      forAll { (id: Long) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.getById(id)(admin)).thenReturn(toFuture(Left(NotFoundError.Group(id))))
        val request = authenticated(FakeRequest(), env)

        val response = fixture.controller.getById(id).apply(request)
        status(response) mustBe NOT_FOUND
      }
    }

    "return json with group" in {
      forAll { (id: Long, group: Group) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.getById(id)(admin)).thenReturn(toFuture(Right(group)))
        val request = authenticated(FakeRequest(), env)

        val response = fixture.controller.getById(id)(request)
        status(response) mustBe OK
        val groupJson = contentAsJson(response)
        groupJson mustBe Json.toJson(ApiGroup(group))
      }
    }
  }

  "GET /groups" should {
    "return groups list from service" in {
      forAll { (
      parentId: Tristate[Long],
      total: Int,
      groups: Seq[Group]
      ) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.list(parentId)(admin, ListMeta.default))
          .thenReturn(toFuture(Right(ListWithTotal(total, groups))))
        val request = authenticated(FakeRequest(), env)

        val response = fixture.controller.getList(parentId)(request)

        status(response) mustBe OK
        val groupsJson = contentAsJson(response)
        val expectedJson = Json.toJson(
          Response.List(Response.Meta(total, ListMeta.default), groups.map(ApiGroup(_)))
        )
        groupsJson mustBe expectedJson
      }
    }
    "return forbidden for non admin user" in {
      val env = fakeEnvironment(admin.copy(role = User.Role.User))
      val fixture = getFixture(env)
      val request = authenticated(FakeRequest(), env)
      val response = fixture.controller.getList(Tristate.Unspecified).apply(request)

      status(response) mustBe FORBIDDEN
    }
  }

  "PUT /groups" should {
    "update groups" in {
      forAll { (group: Group) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.update(group)(admin))
          .thenReturn(toFuture(Right(group)))

        val partialGroup = ApiPartialGroup(group.parentId, group.name)
        val request = authenticated(
          FakeRequest("PUT", "/groups")
            .withBody[ApiPartialGroup](partialGroup)
            .withHeaders(CONTENT_TYPE -> "application/json"),
          env
        )

        val response = fixture.controller.update(group.id).apply(request)
        val responseJson = contentAsJson(response)
        val expectedJson = Json.toJson(ApiGroup(group))

        status(response) mustBe OK
        responseJson mustBe expectedJson
      }
    }
  }

  "POST /groups" should {
    "create groups" in {
      forAll { (group: Group) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.create(group.copy(id = 0))(admin))
          .thenReturn(toFuture(Right(group)))

        val partialGroup = ApiPartialGroup(group.parentId, group.name)
        val request = authenticated(
          FakeRequest("POST", "/groups")
            .withBody[ApiPartialGroup](partialGroup)
            .withHeaders(CONTENT_TYPE -> "application/json"),
          env
        )

        val response = fixture.controller.create.apply(request)
        val responseJson = contentAsJson(response)
        val expectedJson = Json.toJson(ApiGroup(group))

        status(response) mustBe CREATED
        responseJson mustBe expectedJson
      }
    }
  }

  "DELETE /groups" should {
    "delete groups" in {
      forAll { (id: Long) =>
        val env = fakeEnvironment(admin)
        val fixture = getFixture(env)
        when(fixture.groupServiceMock.delete(id)(admin)).thenReturn(toFuture(Right(())))
        val request = authenticated(FakeRequest(), env)

        val response = fixture.controller.delete(id)(request)
        status(response) mustBe NO_CONTENT
      }
    }
  }
}
