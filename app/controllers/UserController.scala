
package controllers

import com.google.inject.Inject
import models.{LogInUser, RegisterUser}
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{Action, Controller}
import services.RWService

class UserController @Inject()(rWService: RWService) extends Controller {

  val registerUser = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "middleName" -> text,
      "lastName" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "confirmPassword" -> nonEmptyText,
      "contactNumber" -> nonEmptyText,
      "gender" -> text,
      "age" -> number(min = 18, max = 75),
      "hobbies" -> nonEmptyText,
      "isAdmin" -> boolean,
      "isRevoked" -> boolean
    )(RegisterUser)(RegisterUser.unapply).verifying(data => validatePassword(data.password, data.confirmPassword))
  )
  val logInUser = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)
    (LogInUser.apply)(LogInUser.unapply)
  )

  def validatePassword(password: String, confirmPassword: String): Boolean = {
     (password == confirmPassword)
  }

  def login = Action { implicit request =>
    val userData = request.body
    logInUser.bindFromRequest.fold(
      formWithErrors => {
        println(formWithErrors)
        BadRequest(views.html.signInPage())
      },

      userData => {
        val newUser = models.LogInUser(userData.username, userData.password)
        try {
          val username = rWService.read(newUser)
          Redirect(routes.UserController.profile())
            .withSession("connected" -> username)
        }
        catch {
          case ex: Exception =>
            println(ex.getMessage)
            Ok(views.html.registrationPage())
        }
      }
    )
  }

  def profile = Action { request =>
    request.session.get("connected").map{ username =>
      try {
        val user = rWService.getUser(username)
        Ok(views.html.profilePage(user))
      }
      catch {
        case ex: Exception => Unauthorized(ex.getMessage)
      }
    }.getOrElse(
      Unauthorized("Oops, You are not connected.")
    )
  }

  def signUp = Action {
    Ok(views.html.registrationPage())
  }

  def signIn = Action {
    Ok(views.html.signInPage())
  }

  def register = Action {
    implicit request =>
      val userData = request.body
      registerUser.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.registrationPage())
        },
        userData => {
          val passwordHash = BCrypt.hashpw(userData.confirmPassword, BCrypt.gensalt())
          val user = RegisterUser(userData.firstName, userData.middleName, userData.lastName, userData.username, passwordHash,
            passwordHash, userData.contactNumber, userData.gender, userData.age, userData.hobbies, isAdmin, false)
          println(s"data entered:::${user.toString}")
          try{
            val username = rWService.write(user)
            println(s"hashed pass:::$passwordHash")
            Redirect(routes.UserController.profile())
              .withSession("connected" -> username)
          }
          catch {
            case ex: Exception => println(ex.getMessage)
              Ok(views.html.registrationPage())
          }
        }
      )
  }

  private def isAdmin: Boolean = {
    play.Play.application().configuration().getString("Type")=="Admin"
  }


}
