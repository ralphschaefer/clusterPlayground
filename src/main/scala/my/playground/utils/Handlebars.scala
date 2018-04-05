package my.playground.utils

import java.io.IOException

import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import com.github.jknack.handlebars.{Context, Handlebars => HB, Template, JsonNodeValueResolver}
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait Handlebars {

  def apply(templateFile:String, value:JValue):String

  def render(template:String, value:JValue):String

}

object Handlebars extends Handlebars {

  case class HandlebarsException(msg:String) extends Exception("Handlebars Exception: "+ msg)

  private val cache:collection.mutable.HashMap[String,Template] = collection.mutable.HashMap()

  private val loader = new ClassPathTemplateLoader("/templates", ".handlebars")

  private val handlebars = new HB(loader)

  def apply(templateFile:String, value:JValue):String = {
    try {
      val template:Template = cache.get(templateFile) match {
        case Some(cached) => cached
        case _ =>
          val uncached = handlebars.compile(templateFile)
          cache += (templateFile -> uncached)
          uncached
      }
      template.apply(
        Context
          .newBuilder( asJsonNode(value) )
          .resolver(JsonNodeValueResolver.INSTANCE)
          .build()
      )
    }
    catch {
      case e:IOException =>
        throw HandlebarsException(s"ERR template '$templateFile' : " + e.getMessage)
    }
  }

  def render(templateString:String, value:JValue):String = {
    try {
      val template = handlebars.compileInline(templateString)
      template.apply(
        Context
          .newBuilder( asJsonNode(value))
          .resolver(JsonNodeValueResolver.INSTANCE)
          .build()
      )
    }
    catch {
      case e:IOException =>
        throw HandlebarsException(s"ERR template : " + e.getMessage)
    }
  }

}
