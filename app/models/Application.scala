package models

case class RegisterUser(firstName: String, middleName: String, lastName: String, username: String, password: String,
                        confirmPassword: String, contactNumber: String, gender: String, age: Int, hobbies: String,
                        isAdmin: Boolean, isRevoked: Boolean)

case class LogInUser(username: String, password: String)

