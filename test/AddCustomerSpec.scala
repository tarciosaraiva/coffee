import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._

@RunWith(classOf[JUnitRunner])
class AddCustomerSpec extends Specification {

  "Application" should {
    "allow customer to be created with default flags" in new WithBrowser {
      browser.goTo("/")
      browser.$("#username").text("bv")
      browser.$("#password").text("blackc0ff33")
      browser.$("button").click()

      browser.url must equalTo("/")
      browser.pageSource must contain("Find coffee addict")
      browser.pageSource must contain("Name index")

      browser.$("#add-customer").click()
      browser.$("#name").text("Super user")
      browser.$("#add-customer-form").submit()

      browser.pageSource must contain("Client created successfully.")
      browser.$("#name-index").find("S")
    }
  }

}
