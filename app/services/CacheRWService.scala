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
        val newUserList = cache.getOrElse[List[String]]("userlist") { List[String]() } :+ newUser.username
        cache.remove("userlist")
        cache.set("userlist", newUserList)
        println(newUserList)
        newUser.username
      }

    }
  }

  override def getUserList: List[RegisterUser] = {
    val usernameList = cache.getOrElse[List[String]]("userlist") {
      List()
    }
    usernameList.map(x=>cache.getOrElse[RegisterUser](x){ throw new Exception })
  }

  override def resumeUser(username: String): Unit = {
    val user = getUser(username)
    val newUser = user.copy(isRevoked = false)
    cache.remove(user.username)
    cache.set(newUser.username, newUser)
  }

  override def suspendUser(username: String) = {
    val user = getUser(username)
    val newUser = user.copy(isRevoked = true)
    cache.remove(user.username)
    cache.set(newUser.username, newUser)

  }

  override def getUser(username: String): RegisterUser = {
    val user: Option[RegisterUser] = cache.get[RegisterUser](username)
    user match {
      case Some(x) => x
      case None => throw new Exception("User Not Found")
    }
  }


}