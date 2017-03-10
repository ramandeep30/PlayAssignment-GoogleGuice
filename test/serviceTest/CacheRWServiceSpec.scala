package serviceTest

import models.RegisterUser
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import services.CacheRWService


class CacheRWServiceSpec extends PlaySpec with MockitoSugar{

  "CacheRWService should " should {

    val cache = mock[CacheApi]
    val cacheRWService = new CacheRWService(cache)

    " write user object in empty cache " in {
      val newUser = RegisterUser("Raman", "", "Kaur", "ramandeep30", "1234", "1234",
        "1234567899", "female", 23, "", true, false)
      when(cache.get[RegisterUser]("ramandeep30")) thenReturn None
      when(cache.get[List[String]]("userlist")) thenReturn None
      cacheRWService.write(newUser) mustBe "ramandeep30"
    }

    " write another user object in cache " in {
      val newUser = RegisterUser("Raman", "", "Kaur", "ramandeep", "1234", "1234",
        "1234567899", "female", 23, "", true, false)
      when(cache.get[RegisterUser]("ramandeep")) thenReturn None
      when(cache.get[List[String]]("userlist")) thenReturn Some(List[String]("ramandeep30"))
      cacheRWService.write(newUser) mustBe "ramandeep"
    }

    "read user object from username " in {
      val newUser = RegisterUser("Raman", "", "Kaur", "ramandeep", "1234", "1234",
        "1234567899", "female", 23, "", true, false)
      when(cache.get[RegisterUser]("ramandeep")) thenReturn Some(newUser)
      cacheRWService.getUser("ramandeep") mustBe newUser
    }

    "get list of users from cache " in {
      val newUser = RegisterUser("Raman", "", "Kaur", "ramandeep", "1234", "1234",
        "1234567899", "female", 23, "", true, false)
      when(cache.get[List[String]]("userlist")) thenReturn Some(List[String]("ramandeep"))
      when(cache.get[RegisterUser]("ramandeep")) thenReturn Some(newUser)
      cacheRWService.getUserList mustBe List(newUser)
    }

    "Revoke user authorisation " in {
      cacheRWService.suspendUser("ramandeep") mustBe true
    }

    "Resume user authorisation " in {
      cacheRWService.resumeUser("ramandeep") mustBe true
    }

  }

}
