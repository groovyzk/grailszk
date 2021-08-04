import org.openqa.selenium.Dimension
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxProfile

driver = {
    FirefoxProfile profile = new FirefoxProfile()
    profile.setPreference("intl.accept_languages", "th, en-US, en")
    def firefox = new FirefoxDriver(profile)
    firefox.manage().window().size = new Dimension(670, 600)
    return firefox
}
