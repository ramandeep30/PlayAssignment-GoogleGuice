package services

import javax.inject.Inject

import models.{LogInUser, RegisterUser}
import org.mindrot.jbcrypt.BCrypt
import play.api.cache.CacheApi

class CacheRWService @Inject()(cache: CacheApi) extends RWService {

  override def read(loginUser: LogInUser): String = {
    val user: Option[RegisterUser] = cache.get[RegisterUser](loginUser.username)
    user match {
      case Some(x) =>
        println(s"verifying  ${loginUser.password}==${x.password}")
        if (BCrypt.checkpw(loginUser.password, x.password)) {
          if (!x.isRevoked) x.username else throw new Exception("Authorization revoked")
        } else throw new Exception("Password doesn't Match")
      case None => throw new Exception("No user")
    }
  }

  override def write(newUser: RegisterUser): String = {
    val user: Option[RegisterUser] = cache.get[RegisterUser](newUser.username)
    user match {
      case Some(x) => throw new Exception("User Already Exists")
      case None => {
        cache.set(newUser.username, newUser)
        cache.get[List[String]]("userlist") match {
          case Some(userList) =>
            val newUserList = userList :+ newUser.username
            cache.remove("userlist")
            cache.set("userlist", newUserList)
            println(newUserList)
          case None =>
            cache.set("userlist", List[String](newUser.username))
            println(newUser.username)
        }

        newUser.username
      }

    }
  }

  override def getUserList: List[RegisterUser] = {
    cache.get[List[String]]("userlist") match {
      case Some(usernameList) =>
        usernameList.map(x=>
          cache.get[RegisterUser](x) match {

            case Some(user: RegisterUser) => user
            case None => throw new Exception
          })
      case None => throw new Exception
    }
  }

  override def resumeUser(username: String): Boolean = {
    val user = getUser(username)
    val newUser = user.copy(isRevoked = false)
    cache.remove(user.username)
    cache.set(newUser.username, newUser)
    true
  }

  override def suspendUser(username: String): Boolean = {
    val user = getUser(username)
    val newUser = user.copy(isRevoked = true)
    cache.remove(user.username)
    cache.set(newUser.username, newUser)
    true
  }

  override def getUser(username: String): RegisterUser = {
    val user: Option[RegisterUser] = cache.get[RegisterUser](username)
    user match {
      case Some(x) => x
      case None => throw new Exception("User Not Found")
    }
  }


}