import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._

@RunWith(classOf[JUnitRunner])
class LoginSpec extends Specification {

  "Application" should {
    "allow user to login" in new WithBrowser {
      browser.goTo("/")
      browser.$("#username").text("bv")
      browser.$("#password").text("blackc0ff33")
      browser.$("button").click()

      browser.url must equalTo("/")
      browser.pageSource must contain("Find coffee addict")
      browser.pageSource must contain("Name index")
    }

    "deny login with invalid credentials" in new WithBrowser {
      browser.goTo("/")
      browser.$("#username").text("bv")
      browser.$("#password").text("bv")
      browser.$("button").click()

      browser.url must equalTo("/login")
      browser.pageSource must contain("Invalid username or password.")
    }
  }

}
