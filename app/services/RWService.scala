package services

import models.{LogInUser, RegisterUser}

import scala.collection.mutable.ListBuffer


trait RWService {

  def getUserList : List[RegisterUser]

  def read(loginUser: LogInUser): String

  def write(newUser: RegisterUser): String

  def getUser(username: String): RegisterUser

  def resumeUser(username: String) : Boolean

  def suspendUser(username: String): Boolean

}
