import org.scalatest.FunSuite
import my.playground.utils.Handlebars

import org.json4s._
import org.json4s.jackson.JsonMethods._

case class TestHB(name:String)

class HandlebarsTest extends FunSuite {

  implicit val formats = org.json4s.DefaultFormats

  test("render")
  {
    assert(Handlebars("hello.txt",Extraction.decompose(TestHB("theName"))) == "hello from Handelbars theName")
  }
}
