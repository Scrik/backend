package utils

import javax.inject.Singleton

import controllers.api.Response.Error
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Custom error handler.
  */
@Singleton
class ErrorHandler extends HttpErrorHandler with Logger {
  private val statusCodesMapping = Map(
    415 -> 400 // wrong json format now is bad request
  )

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = async {
    val code = statusCodesMapping.getOrElse(statusCode, statusCode)
    Status(code)(Json.toJson(Error("GENERAL-1", message)))
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = async {
    log.error("Server error", exception)
    InternalServerError(Json.toJson(Error("GENERAL-2", "Server error")))
  }
}
